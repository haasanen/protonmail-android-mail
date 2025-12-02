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

package ch.protonmail.android.testdata.message

import ch.protonmail.android.mailattachments.domain.model.AttachmentDisposition
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.testdata.message.MessageTestData.RAW_MESSAGE_ID
import kotlin.collections.listOf

object MessageBodyTestData {

    const val RAW_ENCRYPTED_MESSAGE_BODY = "This is a raw encrypted message body."

    val messageBody = buildMessageBody()

    val htmlMessageBody = buildMessageBody(
        mimeType = MimeType.Html
    )

    val multipartMixedMessageBody = buildMessageBody(
        mimeType = MimeType.MultipartMixed
    )

    val attachmentsBody = buildMessageBody(
        attachments = listOf(
            AttachmentMetadata(
                AttachmentId("100"),
                ch.protonmail.android.mailattachments.domain.model.AttachmentMimeType(
                    "test",
                    ch.protonmail.android.mailattachments.domain.model.MimeTypeCategory.Audio
                ),
                AttachmentDisposition.Attachment,
                "test attachment",
                100L,
                true
            )
        )
    )

    fun buildMessageBody(
        messageId: MessageId = MessageId(RAW_MESSAGE_ID),
        body: String = RAW_ENCRYPTED_MESSAGE_BODY,
        mimeType: MimeType = MimeType.PlainText,
        attachments: List<AttachmentMetadata> = emptyList()
    ) = MessageBody(
        messageId = messageId,
        body = body,
        hasQuotedText = false,
        mimeType = mimeType,
        banners = emptyList(),
        transformations = MessageBodyTransformations(false, false, false, null),
        attachments = attachments
    )
}
