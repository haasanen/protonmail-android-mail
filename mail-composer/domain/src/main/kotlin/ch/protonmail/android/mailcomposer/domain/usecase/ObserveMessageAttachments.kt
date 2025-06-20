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

package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.Either
import ch.protonmail.android.mailattachments.domain.model.AttachmentDisposition
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveMessageAttachments @Inject constructor(
    private val attachmentRepository: AttachmentRepository
) {

    suspend operator fun invoke(): Flow<Either<DataError, List<AttachmentMetadataWithState>>> =
        attachmentRepository.observeAttachments().map { either ->
            either.map { attachments ->
                attachments.filter { it.hasAttachmentDisposition() }
            }
        }

    private fun AttachmentMetadataWithState.hasAttachmentDisposition() =
        this.attachmentMetadata.disposition == AttachmentDisposition.Attachment
}
