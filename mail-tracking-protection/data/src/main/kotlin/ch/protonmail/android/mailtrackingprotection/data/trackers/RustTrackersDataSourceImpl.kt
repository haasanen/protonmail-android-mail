/*
 * Copyright (c) 2026 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailtrackingprotection.data.trackers

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailtrackingprotection.data.wrapper.RustTrackersWrapper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import timber.log.Timber
import uniffi.proton_mail_uniffi.TrackerDomain
import uniffi.proton_mail_uniffi.WatchTrackerInfoStreamNextAsyncResult
import javax.inject.Inject

class RustTrackersDataSourceImpl @Inject constructor() : RustTrackersDataSource {

    override fun observeTrackers(
        rustTrackersWrapper: RustTrackersWrapper,
        messageId: LocalMessageId
    ): Flow<Either<DataError, List<TrackerDomain>>> = callbackFlow {
        Timber.d("rust-tracker-protection: Starting tracking protection observation")

        rustTrackersWrapper.watchTrackerInfoStream(messageId).onLeft { error ->
            Timber.e("rust-tracker-protection: Failed to create stream watcher: $error")
            trySend(error.left()).onFailure { throwable ->
                Timber.w("Failed to send error: $throwable")
                close(throwable)
                return@callbackFlow
            }
            close()
            return@callbackFlow
        }.onRight { stream ->
            Timber.d("rust-tracker-protection: Created tracking info watcher")

            val initialInfo = stream.initialInfo()?.trackers
            if (initialInfo != null) send(initialInfo.right())

            while (isActive) {
                when (val nextValueResult = stream.nextAsync()) {
                    is WatchTrackerInfoStreamNextAsyncResult.Error -> {
                        Timber.w("rust-tracker-protection: received new watcher error - ${nextValueResult.v1}")
                        close()
                        return@callbackFlow
                    }

                    WatchTrackerInfoStreamNextAsyncResult.Ok -> {
                        rustTrackersWrapper.getTrackerInfoForMessage(messageId).onLeft { error ->
                            Timber.w("rust-tracker-protection: error on getting next value - $error")
                            close()
                            return@callbackFlow
                        }.onRight { trackerInfo ->
                            val update = trackerInfo?.trackers
                            if (update != null) send(update.right())
                        }
                    }
                }
            }
        }

        awaitClose {
            Timber.d("rust-tracker-protection: Closing watcher")
        }
    }
}
