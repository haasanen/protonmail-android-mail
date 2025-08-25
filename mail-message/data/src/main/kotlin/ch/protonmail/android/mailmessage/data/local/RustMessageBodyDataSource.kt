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

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import ch.protonmail.android.mailcommon.data.mapper.LocalAttachmentData
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.local.RustMailboxFactory
import ch.protonmail.android.mailmessage.data.mapper.toLocalThemeOptions
import ch.protonmail.android.mailmessage.data.mapper.toMessageBody
import ch.protonmail.android.mailmessage.data.mapper.toMessageId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.TransformOpts
import javax.inject.Inject

class RustMessageBodyDataSource @Inject constructor(
    private val createRustMessageBodyAccessor: CreateRustMessageBodyAccessor,
    private val rustMailboxFactory: RustMailboxFactory,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : MessageBodyDataSource {

    override suspend fun getMessageBody(
        userId: UserId,
        messageId: LocalMessageId,
        transformations: MessageBodyTransformations
    ): Either<DataError, MessageBody> = withContext(ioDispatcher) {
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to get the body and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull()
            ?: return@withContext DataError.Local.NoDataCached.left()

        return@withContext createRustMessageBodyAccessor(mailbox, messageId)
            .onLeft { Timber.e("rust-message: Failed to get message body $it") }
            .flatMap { decryptedMessage ->
                val transformOptions = TransformOpts(
                    showBlockQuote = transformations.showQuotedText,
                    hideRemoteImages = transformations.hideRemoteContent,
                    hideEmbeddedImages = transformations.hideEmbeddedImages,
                    theme = transformations.messageThemeOptions?.toLocalThemeOptions()
                )

                decryptedMessage.body(transformOptions).map { decryptedBody ->
                    decryptedBody.toMessageBody(messageId.toMessageId(), decryptedMessage.mimeType())
                }
            }
    }

    override suspend fun loadImage(
        userId: UserId,
        messageId: LocalMessageId,
        url: String
    ): Either<DataError, LocalAttachmentData> = withContext(ioDispatcher) {
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to get the body and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull()
            ?: return@withContext DataError.Local.NoDataCached.left()

        return@withContext createRustMessageBodyAccessor(mailbox, messageId)
            .onLeft { Timber.e("rust-message: Failed to build message body accessor $it") }
            .flatMap { decryptedMessage ->
                decryptedMessage.loadImage(url)
            }
    }

    override suspend fun unsubscribeFromNewsletter(userId: UserId, messageId: LocalMessageId): Either<DataError, Unit> =
        withContext(ioDispatcher) {
            // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
            // the current labelId is not needed to get the body and is planned to be dropped on this API
            val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull()
                ?: return@withContext DataError.Local.NoDataCached.left()

            return@withContext createRustMessageBodyAccessor(mailbox, messageId)
                .onLeft { Timber.e("rust-message: Failed to build message body accessor $it") }
                .flatMap { decryptedMessage ->
                    decryptedMessage.unsubscribeFromNewsletter()
                }
        }
}
