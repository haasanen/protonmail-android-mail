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

package ch.protonmail.android.mailcomposer.domain.repository

import android.net.Uri
import arrow.core.Either
import ch.protonmail.android.mailattachments.domain.model.AddAttachmentError
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailattachments.domain.model.ConvertAttachmentError
import ch.protonmail.android.mailcommon.domain.model.DataError
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {

    suspend fun observeAttachments(): Flow<Either<DataError, List<AttachmentMetadataWithState>>>

    suspend fun deleteAttachment(attachmentId: AttachmentId): Either<DataError, Unit>

    suspend fun addAttachment(fileUri: Uri): Either<AddAttachmentError, Unit>

    suspend fun deleteInlineAttachment(contentId: String): Either<DataError, Unit>
    suspend fun addInlineAttachment(fileUri: Uri): Either<AddAttachmentError, String>

    suspend fun convertToAttachment(cid: String): Either<ConvertAttachmentError, Unit>
}
