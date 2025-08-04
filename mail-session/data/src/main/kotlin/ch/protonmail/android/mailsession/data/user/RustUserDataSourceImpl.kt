/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsession.data.user

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import uniffi.proton_mail_uniffi.AsyncLiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSessionWatchUserResult
import uniffi.proton_mail_uniffi.User
import uniffi.proton_mail_uniffi.WatchHandle
import javax.inject.Inject

class RustUserDataSourceImpl @Inject constructor() : RustUserDataSource {

    override fun observeUser(mailUserSession: MailUserSessionWrapper): Flow<Either<DataError, User>> = callbackFlow {
        Timber.d("rust-user: Starting user observation")
        var watcher: WatchHandle? = null
        val updateCallback = object : AsyncLiveQueryCallback {
            override suspend fun onUpdate() {
                Timber.d("rust-user: user updated")
                mailUserSession.getUser()
                    .onLeft { error -> send(error.left()) }
                    .onRight { user -> send(user.right()) }
            }
        }

        mailUserSession.getUser().onLeft { error ->
            Timber.e("rust-user: Failed to get user: $error")
            send(error.left())
            close()
            return@callbackFlow
        }.onRight { user ->
            send(user.right())

            Timber.d("rust-user: Got user, creating watcher")
            when (val watcherResult = mailUserSession.watchUser(updateCallback)) {

                is MailUserSessionWatchUserResult.Error -> {
                    Timber.e("rust-user: Failed to create watcher: ${watcherResult.v1}")
                    send(watcherResult.v1.toDataError().left())
                    close()
                    return@callbackFlow
                }

                is MailUserSessionWatchUserResult.Ok -> {
                    Timber.d("rust-user: Created user watcher")
                    watcher = watcherResult.v1
                }
            }
        }

        awaitClose {
            Timber.d("rust-user: Closing watcher")
            watcher?.disconnect()
        }
    }
}
