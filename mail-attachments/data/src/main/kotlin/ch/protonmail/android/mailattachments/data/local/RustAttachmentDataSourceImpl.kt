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

package ch.protonmail.android.mailattachments.data.local

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailattachments.data.usecase.GetRustAttachment
import ch.protonmail.android.mailcommon.data.mapper.LocalAttachmentId
import ch.protonmail.android.mailcommon.data.mapper.LocalDecryptedAttachment
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustAttachmentDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val getRustAttachment: GetRustAttachment
) : RustAttachmentDataSource {

    override suspend fun getAttachment(
        userId: UserId,
        attachmentId: LocalAttachmentId
    ): Either<DataError, LocalDecryptedAttachment> {

        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-attachment: trying to load attachment with a null session")
            return DataError.Local.NoUserSession.left()
        }

        return getRustAttachment(session, attachmentId)
    }
}
