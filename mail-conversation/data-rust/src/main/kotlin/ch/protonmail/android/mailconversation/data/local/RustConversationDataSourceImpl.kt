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

package ch.protonmail.android.mailconversation.data.local

import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversation
import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailconversation.data.usecase.GetRustAvailableConversationActions
import ch.protonmail.android.mailconversation.data.usecase.GetRustConversationMoveToActions
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.ConversationAvailableActions
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import uniffi.proton_mail_uniffi.MoveAction
import uniffi.proton_mail_uniffi.applyLabelToConversations
import uniffi.proton_mail_uniffi.markConversationsAsRead
import uniffi.proton_mail_uniffi.markConversationsAsUnread
import uniffi.proton_mail_uniffi.removeLabelFromConversations
import uniffi.proton_mail_uniffi.starConversations
import uniffi.proton_mail_uniffi.unstarConversations
import javax.inject.Inject

class RustConversationDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val rustMailbox: RustMailbox,
    private val rustConversationDetailQuery: RustConversationDetailQuery,
    private val rustConversationsQuery: RustConversationsQuery,
    private val getRustAvailableConversationActions: GetRustAvailableConversationActions,
    private val getRustConversationMoveToActions: GetRustConversationMoveToActions,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustConversationDataSource {

    /**
     * Gets the first x conversations for this labelId.
     * Adds in an Invalidation Observer on the label that will be fired when any conversation
     * in the label changes
     */
    override suspend fun getConversations(userId: UserId, pageKey: PageKey): List<LocalConversation> =
        rustConversationsQuery.getConversations(userId, pageKey) ?: emptyList()

    override fun observeConversation(userId: UserId, conversationId: LocalConversationId): Flow<LocalConversation>? =
        runCatching { rustConversationDetailQuery.observeConversation(userId, conversationId) }
            .onFailure { Timber.w("rust-conversation: failed to observe conversation $it") }
            .getOrNull()

    override fun observeConversationMessages(
        userId: UserId,
        conversationId: LocalConversationId
    ): Flow<LocalConversationMessages> = rustConversationDetailQuery.observeConversationMessages(
        userId, conversationId
    )

    override suspend fun deleteConversations(userId: UserId, conversations: List<LocalConversationId>) {
        executeMailboxAction(
            userId = userId,
            action = { deleteConversations(userId, conversations) },
            actionName = "delete conversations"
        )
    }

    override suspend fun markRead(userId: UserId, conversations: List<LocalConversationId>) {
        executeUserSessionAction(
            userId = userId,
            action = { userSession -> markConversationsAsRead(userSession, conversations) },
            actionName = "mark as read"
        )
    }

    override suspend fun markUnread(userId: UserId, conversations: List<LocalConversationId>) {
        executeMailboxAction(
            userId = userId,
            action = { mailbox -> markConversationsAsUnread(mailbox, conversations) },
            actionName = "mark as unread"
        )
    }

    override suspend fun starConversations(userId: UserId, conversations: List<LocalConversationId>) {
        executeUserSessionAction(
            userId = userId,
            action = { userSession -> starConversations(userSession, conversations) },
            actionName = "star conversations"
        )
    }

    override suspend fun unStarConversations(userId: UserId, conversations: List<LocalConversationId>) {
        executeUserSessionAction(
            userId = userId,
            action = { userSession -> unstarConversations(userSession, conversations) },
            actionName = "unstar conversations"
        )
    }

    override suspend fun relabel(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        labelsToBeRemoved: List<LocalLabelId>,
        labelsToBeAdded: List<LocalLabelId>
    ) {
        executeUserSessionAction(
            userId = userId,
            action = { userSession ->
                labelsToBeRemoved.forEach { localLabelId ->
                    removeLabelFromConversations(userSession, localLabelId, conversationIds)
                }

                labelsToBeAdded.forEach { localLabelId ->
                    applyLabelToConversations(userSession, localLabelId, conversationIds)
                }
            },
            actionName = "relabel conversations"
        )
    }

    @MissingRustApi
    // ET - Missing Implementation. This function requires Rust settings integration
    override fun getSenderImage(address: String, bimi: String?): ByteArray? = null

    override suspend fun getAvailableActions(
        userId: UserId,
        labelId: LocalLabelId,
        conversationIds: List<LocalConversationId>
    ): ConversationAvailableActions? {
        val mailbox = rustMailbox.observeMailbox(labelId).firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-conversation: trying to get available actions for null Mailbox! failing")
            return null
        }

        return getRustAvailableConversationActions(mailbox, conversationIds)
    }

    override suspend fun getAvailableSystemMoveToActions(
        userId: UserId,
        labelId: LocalLabelId,
        conversationIds: List<LocalConversationId>
    ): List<MoveAction.SystemFolder>? {
        val mailbox = rustMailbox.observeMailbox(labelId).firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-conversation: trying to get available actions for null Mailbox! failing")
            return null
        }
        val moveActions = getRustConversationMoveToActions(mailbox, conversationIds)
        return moveActions.filterIsInstance<MoveAction.SystemFolder>()
    }

    override suspend fun moveConversations(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        toLabelId: LocalLabelId
    ) {
        executeMailboxAction(
            userId = userId,
            action = { moveConversations(userId, conversationIds, toLabelId) },
            actionName = "move conversations"
        )
    }

    private suspend fun executeUserSessionAction(
        userId: UserId,
        action: suspend (MailUserSession) -> Unit,
        actionName: String
    ) {
        val userSession = userSessionRepository.getUserSession(userId)
        if (userSession == null) {
            Timber.e("rust-conversation: Failed to perform $actionName, null user session")
            return
        }

        try {
            action(userSession)
            executePendingActions(userId)
        } catch (e: MailboxException) {
            Timber.e(e, "rust-conversation: Failed to perform $actionName")
        }
    }
    private suspend fun executeMailboxAction(
        userId: UserId,
        action: suspend (Mailbox) -> Unit,
        actionName: String
    ) {
        val mailbox = rustMailbox.observeMailbox().firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-conversation: Failed to perform $actionName, null mailbox")
            return
        }

        try {
            action(mailbox)
            executePendingActions(userId)
        } catch (e: MailboxException) {
            Timber.e(e, "rust-conversation: Failed to perform $actionName")
        }
    }

    private fun executePendingActions(userId: UserId) {
        coroutineScope.launch {
            userSessionRepository.getUserSession(userId)?.executePendingActions()
        }
    }
}
