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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import ch.protonmail.android.mailcommon.datarust.mapper.LocalAddressId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalAttachmentId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalAttachmentMetadata
import ch.protonmail.android.mailcommon.datarust.mapper.LocalAvatarInformation
import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMimeType
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMimeTypeCategory
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toExclusiveLocation
import ch.protonmail.android.maillabel.data.mapper.toLabel
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailmessage.domain.model.AttachmentCount
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.model.MimeTypeCategory
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.domain.model.Recipient
import me.proton.core.user.domain.entity.AddressId
import timber.log.Timber
import uniffi.proton_mail_common.BodyOutput
import uniffi.proton_mail_uniffi.Disposition
import uniffi.proton_mail_uniffi.MessageRecipient
import uniffi.proton_mail_uniffi.MessageSender


fun LocalAvatarInformation.toAvatarInformation(): AvatarInformation {
    return AvatarInformation(
        initials = this.text,
        color = this.color
    )
}

fun ConversationId.toLocalConversationId(): LocalConversationId = LocalConversationId(this.id.toULong())

fun MessageId.toLocalMessageId(): LocalMessageId = LocalMessageId(this.id.toULong())

fun LocalMessageId.toMessageId(): MessageId = MessageId(this.value.toString())

fun LocalConversationId.toConversationId(): ConversationId = ConversationId(this.value.toString())

fun LocalAddressId.toAddressId(): AddressId = AddressId(this.value.toString())

fun LocalAttachmentId.toAttachmentId(): AttachmentId = AttachmentId(this.value.toString())

fun LocalMessageMetadata.toMessage(): Message {
    return Message(
        messageId = this.id.toMessageId(),
        conversationId = this.conversationId.toConversationId(),
        time = this.time.toLong(),
        size = this.size.toLong(),
        order = this.displayOrder.toLong(),
        subject = this.subject,
        isUnread = this.unread,
        sender = this.sender.toParticipant(),
        toList = this.toList.map { it.toRecipient() },
        ccList = this.ccList.map { it.toRecipient() },
        bccList = this.bccList.map { it.toRecipient() },
        expirationTime = this.expirationTime.toLong(),
        isReplied = this.isReplied,
        isRepliedAll = this.isRepliedAll,
        isForwarded = this.isForwarded,
        isStarred = this.starred,
        addressId = this.addressId.toAddressId(),
        numAttachments = this.numAttachments.toInt(),
        flags = this.flags.value.toLong(),
        attachmentCount = AttachmentCount(this.numAttachments.toInt()),
        attachments = attachmentsMetadata.filter { it.disposition == Disposition.ATTACHMENT }
            .map { it.toAttachmentMetadata() },
        customLabels = customLabels.map { it.toLabel() },
        avatarInformation = this.avatar.toAvatarInformation(),
        exclusiveLocation = this.exclusiveLocation.toExclusiveLocation()
    )
}

fun LocalAttachmentMetadata.toAttachmentMetadata(): AttachmentMetadata {
    return AttachmentMetadata(
        id = this.id.toAttachmentId(),
        name = this.name,
        size = this.size.toLong(),
        mimeTypeCategory = this.mimeType.category.toMimeTypeCategory()
    )
}

fun MessageSender.toParticipant(): Participant {
    return Participant(
        address = this.address, name = this.name, isProton = this.isProton, bimiSelector = this.bimiSelector
    )
}

fun MessageRecipient.toParticipant(): Participant =
    Participant(address = this.address, name = this.name, isProton = this.isProton)

fun MessageRecipient.toRecipient(): Recipient {
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

fun BodyOutput.toMessageBody(messageId: MessageId, mimeType: LocalMimeType): MessageBody {
    return MessageBody(
        messageId = messageId,
        body = this.body,
        header = "",
        attachments = emptyList(),
        mimeType = mimeType.toAndroidMimeType(),
        spamScore = "",
        replyTo = Recipient("", ""),
        replyTos = emptyList(),
        unsubscribeMethods = null
    )
}

fun LocalConversationMessages.toConversationMessagesWithMessageToOpen(): Either<DataError, ConversationMessages> {
    val messages = messages.toNonEmptyListOrNull()?.map { it.toMessage() }
        ?: return DataError.Local.NoDataCached.left()

    return ConversationMessages(
        messages = messages,
        messageIdToOpen = messageIdToOpen.toMessageId()
    ).right()
}

fun LocalMimeTypeCategory.toMimeTypeCategory(): MimeTypeCategory = when (this) {
    LocalMimeTypeCategory.AUDIO -> MimeTypeCategory.Audio
    LocalMimeTypeCategory.CALENDAR -> MimeTypeCategory.Calendar
    LocalMimeTypeCategory.CODE -> MimeTypeCategory.Code
    LocalMimeTypeCategory.COMPRESSED -> MimeTypeCategory.Compressed
    LocalMimeTypeCategory.DEFAULT -> MimeTypeCategory.Default
    LocalMimeTypeCategory.EXCEL -> MimeTypeCategory.Excel
    LocalMimeTypeCategory.FONT -> MimeTypeCategory.Font
    LocalMimeTypeCategory.IMAGE -> MimeTypeCategory.Image
    LocalMimeTypeCategory.KEY -> MimeTypeCategory.Key
    LocalMimeTypeCategory.KEYNOTE -> MimeTypeCategory.Keynote
    LocalMimeTypeCategory.NUMBERS -> MimeTypeCategory.Numbers
    LocalMimeTypeCategory.PAGES -> MimeTypeCategory.Pages
    LocalMimeTypeCategory.PDF -> MimeTypeCategory.Pdf
    LocalMimeTypeCategory.POWERPOINT -> MimeTypeCategory.Powerpoint
    LocalMimeTypeCategory.TEXT -> MimeTypeCategory.Text
    LocalMimeTypeCategory.VIDEO -> MimeTypeCategory.Video
    LocalMimeTypeCategory.WORD -> MimeTypeCategory.Word
    LocalMimeTypeCategory.UNKNOWN -> MimeTypeCategory.Unknown
}
