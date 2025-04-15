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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.mapper.toMessageSendingStatus
import ch.protonmail.android.composer.data.usecase.CreateRustDraftSendWatcher
import ch.protonmail.android.composer.data.usecase.RustDeleteDraftSendResult
import ch.protonmail.android.composer.data.usecase.RustMarkDraftSendResultAsSeen
import ch.protonmail.android.composer.data.usecase.RustQueryUnseenDraftSendResults
import ch.protonmail.android.mailcommon.data.mapper.LocalDraftSendResult
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.MessageSendingStatus
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.DraftSendResultCallback
import uniffi.proton_mail_uniffi.DraftSendResultWatcher
import javax.inject.Inject

class RustSendingStatusDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustDraftSendWatcher: CreateRustDraftSendWatcher,
    private val rustQueryUnseenDraftSendResults: RustQueryUnseenDraftSendResults,
    private val rustDeleteDraftSendResult: RustDeleteDraftSendResult,
    private val rustMarkDraftSendResultAsSeen: RustMarkDraftSendResultAsSeen
) : RustSendingStatusDataSource {

    private var draftSendResultWatcher: DraftSendResultWatcher? = null
    private val mutex = Mutex()
    private val sendResultsFlow = MutableSharedFlow<MessageSendingStatus>(1)

    private val draftSendResultCallback = object : DraftSendResultCallback {
        override fun onNewSendResult(sendResults: List<LocalDraftSendResult>) {
            for (result in sendResults) {
                sendResultsFlow.tryEmit(result.toMessageSendingStatus())
            }
        }
    }

    override suspend fun observeMessageSendingStatus(userId: UserId): Flow<MessageSendingStatus> {

        initialiseDraftSendResultWatcher(userId)

        return draftSendResultWatcher?.let {
            sendResultsFlow
        } ?: run {
            Timber.e("rust-draft: Failed to observe message sending status.")
            flowOf()
        }
    }

    override suspend fun queryUnseenMessageSendingStatuses(
        userId: UserId
    ): Either<DataError, List<MessageSendingStatus>> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to query unseen sending status; Failing.")
            return DataError.Local.Unknown.left()
        }
        return rustQueryUnseenDraftSendResults(session).fold(
            ifLeft = { error ->
                Timber.e("rust-draft: Query unseen sending status failed with error: $error")
                error.left()
            },
            ifRight = { results ->
                results.map { it.toMessageSendingStatus() }.right()
            }
        )
    }

    override suspend fun deleteMessageSendingStatuses(
        userId: UserId,
        messageIds: List<MessageId>
    ): Either<DataError, Unit> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to delete sending status; Failing.")
            return DataError.Local.Unknown.left()
        }
        return rustDeleteDraftSendResult(session, messageIds.map { it.toLocalMessageId() }).fold(
            ifLeft = { error ->
                Timber.e("rust-draft: Delete sending status failed with error: $error")
                error.left()
            },
            ifRight = { Unit.right() }
        )
    }

    override suspend fun markMessageSendingStatusesAsSeen(
        userId: UserId,
        messageIds: List<MessageId>
    ): Either<DataError, Unit> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to mark sending status as seen; Failing.")
            return DataError.Local.Unknown.left()
        }
        return rustMarkDraftSendResultAsSeen(session, messageIds.map { it.toLocalMessageId() }).fold(
            ifLeft = { error ->
                Timber.e("rust-draft: Mark sending status as seen failed with error: $error")
                error.left()
            },
            ifRight = { Unit.right() }
        )
    }

    private suspend fun initialiseDraftSendResultWatcher(userId: UserId) {
        mutex.withLock {
            if (draftSendResultWatcher != null) return@withLock

            val session = userSessionRepository.getUserSession(userId) ?: run {
                Timber.e("rust-draft: Trying to observe sending status; Failing.")
                return@withLock
            }

            draftSendResultWatcher = createRustDraftSendWatcher(session, draftSendResultCallback).getOrNull()
                ?: run {
                    Timber.e("rust-draft: Failed to create draft send result watcher; Failing.")
                    return@withLock
                }

            Timber.d("rust-draft: Draft send result watcher created.")
        }
    }

}
