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

package ch.protonmail.android.maildetail.presentation.mapper

import ch.protonmail.android.mailattachments.domain.model.isCalendarAttachment
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetDecryptedMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.MessageBanner
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentGroupUiModelMapper
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyUiModel
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.MimeTypeUiModel
import ch.protonmail.android.mailmessage.presentation.model.ViewModePreference
import ch.protonmail.android.mailmessage.presentation.usecase.InjectCssIntoDecryptedMessageBody
import javax.inject.Inject

class MessageBodyUiModelMapper @Inject constructor(
    private val attachmentGroupUiModelMapper: AttachmentGroupUiModelMapper,
    private val injectCssIntoDecryptedMessageBody: InjectCssIntoDecryptedMessageBody
) {

    suspend fun toUiModel(
        decryptedMessageBody: DecryptedMessageBody,
        existingMessageBodyUiModel: MessageBodyUiModel? = null
    ): MessageBodyUiModel {
        val decryptedMessageBodyWithType = MessageBodyWithType(
            decryptedMessageBody.value,
            decryptedMessageBody.mimeType.toMimeTypeUiModel()
        )
        val messageBody = decryptedMessageBodyWithType.messageBody

        val hasRemoteContentBlocked = decryptedMessageBody.banners.contains(MessageBanner.RemoteContent)
        val hasEmbeddedImagesBlocked = decryptedMessageBody.banners.contains(MessageBanner.EmbeddedImages)
        val hasExpandCollapseButton = existingMessageBodyUiModel?.shouldShowExpandCollapseButton == true ||
            decryptedMessageBody.hasQuotedText

        val messageBodyWithType = MessageBodyWithType(
            messageBody,
            decryptedMessageBody.mimeType.toMimeTypeUiModel()
        )

        val originalMessageBody = injectCssIntoDecryptedMessageBody(messageBodyWithType)
        val viewModePreference = existingMessageBodyUiModel?.viewModePreference ?: ViewModePreference.ThemeDefault

        return MessageBodyUiModel(
            messageId = decryptedMessageBody.messageId,
            messageBody = originalMessageBody,
            mimeType = decryptedMessageBody.mimeType.toMimeTypeUiModel(),
            shouldShowEmbeddedImagesBanner = hasEmbeddedImagesBlocked,
            shouldShowRemoteContentBanner = hasRemoteContentBlocked,
            shouldShowExpandCollapseButton = hasExpandCollapseButton,
            shouldShowOpenInProtonCalendar = decryptedMessageBody.attachments.any { it.isCalendarAttachment() },
            attachments = if (decryptedMessageBody.attachments.isNotEmpty()) {
                attachmentGroupUiModelMapper.toUiModel(
                    decryptedMessageBody.attachments
                )
            } else null,
            viewModePreference = viewModePreference
        )
    }

    fun toUiModel(decryptionError: GetDecryptedMessageBodyError.Decryption) = MessageBodyUiModel(
        messageId = decryptionError.messageId,
        messageBody = decryptionError.encryptedMessageBody,
        mimeType = MimeTypeUiModel.PlainText,
        shouldShowEmbeddedImagesBanner = false,
        shouldShowRemoteContentBanner = false,
        shouldShowExpandCollapseButton = false,
        shouldShowOpenInProtonCalendar = false,
        attachments = null,
        viewModePreference = ViewModePreference.ThemeDefault
    )

    private fun MimeType.toMimeTypeUiModel() = when (this) {
        MimeType.PlainText -> MimeTypeUiModel.PlainText
        MimeType.Html, MimeType.MultipartMixed -> MimeTypeUiModel.Html
    }
}
