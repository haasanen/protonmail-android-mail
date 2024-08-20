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
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.domain.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.domain.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.domain.mapper.LocalMimeType
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
import timber.log.Timber
import uniffi.proton_mail_uniffi.BodyOutput
import uniffi.proton_mail_uniffi.MessageAddress

fun ConversationId.toLocalConversationId(): LocalConversationId = this.id.toULong()

fun MessageId.toLocalMessageId(): LocalMessageId = this.id.toULong()

fun LocalMessageId.toMessageId(): MessageId = MessageId(this.toString())

fun LocalMessageMetadata.toMessage(): Message {
    return Message(
        userId = FAKE_USER_ID,
        messageId = MessageId(this.localId.toString()),
        conversationId = ConversationId(this.localConversationId.toString()),
        time = this.time.toLong(),
        size = this.size.toLong(),
        order = this.displayOrder.toLong(),
        labelIds = this.customLabels.map { LabelId(it.localId.toString()) },
        subject = this.subject,
        unread = this.unread,
        sender = this.sender.toParticipant(),
        toList = this.toList.value.map { it.toRecipient() },
        ccList = this.ccList.value.map { it.toRecipient() },
        bccList = this.bccList.value.map { it.toRecipient() },
        expirationTime = this.expirationTime.toLong(),
        isReplied = this.isReplied,
        isRepliedAll = this.isRepliedAll,
        isForwarded = this.isForwarded,
        addressId = AddressId(this.addressId.toString()),
        numAttachments = this.numAttachments.toInt(),
        flags = this.flags.value.toLong(),
        attachmentCount = AttachmentCount(this.numAttachments.toInt()),
        isStarred = this.starred,
        externalId = null
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

fun LocalMimeType.toAndroidMimeType(): MimeType {
    return when (this) {
        LocalMimeType.MESSAGE_RFC822 -> MimeType.PlainText
        LocalMimeType.TEXT_PLAIN -> MimeType.PlainText
        LocalMimeType.TEXT_HTML -> MimeType.Html
        LocalMimeType.MULTIPART_MIXED -> MimeType.MultipartMixed
        LocalMimeType.MULTIPART_RELATED -> MimeType.MultipartMixed
        LocalMimeType.APPLICATION_JSON,
        LocalMimeType.APPLICATION_PDF -> {
            Timber.w("rust-message-mapper: Received unsupported mime type $this. Fallback to plaintext")
            MimeType.PlainText
        }
    }
}

@MissingRustApi
// Mime type hardcoded as not coming through as part of decrypted message anymore...
fun BodyOutput.toMessageBody(messageId: MessageId): MessageBody {
    return MessageBody(
        userId = FAKE_USER_ID,
        messageId = messageId,
        body = this.body,
        header = "",
        attachments = emptyList(),
        mimeType = LocalMimeType.TEXT_PLAIN.toAndroidMimeType(),
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
