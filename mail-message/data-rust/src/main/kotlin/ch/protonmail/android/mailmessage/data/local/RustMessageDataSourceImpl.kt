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

package ch.protonmail.android.mailmessage.data.local

import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_common.LocalMessageId
import uniffi.proton_mail_common.LocalMessageMetadata
import uniffi.proton_mail_uniffi.DecryptedMessageBody
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.MailboxException
import javax.inject.Inject

class RustMessageDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val rustMailbox: RustMailbox,
    private val rustMessageQuery: RustMessageQuery,
    private val rustConversationMessageQuery: RustConversationMessageQuery
) : RustMessageDataSource {

    override suspend fun getMessage(userId: UserId, messageId: LocalMessageId): LocalMessageMetadata? {
        return try {
            val userSession = userSessionRepository.getUserSession(userId)
            userSession?.messageMetadata(messageId)
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to get message")
            null
        }
    }

    override suspend fun getMessageBody(userId: UserId, messageId: LocalMessageId): DecryptedMessageBody? {
        return try {
            rustMailbox.observeMessageMailbox()
                .mapLatest { mailbox ->
                    mailbox.messageBody(messageId)
                }
                .firstOrNull()
        } catch (e: MailboxException) {
            Timber.e(e, "rust-message: Failed to get message body")
            null
        }
    }

    override suspend fun getMessages(userId: UserId, labelId: LocalLabelId): List<LocalMessageMetadata> {
        Timber.d("rust-message: getMessages for labelId: $labelId")
        return rustMessageQuery.observeMessages(userId, labelId)
            .mapLatest { messageList -> messageList }
            .first()
    }

    override suspend fun markRead(userId: UserId, messages: List<LocalMessageId>) {
        throw UnsupportedOperationException("rust-message: markRead has not been implemented by Rust")
    }

    override suspend fun markUnread(userId: UserId, messages: List<LocalMessageId>) {
        throw UnsupportedOperationException("rust-message: markUnread has not been implemented by Rust")
    }

    override fun observeConversationMessages(
        userId: UserId,
        conversationId: LocalConversationId
    ): Flow<LocalConversationMessages> {
        return rustConversationMessageQuery.observeConversationMessages(
            userId,
            conversationId
        )
    }

    override fun disconnect() {
        rustMessageQuery.disconnect()
    }
}
