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

import android.net.Uri
import arrow.core.Either
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailattachments.domain.model.AttachmentError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import javax.inject.Inject

class AddStandardAttachment @Inject constructor(
    private val attachmentRepository: AttachmentRepository
) {

    suspend operator fun invoke(fileUri: Uri): Either<AttachmentAddError, Unit> =
        attachmentRepository.addAttachment(fileUri).mapLeft {
            when (it) {
                AttachmentError.AttachmentTooLarge -> AttachmentAddError.AttachmentTooLarge
                AttachmentError.EncryptionError -> AttachmentAddError.EncryptionError
                AttachmentError.InvalidDraftMessage -> AttachmentAddError.InvalidDraftMessage
                AttachmentError.TooManyAttachments -> AttachmentAddError.TooManyAttachments
                AttachmentError.InvalidState -> AttachmentAddError.RetryUpload
                is AttachmentError.Other -> AttachmentAddError.Unknown
            }
        }

}
