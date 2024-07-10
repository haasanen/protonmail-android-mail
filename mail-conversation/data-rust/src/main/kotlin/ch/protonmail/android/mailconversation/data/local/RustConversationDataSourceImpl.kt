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

import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber
import uniffi.proton_api_mail.LabelId
import uniffi.proton_mail_common.LocalConversation
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import javax.inject.Inject

class RustConversationDataSourceImpl @Inject constructor(
    private val sessionManager: UserSessionRepository,
    private val rustMailbox: RustMailbox,
    private val rustConversationQuery: RustConversationQuery,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustConversationDataSource {

    override suspend fun getConversations(labelId: LocalLabelId): List<LocalConversation> =
        rustConversationQuery.observeConversations(labelId).first()

    override suspend fun deleteConversations(conversations: List<LocalConversationId>) {

        executeAction({ mailbox ->
            mailbox.deleteConversations(conversations)
        }, "delete conversations")
    }

    override suspend fun markRead(conversations: List<LocalConversationId>) {

        executeAction({ mailbox ->
            mailbox.markConversationsRead(conversations)
        }, "mark as read")

    }

    override suspend fun markUnread(conversations: List<LocalConversationId>) {

        executeAction({ mailbox ->
            mailbox.markConversationsUnread(conversations)
        }, "mark as unread")
    }

    override suspend fun starConversations(conversations: List<LocalConversationId>) {

        executeAction({ mailbox ->
            mailbox.starConversations(conversations)
        }, "star conversations")
    }

    override suspend fun unStarConversations(conversations: List<LocalConversationId>) {

        executeAction({ mailbox ->
            mailbox.unstarConversations(conversations)
        }, "unstar conversations")
    }

    override fun observeConversations(conversationIds: List<LocalConversationId>): Flow<List<LocalConversation>> {
        Timber.d("rust-conversation: observeConversations for conversationIds: $conversationIds")

        return combine(
            rustMailbox.observeConversationMailbox(),
            sessionManager.observeCurrentUserSession().filterNotNull()
        ) { mailbox, userSession ->

            try {
                val currentLabelId = mailbox.labelId()
                val conversationList = mutableListOf<LocalConversation>()

                conversationIds.forEach { conversationId ->
                    userSession.conversationWithIdAndContext(conversationId, currentLabelId)?.let { conversation ->
                        conversationList.add(conversation)
                    }
                }

                conversationList
            } catch (e: MailboxException) {
                Timber.e(e, "rust-conversation: Failed to observe conversations for conversationIds: $conversationIds")
                emptyList()
            }
        }
    }

    override suspend fun getConversation(conversationId: LocalConversationId): LocalConversation? {
        return try {
            sessionManager.observeCurrentUserSession()
                .mapLatest { userSession ->
                    userSession?.conversationWithIdWithAllMailContext(conversationId)
                }
                .firstOrNull()
        } catch (e: MailboxException) {
            Timber.e(e, "rust-conversation: failed to get conversation for conversationId: $conversationId")
            null
        }
    }

    override suspend fun relabel(
        conversationIds: List<LocalConversationId>,
        labelsToBeRemoved: List<LocalLabelId>,
        labelsToBeAdded: List<LocalLabelId>
    ) {

        executeAction({ mailbox ->
            labelsToBeRemoved.forEach { localLabelId ->
                mailbox.unlabelConversations(localLabelId, conversationIds)
            }

            labelsToBeAdded.forEach { localLabelId ->
                mailbox.labelConversations(localLabelId, conversationIds)
            }
        }, "relabel conversations")
    }

    override suspend fun moveConversationsWithRemoteId(
        conversationIds: List<LocalConversationId>,
        toRemoteLabelId: LabelId
    ) {
        try {
            rustMailbox.observeConversationMailbox()
                .mapLatest { mailbox ->
                    mailbox.moveConversationsWithRemoteId(toRemoteLabelId, conversationIds)
                    executePendingActions()
                }
                .launchIn(coroutineScope)
        } catch (e: MailboxException) {
            Timber.e(e, "rust-conversation: Failed to move conversations")
        }
    }

    // ET - Missing Implementation. This function requires Rust settings integration
    override fun getSenderImage(address: String, bimi: String?): ByteArray? = null

    override suspend fun moveConversations(conversationIds: List<LocalConversationId>, toLabelId: LocalLabelId) {

        executeAction({ mailbox ->
            mailbox.moveConversations(toLabelId, conversationIds)
        }, "move conversations")
    }

    override fun disconnect() {
        rustConversationQuery.disconnect()
    }

    private suspend fun executeAction(action: (Mailbox) -> Unit, actionName: String) {
        val mailbox = rustMailbox.observeConversationMailbox().firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-conversation: Failed to delete conversations, null mailbox")
            return
        }

        try {
            action(mailbox)
            executePendingActions()
        } catch (e: MailboxException) {
            Timber.e(e, "rust-conversation: Failed to perform $actionName")
        }
    }

    private fun executePendingActions() {
        sessionManager.observeCurrentUserSession()
            .mapLatest { session ->
                session?.executePendingActions()
            }
            .launchIn(coroutineScope)
    }
}
