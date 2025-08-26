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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import kotlinx.coroutines.flow.Flow
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import me.proton.core.domain.entity.UserId
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

class ObserveContactGroup @Inject constructor(
    private val contactRepository: ContactRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        contactGroupId: ContactGroupId
    ): Flow<Either<GetContactGroupError, ContactMetadata.ContactGroup>> {
        return contactRepository.observeAllContacts(userId).transformLatest {
            it.onLeft {
                emit(GetContactGroupError.GetContactsError.left())
            }
            it.onRight { contacts ->
                val contactGroup = contacts
                    .filterIsInstance<ContactMetadata.ContactGroup>()
                    .find { contact ->
                        contact.id == contactGroupId
                    }

                emit(contactGroup?.right() ?: GetContactGroupError.ContactGroupNotFound.left())
            }
        }
    }
}

sealed interface GetContactGroupError {
    object GetContactsError : GetContactGroupError
    object ContactGroupNotFound : GetContactGroupError
}
