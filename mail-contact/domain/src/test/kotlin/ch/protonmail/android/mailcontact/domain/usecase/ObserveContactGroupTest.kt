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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.testdata.contact.ContactIdTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.maillabel.domain.model.LabelId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveContactGroupTest {

    private val contactGroup = ContactMetadata.ContactGroup(
        labelId = LabelId("LabelId1"),
        name = "Friends",
        color = "#FF0000",
        emails = listOf(
            ContactEmail(
                UserIdTestData.userId,
                ContactEmailId("contact email id 1"),
                "First name from contact email",
                "test1@protonmail.com",
                0,
                0,
                ContactIdTestData.contactId1,
                "test1@protonmail.com",
                emptyList(),
                true,
                lastUsedTime = 0
            )
        )
    )

    private val contacts = listOf(contactGroup)
    private val contactRepository = mockk<ContactRepository> {
        // Mock repository to return contacts list
        every { this@mockk.observeAllContacts(UserIdTestData.userId) } returns flowOf(contacts.right())
    }

    private val observeContactGroup = ObserveContactGroup(contactRepository)

    @Test
    fun `when repository returns contacts, the correct contact group is emitted`() = runTest {
        // When
        observeContactGroup(UserIdTestData.userId, LabelId("LabelId1")).test {
            // Then
            val actual = assertIs<Either.Right<ContactMetadata.ContactGroup>>(awaitItem())
            assertEquals(contactGroup, actual.value)
            awaitComplete()
        }
    }

    @Test
    fun `when contact repository returns any data error, then emit GetContactsError`() = runTest {
        // Given
        every { contactRepository.observeAllContacts(UserIdTestData.userId) } returns flowOf(GetContactError.left())

        // When
        observeContactGroup(UserIdTestData.userId, LabelId("LabelId1")).test {
            // Then
            val actual = assertIs<Either.Left<GetContactGroupError.GetContactsError>>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when contact group is not found, then emit ContactGroupNotFound error`() = runTest {
        // Given
        val otherLabelId = LabelId("LabelId2")
        every { contactRepository.observeAllContacts(UserIdTestData.userId) } returns flowOf(GetContactError.left())

        // When
        observeContactGroup(UserIdTestData.userId, otherLabelId).test {
            // Then
            val actual = assertIs<Either.Left<GetContactGroupError.ContactGroupNotFound>>(awaitItem())
            awaitComplete()
        }
    }
}
