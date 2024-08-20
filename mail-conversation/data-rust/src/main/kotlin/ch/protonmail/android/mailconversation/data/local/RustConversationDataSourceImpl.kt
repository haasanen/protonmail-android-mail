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

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import uniffi.proton_mail_uniffi.applyLabelToConversations
import uniffi.proton_mail_uniffi.markConversationsAsUnread
import uniffi.proton_mail_uniffi.markConverstionsAsRead
import uniffi.proton_mail_uniffi.removeLabelFromConversations
import javax.inject.Inject

class RustConversationDataSourceImpl @Inject constructor(
    private val sessionManager: UserSessionRepository,
    private val mailSessionRepository: MailSessionRepository,
    private val rustMailbox: RustMailbox,
    private val rustConversationQuery: RustConversationQuery,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustConversationDataSource {

    override suspend fun getConversations(userId: UserId, labelId: LocalLabelId): List<LocalConversation> =
        rustConversationQuery.observeConversations(userId, labelId).first()

    override fun observeConversations(
        userId: UserId,
        conversationIds: List<LocalConversationId>
    ): Flow<List<LocalConversation>> {
        Timber.d("rust-conversation: observeConversations for conversationIds: $conversationIds")


        /*  return rustMailbox.observeConversationMailbox().mapLatest { mailbox ->
              try {
                  val userSession = sessionManager.getUserSession(userId)
                  if (userSession == null) {
                      Timber.d("rust-conversation: observeConversations failed due to null session for $userId")
                      return@mapLatest emptyList()
                  }
                  val currentLabelId = mailbox.labelId()
                  val conversationList = mutableListOf<LocalConversation>()

                  conversationIds.forEach { conversationId ->
                      userSession.conversationWithIdAndContext(conversationId, currentLabelId)?.let { conversation ->
                          conversationList.add(conversation)
                      }
                  }

                  conversationList
              } catch (e: MailboxException) {
                  Timber.e(e,
                  "rust-conversation: Failed to observe conversations for conversationIds: $conversationIds")
                  emptyList()
              }
          }*/
        return flowOf()
    }

    override fun observeConversation(userId: UserId, conversationId: LocalConversationId): Flow<LocalConversation> =
        rustConversationQuery.observeConversation(userId, conversationId)

    override suspend fun deleteConversations(userId: UserId, conversations: List<LocalConversationId>) {
        executeMailboxAction(
            userId = userId,
            action = { deleteConversations(userId, conversations) },
            actionName = "delete conversations"
        )
    }

    override suspend fun markRead(userId: UserId, conversations: List<LocalConversationId>) {
        executeMailSessionAction(
            userId = userId,
            action = { mailSession -> markConverstionsAsRead(mailSession, conversations) },
            actionName = "mark as read"
        )
    }

    override suspend fun markUnread(userId: UserId, conversations: List<LocalConversationId>) {
        executeMailSessionAction(
            userId = userId,
            action = { mailSession -> markConversationsAsUnread(mailSession, conversations) },
            actionName = "mark as unread"
        )
    }

    override suspend fun starConversations(userId: UserId, conversations: List<LocalConversationId>) {
        executeMailSessionAction(
            userId = userId,
            action = { starConversations(userId, conversations) },
            actionName = "star conversations"
        )
    }

    override suspend fun unStarConversations(userId: UserId, conversations: List<LocalConversationId>) {
        executeMailSessionAction(
            userId = userId,
            action = { unStarConversations(userId, conversations) },
            actionName = "unstar conversations"
        )
    }

    override suspend fun relabel(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        labelsToBeRemoved: List<LocalLabelId>,
        labelsToBeAdded: List<LocalLabelId>
    ) {
        executeMailSessionAction(
            userId = userId,
            action = { session ->
                labelsToBeRemoved.forEach { localLabelId ->
                    removeLabelFromConversations(session, localLabelId, conversationIds)
                }

                labelsToBeAdded.forEach { localLabelId ->
                    applyLabelToConversations(session, localLabelId, conversationIds)
                }
            },
            actionName = "relabel conversations"
        )
    }

    @MissingRustApi
    // ET - Missing Implementation. This function requires Rust settings integration
    override fun getSenderImage(address: String, bimi: String?): ByteArray? = null

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

    override fun disconnect() {
        rustConversationQuery.disconnect()
    }

    private suspend fun executeMailboxAction(
        userId: UserId,
        action: suspend (Mailbox) -> Unit,
        actionName: String
    ) {
        val mailbox = rustMailbox.observeConversationMailbox().firstOrNull()
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

    private suspend fun executeMailSessionAction(
        userId: UserId,
        action: suspend (MailSession) -> Unit,
        actionName: String
    ) {
        val mailSession = mailSessionRepository.getMailSession()

        try {
            action(mailSession)
            executePendingActions(userId)
        } catch (e: MailboxException) {
            Timber.e(e, "rust-conversation: Failed to perform $actionName")
        }
    }

    private fun executePendingActions(userId: UserId) {
        coroutineScope.launch {
            sessionManager.getUserSession(userId)?.executePendingActions()
        }
    }
}
