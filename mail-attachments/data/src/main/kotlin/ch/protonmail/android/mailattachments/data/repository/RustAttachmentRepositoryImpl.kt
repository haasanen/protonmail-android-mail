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

package ch.protonmail.android.mailattachments.data.repository

import arrow.core.Either
import arrow.core.flatMap
import ch.protonmail.android.mailattachments.data.local.RustAttachmentDataSource
import ch.protonmail.android.mailattachments.data.mapper.DecryptedAttachmentMapper
import ch.protonmail.android.mailattachments.data.mapper.toLocalAttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.DecryptedAttachment
import ch.protonmail.android.mailattachments.domain.repository.AttachmentRepository
import ch.protonmail.android.mailcommon.domain.model.DataError
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    private val rustAttachmentDataSource: RustAttachmentDataSource,
    private val decryptedAttachmentMapper: DecryptedAttachmentMapper
) : AttachmentRepository {

    override suspend fun getAttachment(
        userId: UserId,
        attachmentId: AttachmentId
    ): Either<DataError, DecryptedAttachment> {
        return rustAttachmentDataSource
            .getAttachment(userId, attachmentId.toLocalAttachmentId())
            .flatMap { decryptedAttachmentMapper.toDomainModel(it) }
    }
}
