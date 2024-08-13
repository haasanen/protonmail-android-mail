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

package ch.protonmail.android.mailmessage.data.mapper

import arrow.core.toNonEmptyListOrNull
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailmessage.domain.model.AttachmentCount
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.domain.model.Recipient
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.user.domain.entity.AddressId
import uniffi.proton_api_mail.MessageAddress
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_api_mail.MimeType as RustMimeType
import uniffi.proton_mail_common.LocalMessageId
import uniffi.proton_mail_common.LocalMessageMetadata
import uniffi.proton_mail_uniffi.DecryptedMessageBody

fun ConversationId.toLocalConversationId(): LocalConversationId = this.id.toULong()

fun MessageId.toLocalMessageId(): LocalMessageId = this.id.toULong()

fun LocalMessageId.toMessageId(): MessageId = MessageId(this.toString())

fun LocalMessageMetadata.toMessage(): Message {
    return Message(
        userId = FAKE_USER_ID,
        messageId = MessageId(this.id.toString()),
        conversationId = ConversationId(this.conversationId.toString()),
        time = this.time.toLong(),
        size = this.size.toLong(),
        order = this.order.toLong(),
        labelIds = this.labels?.map { LabelId(it.id.toString()) } ?: emptyList(),
        subject = this.subject,
        unread = this.unread,
        sender = this.sender.toParticipant(),
        toList = this.to.map { it.toRecipient() },
        ccList = this.cc.map { it.toRecipient() },
        bccList = this.bcc.map { it.toRecipient() },
        expirationTime = this.expirationTime.toLong(),
        isReplied = this.isReplied,
        isRepliedAll = this.isRepliedAll,
        isForwarded = this.isForwarded,
        addressId = AddressId(this.addressId),
        externalId = this.externalId,
        numAttachments = this.numAttachments.toInt(),
        flags = this.flags.toLong(),
        attachmentCount = AttachmentCount(this.attachments?.size ?: 0),
        isStarred = this.starred
    )
}

fun MessageAddress.toParticipant(): Participant {
    return Participant(
        address = this.address, name = this.name, isProton = this.isProton, bimiSelector = this.bimiSelector
    )
}

fun MessageAddress.toRecipient(): Recipient {
    return Recipient(
        address = this.address,
        name = this.name,
        isProton = this.isProton
    )
}

fun RustMimeType.toAndroidMimeType(): MimeType {
    return when (this) {
        RustMimeType.MESSAGE_RFC822 -> MimeType.PlainText
        RustMimeType.TEXT_PLAIN -> MimeType.PlainText
        RustMimeType.TEXT_HTML -> MimeType.Html
        RustMimeType.MULTIPART_MIXED -> MimeType.MultipartMixed
        RustMimeType.MULTIPART_RELATED -> MimeType.MultipartMixed
    }
}

fun DecryptedMessageBody.toMessageBody(messageId: MessageId): MessageBody {
    return MessageBody(
        userId = FAKE_USER_ID,
        messageId = messageId,
        body = this.body(),
        header = "",
        attachments = emptyList(),
        mimeType = this.mimeType().toAndroidMimeType(),
        spamScore = "",
        replyTo = Recipient("", ""),
        replyTos = emptyList(),
        unsubscribeMethods = null
    )
}

fun LocalConversationMessages.toConversationMessagesWithMessageToOpen(): ConversationMessages? {
    return messages.toNonEmptyListOrNull()?.map { it.toMessage() }?.let { messageList ->
        ConversationMessages(
            messages = messageList,
            messageIdToOpen = messageIdToOpen.toMessageId()
        )
    }
}
