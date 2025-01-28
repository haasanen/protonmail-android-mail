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

package ch.protonmail.android.maildetail.domain.usecase

import java.io.File
import android.content.Context
import androidx.core.content.FileProvider
import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maildetail.domain.model.OpenAttachmentIntentValues
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.AttachmentRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetAttachmentIntentValues @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val messageRepository: MessageRepository,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Either<DataError, OpenAttachmentIntentValues> = either {
        val messageWithBody = messageRepository.getMessageWithBody(userId, messageId).bind()
        val filePath = attachmentRepository.getAttachment(userId, messageId, attachmentId).bind().dataPath
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(filePath)
        )

        val attachment = messageWithBody.messageBody.attachments.firstOrNull { it.attachmentId == attachmentId }
            ?: raise(DataError.Local.NoDataCached)

        return@either uri?.let {
            OpenAttachmentIntentValues(
                mimeType = attachment.mimeType.mime,
                uri = it
            )
        } ?: raise(DataError.Local.NoDataCached)
    }
}
