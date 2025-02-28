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

package ch.protonmail.android.composer.data.local

import ch.protonmail.android.composer.data.mapper.toMessageSendingStatus
import ch.protonmail.android.composer.data.usecase.CreateRustDraftSendWatcher
import ch.protonmail.android.mailcommon.datarust.mapper.LocalDraftSendResult
import ch.protonmail.android.mailcomposer.domain.model.MessageSendingStatus
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.DraftSendResultCallback
import uniffi.proton_mail_uniffi.DraftSendResultWatcher
import javax.inject.Inject

class RustSendingStatusDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustDraftSendWatcher: CreateRustDraftSendWatcher
) : RustSendingStatusDataSource {

    private var draftSendResultWatcher: DraftSendResultWatcher? = null
    private val sendResultsFlow = MutableSharedFlow<MessageSendingStatus>(1)


    private val draftSendResultCallback = object : DraftSendResultCallback {
        override fun onNewSendResult(sendResults: List<LocalDraftSendResult>) {
            Timber.d("rust-draft: draft send result updated: ${sendResults.get(0).toMessageSendingStatus()}")
            for (result in sendResults) {
                sendResultsFlow.tryEmit(result.toMessageSendingStatus())
            }
        }
    }


    override suspend fun observeMessageSendingStatus(userId: UserId): Flow<MessageSendingStatus> {
        Timber.d("rust-draft: Observing message sending status...")
        if (draftSendResultWatcher == null) {

            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-draft: Trying to observe sending status; Failing.")
                return flowOf()
            }

            draftSendResultWatcher = createRustDraftSendWatcher(session, draftSendResultCallback).getOrNull()
            if (draftSendResultWatcher == null) {
                Timber.e("rust-draft: Failed to create draft send result watcher; Failing.")
                return flowOf()
            }
            Timber.d("rust-draft: Draft send result watcher created.")
        }

        return sendResultsFlow
    }
}
