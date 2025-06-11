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

import android.os.Build
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import ch.protonmail.android.mailattachments.data.mapper.getCalendarAttachmentCount
import ch.protonmail.android.mailattachments.data.mapper.toAttachmentMetadata
import ch.protonmail.android.mailattachments.domain.model.AttachmentCount
import ch.protonmail.android.mailcommon.data.mapper.LocalAddressId
import ch.protonmail.android.mailcommon.data.mapper.LocalAttachmentDisposition
import ch.protonmail.android.mailcommon.data.mapper.LocalAvatarInformation
import ch.protonmail.android.mailcommon.data.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBanner
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerAutoDelete
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerBlockedSender
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerEmbeddedImages
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerExpiry
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerPhishingAttempt
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerRemoteContent
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerScheduledSend
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerSnoozed
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerSpam
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageBannerUnsubscribeNewsletter
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.data.mapper.LocalMimeType
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toExclusiveLocation
import ch.protonmail.android.maillabel.data.mapper.toLabel
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBanner
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageTheme
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailmessage.domain.model.RemoteMessageId
import me.proton.core.user.domain.entity.AddressId
import timber.log.Timber
import uniffi.proton_mail_uniffi.BodyOutput
import uniffi.proton_mail_uniffi.MailTheme
import uniffi.proton_mail_uniffi.MessageRecipient
import uniffi.proton_mail_uniffi.MessageSender
import uniffi.proton_mail_uniffi.ThemeOpts
import uniffi.proton_mail_uniffi.TransformOpts
import kotlin.time.Instant
import ch.protonmail.android.mailcommon.data.mapper.RemoteMessageId as RustRemoteMessageId

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
        attachmentCount = AttachmentCount(
            calendar = this.attachmentsMetadata.getCalendarAttachmentCount()
        ),
        attachments = attachmentsMetadata.filter { it.disposition == LocalAttachmentDisposition.ATTACHMENT }
            .map { it.toAttachmentMetadata() },
        customLabels = customLabels.map { it.toLabel() },
        avatarInformation = this.avatar.toAvatarInformation(),
        exclusiveLocation = this.exclusiveLocation.toExclusiveLocation(),
        isDraft = this.isDraft
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

fun BodyOutput.toMessageBody(messageId: MessageId, mimeType: LocalMimeType) = MessageBody(
    messageId = messageId,
    body = this.body,
    hasQuotedText = this.hadBlockquote,
    banners = this.bodyBanners.map { it.toMessageBanner() },
    mimeType = mimeType.toAndroidMimeType(),
    transformations = this.transformOpts.toMessageBodyTransformations()
)

fun LocalConversationMessages.toConversationMessagesWithMessageToOpen(): Either<DataError, ConversationMessages> {
    val messages = messages.toNonEmptyListOrNull()?.map { it.toMessage() }
        ?: return DataError.Local.NoDataCached.left()

    return ConversationMessages(
        messages = messages,
        messageIdToOpen = messageIdToOpen.toMessageId()
    ).right()
}

fun RemoteMessageId.toRemoteMessageId(): RustRemoteMessageId = RustRemoteMessageId(this.id)
fun RustRemoteMessageId.toRemoteMessageId(): RemoteMessageId = RemoteMessageId(this.value)

fun TransformOpts.toMessageBodyTransformations(): MessageBodyTransformations {
    return MessageBodyTransformations(
        showQuotedText = this.showBlockQuote,
        hideEmbeddedImages = this.hideEmbeddedImages,
        hideRemoteContent = this.hideRemoteImages,
        messageThemeOptions = this.theme?.toMessageThemeOptions()
    )
}

fun MessageThemeOptions.toLocalThemeOptions(): ThemeOpts = ThemeOpts(
    currentTheme = currentTheme.toMailTheme(),
    themeOverride = themeOverride?.toMailTheme(),
    supportsDarkModeViaMediaQuery = if (themeOverride == MessageTheme.Light) {
        // If the themeOverride is Light and the html style contains media query for dark mode,
        // then we cannot tell WebView to render in light mode, so we set this to false.
        false
    } else {
        // Media query for dark mode is supported on Android 10 and above.
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
)

fun ThemeOpts.toMessageThemeOptions(): MessageThemeOptions {
    return MessageThemeOptions(
        currentTheme = currentTheme.toMessageTheme(),
        themeOverride = themeOverride?.toMessageTheme()
    )
}

fun MessageTheme.toMailTheme(): MailTheme {
    return when (this) {
        MessageTheme.Light -> MailTheme.LIGHT_MODE
        MessageTheme.Dark -> MailTheme.DARK_MODE
    }
}

fun MailTheme.toMessageTheme(): MessageTheme {
    return when (this) {
        MailTheme.LIGHT_MODE -> MessageTheme.Light
        MailTheme.DARK_MODE -> MessageTheme.Dark
    }
}

private fun LocalMessageBanner.toMessageBanner(): MessageBanner {
    fun ULong.toInstant() = Instant.fromEpochSeconds(this.toLong())

    return when (this) {
        is LocalMessageBannerAutoDelete -> MessageBanner.AutoDelete(timestamp.toInstant())
        is LocalMessageBannerBlockedSender -> MessageBanner.BlockedSender
        is LocalMessageBannerEmbeddedImages -> MessageBanner.EmbeddedImages
        is LocalMessageBannerExpiry -> MessageBanner.Expiry(timestamp.toInstant())
        is LocalMessageBannerPhishingAttempt -> MessageBanner.PhishingAttempt
        is LocalMessageBannerRemoteContent -> MessageBanner.RemoteContent
        is LocalMessageBannerScheduledSend -> MessageBanner.ScheduledSend(timestamp.toInstant())
        is LocalMessageBannerSnoozed -> MessageBanner.Snoozed(timestamp.toInstant())
        is LocalMessageBannerSpam -> MessageBanner.Spam
        is LocalMessageBannerUnsubscribeNewsletter -> MessageBanner.UnsubscribeNewsletter
    }
}
