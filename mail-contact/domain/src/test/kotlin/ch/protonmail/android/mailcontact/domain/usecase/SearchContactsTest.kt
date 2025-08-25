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

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.testdata.contact.ContactEmailSample
import ch.protonmail.android.testdata.contact.ContactTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Suppress("MaxLineLength")
class SearchContactsTest {

    private val userId = UserIdTestData.userId

    private val contactRepository = mockk<ContactRepository> {
        coEvery { this@mockk.observeAllContacts(userId) } returns flowOf(Either.Right(ContactTestData.contacts))
    }

    private val searchContacts = SearchContacts(contactRepository)

    @Test
    fun `when there are multiple matching contacts, they are emitted`() = runTest {
        // Given
        val query = "cont"

        // When
        searchContacts(userId, query).test {
            // Then
            val actual = assertIs<Either.Right<List<ContactMetadata.Contact>>>(awaitItem())
            assertEquals(ContactTestData.contacts, actual.value)
            awaitComplete()
        }
    }

    @Test
    fun `when there is contact matched only by name, it is emitted with all ContactEmails`() = runTest {
        // Given
        val query = "impo"

        val contact = ContactTestData.buildContactWith(
            name = "important contact display name", // <-- match
            contactEmails = listOf(
                ContactTestData.buildContactEmailWith(
                    address = "address1@proton.ch"
                ),
                ContactTestData.buildContactEmailWith(
                    address = "address2@protonmail.ch"
                )
            )
        )
        val contacts = ContactTestData.contacts + contact
        coEvery { contactRepository.observeAllContacts(userId) } returns flowOf(Either.Right(contacts))

        // When
        searchContacts(userId, query).test {
            // Then
            val actual = assertIs<Either.Right<List<ContactMetadata.Contact>>>(awaitItem())
            assertEquals(listOf(contact), actual.value)
            awaitComplete()
        }
    }

    @Test
    fun `when there is contact matched only by ContactEmail, it is emitted with all ContactEmails`() = runTest {
        // Given
        val query = "mail"

        val contact = ContactTestData.buildContactWith(
            name = "important contact display name",
            contactEmails = listOf(
                ContactTestData.buildContactEmailWith(
                    address = "address1@proton.ch"
                ),
                ContactTestData.buildContactEmailWith(
                    address = "address2@protonmail.ch" // <-- match
                )
            )
        )
        val contacts = ContactTestData.contacts + contact
        coEvery { contactRepository.observeAllContacts(userId) } returns flowOf(Either.Right(contacts))

        // When
        searchContacts(userId, query).test {
            // Then
            val actual = assertIs<Either.Right<List<ContactMetadata.Contact>>>(awaitItem())
            assertTrue(actual.value.size == 1)

            val matchedContact = actual.value.first()

            assertEquals(contact.id, matchedContact.id)
            assertEquals(contact.name, matchedContact.name)

            assertTrue(matchedContact.emails.size == 2)
            assertEquals(
                listOf(contact.emails[0]),
                listOf(matchedContact.emails[0])
            )
            assertEquals(
                listOf(contact.emails[1]),
                listOf(matchedContact.emails[1])
            )
            awaitComplete()
        }
    }

    @Test
    fun `when there are no matching contacts, empty list is emitted`() = runTest {
        // Given
        val query = "there is no contact like this"

        // When
        searchContacts(userId, query).test {
            // Then
            val actual = assertIs<Either.Right<List<ContactMetadata.Contact>>>(awaitItem())
            assertEquals(emptyList(), actual.value)
            awaitComplete()
        }
    }

    @Test
    fun `when observe contacts returns any error, this error is emitted`() = runTest {
        // Given
        val query = "cont"
        coEvery { contactRepository.observeAllContacts(userId) } returns flowOf(Either.Left(GetContactError))

        // When
        searchContacts(userId, query).test {
            // Then
            assertIs<Either.Left<GetContactError>>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `group name match adds the group to results`() = runTest {
        // Given
        val query = "group here"
        val group = ContactTestData.contactGroupSuggestion
        val dataset = ContactTestData.contacts + group
        coEvery {
            contactRepository.observeAllContacts(userId)
        } returns flowOf(dataset.right())

        // When
        searchContacts(userId, query).test {
            // Then
            val actual = awaitItem() as Either.Right<List<ContactMetadata>>
            val groups = actual.value.filterIsInstance<ContactMetadata.ContactGroup>()
            assertTrue(groups.any { it.id == group.id })
            awaitComplete()
        }
    }

    @Test
    fun `member match inside group emits contact items`() = runTest {
        // Given
        val memberQuery = ContactEmailSample.contactGroupSuggestionEmail1.email.substring(0, 5)
        val group = ContactTestData.contactGroupSuggestion
        val dataset = listOf<ContactMetadata>(group)
        coEvery { contactRepository.observeAllContacts(userId) } returns flowOf(dataset.right())

        // When
        searchContacts(userId, memberQuery).test {
            // Then
            val actual = awaitItem() as Either.Right<List<ContactMetadata>>
            val contacts = actual.value.filterIsInstance<ContactMetadata.Contact>()
            assertTrue(contacts.any { c -> c.id == ContactEmailSample.contactGroupSuggestionEmail1.id })
            awaitComplete()
        }
    }

    @Test
    fun `duplicate contacts coming from multiple groups appear only once`() = runTest {
        // Given
        val sharedMember: ContactEmail = ContactEmailSample.contactGroupSuggestionEmail1
        val query = sharedMember.email.substring(0, 5)

        val groupA = ContactMetadata.ContactGroup(
            id = ContactGroupId("grp-A"),
            name = "Alpha Group",
            color = AvatarInformationSample.avatarSample.color,
            members = listOf(sharedMember)
        )
        val groupB = ContactMetadata.ContactGroup(
            id = ContactGroupId("grp-B"),
            name = "Beta Group",
            color = AvatarInformationSample.avatarSample.color,
            members = listOf(sharedMember)
        )

        val dataset = listOf<ContactMetadata>(groupA, groupB)
        coEvery {
            contactRepository.observeAllContacts(userId)
        } returns flowOf(dataset.right())

        // When
        searchContacts(userId, query).test {
            // Then
            val actual = awaitItem() as Either.Right<List<ContactMetadata>>
            val contacts = actual.value.filterIsInstance<ContactMetadata.Contact>()
            assertEquals(1, contacts.count { it.id == sharedMember.id })
            awaitComplete()
        }
    }
}
