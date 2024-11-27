/*
 * Copyright (c) 2022 Proton Technologies AG
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

package ch.protonmail.android.mailcomposer.domain.usecase

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.coroutines.DefaultDispatcher
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
@MissingRustApi
// To be bound to rust or dropped when implementing send
class DraftUploader @Inject constructor(
    @DefaultDispatcher
    private val dispatcher: CoroutineDispatcher
) {

    private var syncJob: Job? = null

    suspend fun startContinuousUpload(
        userId: UserId,
        messageId: MessageId,
        action: DraftAction,
        scope: CoroutineScope
    ) {
        Timber.w("Not implemented")
    }

    suspend fun upload(userId: UserId, messageId: MessageId) {
        Timber.w("Not implemented")
    }

    fun stopContinuousUpload() {
        syncJob?.cancel()
    }

    companion object {
        val SyncInterval = 1.seconds
    }
}
