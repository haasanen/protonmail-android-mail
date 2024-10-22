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
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.testdata.contact.ContactEmailSample
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetContactsByIdTest {

    private val defaultTestContacts = listOf(
        ContactMetadata.Contact(
            id = ContactId("ContactId1"),
            name = "John Doe",
            avatar = AvatarInformationSample.avatarSample,
            emails = listOf(
                ContactEmailSample.contactEmail1
            )
        ),
        ContactMetadata.Contact(
            id = ContactId("ContactId2"),
            name = "Jack",
            avatar = AvatarInformationSample.avatarSample,
            emails = listOf(
                ContactEmailSample.contactEmail2
            )
        ),
        ContactMetadata.Contact(
            id = ContactId("ContactId3"),
            name = "Contact 3",
            avatar = AvatarInformationSample.avatarSample,
            emails = listOf(
                ContactEmailSample.contactEmail3
            )
        )
    )

    private val observeContacts = mockk<ObserveContacts> {
        coEvery { this@mockk.invoke(UserIdTestData.userId) } returns flowOf(Either.Right(defaultTestContacts))
    }

    private val getContactsById = GetContactsById(observeContacts)

    @Test
    fun `when observe contacts returns contacts they are successfully emitted`() = runTest {
        // When
        val actual = getContactsById(UserIdTestData.userId, listOf(ContactId("ContactId2")))
        // Then
        assertIs<Either.Right<List<ContactMetadata.Contact>>>(actual)
        assertEquals(listOf(defaultTestContacts[1]), actual.value)
    }

    @Test
    fun `when observe contacts returns any error then emit get contacts error`() = runTest {
        // Given
        coEvery { observeContacts(UserIdTestData.userId) } returns flowOf(Either.Left(GetContactError))
        // When
        val actual = getContactsById(UserIdTestData.userId, listOf(ContactId("ContactId2")))
        // Then
        assertIs<Either.Left<GetContactError>>(actual)
    }
}
