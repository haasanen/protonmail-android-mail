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

package ch.protonmail.android.mailcontact.domain.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcontact.domain.model.ContactCard
import ch.protonmail.android.mailcontact.domain.model.ContactDetailCard
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.model.DeviceContactsWithSignature
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

interface ContactRepository {

    /**
     * Observe all [GroupedContacts] from [userId].
     */
    suspend fun observeAllGroupedContacts(userId: UserId): Flow<Either<GetContactError, List<GroupedContacts>>>

    /**
     * Observe all [ContactMetadata] from [userId].
     */
    suspend fun observeAllContacts(
        userId: UserId,
        refresh: Boolean = false
    ): Flow<Either<GetContactError, List<ContactMetadata>>>

    /**
     * Create a [Contact] for [userId], using [contactCards].
     */
    suspend fun createContact(userId: UserId, contactCards: List<ContactCard>)

    /**
     * Delete contact from [userId] by [contactId].
     */
    suspend fun deleteContact(userId: UserId, contactId: ContactId): Either<DataError, Unit>

    /**
     * Update contact from [userId], by [contactId], using [contactCards].
     */
    suspend fun updateContact(
        userId: UserId,
        contactId: ContactId,
        contactCards: List<ContactCard>
    )

    suspend fun getContactSuggestions(
        userId: UserId,
        deviceContacts: DeviceContactsWithSignature,
        query: ContactSuggestionQuery
    ): Either<DataError, List<ContactMetadata>>

    suspend fun preloadContactSuggestions(
        userId: UserId,
        deviceContacts: DeviceContactsWithSignature
    ): Either<DataError, Unit>

    suspend fun getContactDetails(userId: UserId, contactId: ContactId): Either<DataError, ContactDetailCard>
}
