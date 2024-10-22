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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.containsNoCase
import me.proton.core.util.kotlin.takeIfNotBlank
import timber.log.Timber
import javax.inject.Inject

class SearchContacts @Inject constructor(
    private val observeContacts: ObserveContacts
) {

    operator fun invoke(
        userId: UserId,
        query: String,
        onlyMatchingContactEmails: Boolean = true
    ): Flow<Either<GetContactError, List<ContactMetadata>>> = observeContacts(
        userId
    ).distinctUntilChanged().transformLatest {
        it.onLeft {
            Timber.e("SearchContacts, error observing contacts: $it")
            emit(GetContactError.left())
        }.onRight { contacts ->
            query.trim().takeIfNotBlank()?.run {
                val searchResult = search(contacts, query, onlyMatchingContactEmails)
                emit(searchResult.right())
            }
        }
    }.distinctUntilChanged()

    private fun search(
        contacts: List<ContactMetadata>,
        query: String,
        onlyMatchingContactEmails: Boolean
    ): List<ContactMetadata> = contacts.mapNotNull { contact ->
        when (contact) {
            is ContactMetadata.Contact -> searchInContact(contact, query, onlyMatchingContactEmails)
            is ContactMetadata.ContactGroup -> searchInContactGroup(contact, query, onlyMatchingContactEmails)
        }
    }

    private fun searchInContact(
        contact: ContactMetadata.Contact,
        query: String,
        onlyMatchingContactEmails: Boolean
    ): ContactMetadata.Contact? {
        return if (contact.name.containsNoCase(query)) {
            contact // Return the full contact if name matches
        } else {
            if (onlyMatchingContactEmails) {
                val matchingContactEmails = contact.emails.filter { it.email.containsNoCase(query) }
                if (matchingContactEmails.isNotEmpty()) {
                    contact.copy(emails = matchingContactEmails)
                } else null
            } else {
                if (contact.emails.any { it.email.containsNoCase(query) }) {
                    contact
                } else null
            }
        }
    }

    private fun searchInContactGroup(
        contactGroup: ContactMetadata.ContactGroup,
        query: String,
        onlyMatchingContactEmails: Boolean
    ): ContactMetadata.ContactGroup? {
        val matchingMembers = contactGroup.members.mapNotNull { member ->
            searchInContact(member, query, onlyMatchingContactEmails)
        }

        return if (contactGroup.name.containsNoCase(query) || matchingMembers.isNotEmpty()) {
            contactGroup.copy(members = matchingMembers)
        } else {
            null
        }
    }
}

