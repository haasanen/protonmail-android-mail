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

package ch.protonmail.android.mailcontact.domain.usecase

import arrow.core.Either
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.model.DeviceContactsWithSignature
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.mailcontact.domain.repository.DeviceContactsRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetContactSuggestions @Inject constructor(
    private val contactsRepository: ContactRepository,
    private val deviceContactsRepository: DeviceContactsRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        query: ContactSuggestionQuery
    ): Either<DataError, List<ContactMetadata>> {
        val deviceContacts = deviceContactsRepository.getAllContacts(useCacheIfAvailable = true)
            .getOrElse { DeviceContactsWithSignature.Empty }

        return contactsRepository.getContactSuggestions(userId, deviceContacts, query)
    }
}
