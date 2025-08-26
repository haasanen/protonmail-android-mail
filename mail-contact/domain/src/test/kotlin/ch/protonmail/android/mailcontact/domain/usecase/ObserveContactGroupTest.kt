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
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.testdata.contact.ContactGroupIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveContactGroupTest {

    private val testContactGroupId = ContactGroupIdSample.Friends
    private val contactGroup = ContactMetadata.ContactGroup(
        id = testContactGroupId,
        name = "Friends",
        color = "#FF0000",
        members = listOf(
            ContactEmail(
                id = ContactId("contact email id"),
                email = "test1@protonmail.com",
                isProton = false,
                lastUsedTime = 0,
                name = "Contact Name",
                avatarInformation = AvatarInformation("C", "")
            )
        )
    )

    private val contacts = listOf(contactGroup)
    private val contactRepository = mockk<ContactRepository> {
        // Mock repository to return contacts list
        coEvery { this@mockk.observeAllContacts(UserIdTestData.userId) } returns flowOf(contacts.right())
    }

    private val observeContactGroup = ObserveContactGroup(contactRepository)

    @Test
    fun `when repository returns contacts, the correct contact group is emitted`() = runTest {
        // When
        observeContactGroup(UserIdTestData.userId, testContactGroupId).test {
            // Then
            val actual = assertIs<Either.Right<ContactMetadata.ContactGroup>>(awaitItem())
            assertEquals(contactGroup, actual.value)
            awaitComplete()
        }
    }

    @Test
    fun `when contact repository returns any data error, then emit GetContactsError`() = runTest {
        // Given
        coEvery { contactRepository.observeAllContacts(UserIdTestData.userId) } returns flowOf(GetContactError.left())

        // When
        observeContactGroup(UserIdTestData.userId, testContactGroupId).test {
            // Then
            val actual = assertIs<Either.Left<GetContactGroupError.GetContactsError>>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when contact group is not found, then emit ContactGroupNotFound error`() = runTest {
        // Given
        val otherContactGroupId = ContactGroupIdSample.School
        coEvery { contactRepository.observeAllContacts(UserIdTestData.userId) } returns flowOf(GetContactError.left())

        // When
        observeContactGroup(UserIdTestData.userId, otherContactGroupId).test {
            // Then
            val actual = assertIs<Either.Left<GetContactGroupError.ContactGroupNotFound>>(awaitItem())
            awaitComplete()
        }
    }
}
