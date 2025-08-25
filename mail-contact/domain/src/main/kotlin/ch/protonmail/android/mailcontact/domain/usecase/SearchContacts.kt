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
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.model.toContactItem
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.containsNoCase
import me.proton.core.util.kotlin.takeIfNotBlank
import timber.log.Timber
import javax.inject.Inject

class SearchContacts @Inject constructor(
    private val contactRepository: ContactRepository
) {

    operator fun invoke(userId: UserId, query: String): Flow<Either<GetContactError, List<ContactMetadata>>> =
        contactRepository.observeAllContacts(
            userId
        ).distinctUntilChanged().transformLatest {
            it.onLeft {
                Timber.e("SearchContacts, error observing contacts: $it")
                emit(GetContactError.left())
            }.onRight { contacts ->
                query.trim().takeIfNotBlank()?.run {
                    val searchResult = search(contacts, query)
                    emit(searchResult.right())
                }
            }
        }.distinctUntilChanged()

    private fun search(contacts: List<ContactMetadata>, query: String): List<ContactMetadata> = contacts
        .flatMap { contact ->
            when (contact) {
                is ContactMetadata.Contact -> searchInContact(contact, query)
                is ContactMetadata.ContactGroup -> searchInContactGroup(contact, query)
            }
        }
        .distinctBy {
            when (it) {
                is ContactMetadata.Contact -> "contact-${it.id.id}" // ensure uniqueness by ContactId
                is ContactMetadata.ContactGroup -> "group-${it.id.id}" // ensure uniqueness by GroupId
            }
        }

    private fun searchInContact(contact: ContactMetadata.Contact, query: String): List<ContactMetadata> {
        val nameMatches = contact.name.containsNoCase(query)
        val anyContactEmailMatches = contact.emails.any { it.email.containsNoCase(query) }

        return if (nameMatches || anyContactEmailMatches) {
            listOf(contact)
        } else {
            emptyList()
        }
    }

    private fun searchInContactGroup(contactGroup: ContactMetadata.ContactGroup, query: String): List<ContactMetadata> {
        // member matches as individual Contact items
        val matchingMembers: List<ContactMetadata.Contact> =
            contactGroup.members.mapNotNull { member ->
                if (member.name.containsNoCase(query) || member.email.containsNoCase(query)) {
                    member.toContactItem()
                } else {
                    null
                }
            }

        // if group name matches, include the group in the results too
        val results = mutableListOf<ContactMetadata>()
        if (contactGroup.name.containsNoCase(query)) {
            results += contactGroup
        }

        results += matchingMembers
        return results
    }
}

