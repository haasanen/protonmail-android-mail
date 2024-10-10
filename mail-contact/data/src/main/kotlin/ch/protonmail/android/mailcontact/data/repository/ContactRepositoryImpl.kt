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

package ch.protonmail.android.mailcontact.data.repository

import arrow.core.Either
import ch.protonmail.android.mailcontact.domain.model.ContactCard
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.ContactWithCards
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class ContactRepositoryImpl @Inject constructor() : ContactRepository {

    override fun observeContactWithCards(
        userId: UserId,
        contactId: ContactId,
        refresh: Boolean
    ): Flow<DataResult<ContactWithCards>> {
        TODO("Not yet implemented")
    }

    override suspend fun getContactWithCards(
        userId: UserId,
        contactId: ContactId,
        refresh: Boolean
    ): ContactWithCards {
        TODO("Not yet implemented")
    }

    override fun observeAllContacts(
        userId: UserId,
        refresh: Boolean
    ): Flow<Either<GetContactError, List<ContactMetadata>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllContacts(userId: UserId, refresh: Boolean): List<ContactMetadata> {
        TODO("Not yet implemented")
    }

    override fun observeAllContactEmails(userId: UserId, refresh: Boolean): Flow<DataResult<List<ContactEmail>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllContactEmails(userId: UserId, refresh: Boolean): List<ContactEmail> {
        TODO("Not yet implemented")
    }

    override suspend fun createContact(userId: UserId, contactCards: List<ContactCard>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteContacts(userId: UserId, contactIds: List<ContactId>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateContact(
        userId: UserId,
        contactId: ContactId,
        contactCards: List<ContactCard>
    ) {
        TODO("Not yet implemented")
    }
}
