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

package ch.protonmail.android.mailcontact.data.local

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.data.mapper.LocalContactGroupId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcontact.data.mapper.ContactGroupItemMapper
import ch.protonmail.android.mailcontact.data.usecase.RustGetContactGroupDetails
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustContactGroupDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val rustGetContactGroupDetails: RustGetContactGroupDetails,
    private val contactGroupItemMapper: ContactGroupItemMapper
) : RustContactGroupDataSource {

    override suspend fun getContactGroupDetails(
        userId: UserId,
        contactGroupId: LocalContactGroupId
    ): Either<DataError, ContactMetadata.ContactGroup> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-contact-group: trying to load contact group details with a null session")
            return DataError.Local.NoUserSession.left()
        }

        return rustGetContactGroupDetails(session, contactGroupId)
            .onLeft { Timber.e("rust-contact-group: getting contact group details failed") }
            .map { contactGroupItemMapper.toContactGroup(it) }
    }
}
