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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetDecryptedMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class GetDecryptedMessageBody @Inject constructor(
    private val messageRepository: MessageRepository,
    private val messageBodyRepository: MessageBodyRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        transformations: MessageBodyTransformations = MessageBodyTransformations.MessageDetailsDefaults
    ): Either<GetDecryptedMessageBodyError, DecryptedMessageBody> = messageRepository.getMessage(userId, messageId)
        .mapLeft { GetDecryptedMessageBodyError.Data(it) }
        .flatMap { messageMetadata ->
            messageBodyRepository.getMessageBody(userId, messageId, transformations)
                .mapLeft { GetDecryptedMessageBodyError.Data(it) }
                .flatMap { messageBody ->
                    val attachments = when {
                        messageBody.mimeType == MimeType.MultipartMixed ->
                            getDecryptedMimeAttachments(userId, messageId, messageMetadata)

                        else -> messageMetadata.attachments
                    }

                    DecryptedMessageBody(
                        messageId = messageId,
                        value = messageBody.body,
                        isUnread = messageMetadata.isUnread,
                        mimeType = messageBody.mimeType,
                        hasQuotedText = messageBody.hasQuotedText,
                        banners = messageBody.banners,
                        attachments = attachments,
                        transformations = messageBody.transformations
                    ).right()
                }
        }

    private suspend fun getDecryptedMimeAttachments(
        userId: UserId,
        messageId: MessageId,
        message: Message
    ): List<AttachmentMetadata> {
        // After the message body is decrypted (through the first messageWithBody call)
        // rust will expose the decrypted mime attachments to the message "attachments" field.
        // This logic is needed to get such up-to-date attachments when opening a MIME message
        return messageRepository.getMessage(userId, messageId)
            .onLeft {
                Timber.w("decrypted-message-body: Failed getting refreshed MIME attachments")
            }
            .getOrNull()
            ?.attachments
            ?: message.attachments
    }
}
