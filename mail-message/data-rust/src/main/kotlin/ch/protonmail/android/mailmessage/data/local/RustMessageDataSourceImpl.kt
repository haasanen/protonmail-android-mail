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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalDecryptedMessage
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.usecase.GetRustSenderImage
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.MailboxException
import javax.inject.Inject

class RustMessageDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val rustMailbox: RustMailbox,
    private val rustMessageQuery: RustMessageQuery,
    private val createRustMessageAccessor: CreateRustMessageAccessor,
    private val createRustMessageBodyAccessor: CreateRustMessageBodyAccessor,
    private val getRustSenderImage: GetRustSenderImage
) : RustMessageDataSource {

    override suspend fun getMessage(userId: UserId, messageId: LocalMessageId): LocalMessageMetadata? {
        return try {
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-message: trying to load message with a null session")
                return null
            }
            createRustMessageAccessor(session, messageId)
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to get message")
            null
        }
    }

    override suspend fun getMessageBody(
        userId: UserId,
        messageId: LocalMessageId,
        labelId: LocalLabelId?
    ): LocalDecryptedMessage? {
        val mailboxFlow = if (labelId != null) {
            rustMailbox.observeMailbox(labelId)
        } else {
            rustMailbox.observeMessageMailbox()
        }

        return try {
            mailboxFlow
                .mapLatest { mailbox ->
                    createRustMessageBodyAccessor(mailbox, messageId)
                }
                .firstOrNull()
        } catch (e: MailboxException) {
            Timber.e(e, "rust-message: Failed to get message body")
            null
        }
    }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata> {
        Timber.d("rust-message: getMessages for pageKey: $pageKey")
        return rustMessageQuery.observeMessages(userId, pageKey)
            .mapLatest { messageList -> messageList }
            .first()
    }

    override suspend fun getSenderImage(
        userId: UserId,
        address: String,
        bimi: String?
    ): String? {
        Timber.d("rust-message: getSenderImage for address: $address")
        return try {
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-message: trying to get sender image with a null session")
                return null
            }
            getRustSenderImage(session, address, bimi)
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to get sender image")
            null
        }
    }

    @MissingRustApi
    override suspend fun markRead(userId: UserId, messages: List<LocalMessageId>) {
        Timber.w("rust-message: markRead has not been implemented by Rust")
    }

    @MissingRustApi
    override suspend fun markUnread(userId: UserId, messages: List<LocalMessageId>) {
        throw UnsupportedOperationException("rust-message: markUnread has not been implemented by Rust")
    }

    override fun disconnect() {
        rustMessageQuery.disconnect()
    }
}
