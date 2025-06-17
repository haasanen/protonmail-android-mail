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

package ch.protonmail.android.mailmessage.data.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.local.MessageBodyDataSource
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class MessageBodyRepositoryImpl @Inject constructor(
    private val messageBodyDataSource: MessageBodyDataSource
) : MessageBodyRepository {

    override suspend fun getMessageBody(
        userId: UserId,
        messageId: MessageId,
        transformations: MessageBodyTransformations
    ): Either<DataError, MessageBody> =
        messageBodyDataSource.getMessageBody(userId, messageId.toLocalMessageId(), transformations)

    override suspend fun getEmbeddedImage(
        userId: UserId,
        messageId: MessageId,
        contentId: String
    ): Either<DataError, EmbeddedImage> =
        messageBodyDataSource.getEmbeddedImage(userId, messageId.toLocalMessageId(), contentId)
            .map { localEmbeddedImage ->
                Timber.d("RustMessage: Loaded embedded image: $contentId; mime ${localEmbeddedImage.mime};")
                EmbeddedImage(localEmbeddedImage.data, localEmbeddedImage.mime)
            }

}
