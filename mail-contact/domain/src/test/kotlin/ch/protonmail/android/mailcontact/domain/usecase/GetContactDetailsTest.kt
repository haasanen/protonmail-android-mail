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

import arrow.core.right
import ch.protonmail.android.mailcontact.domain.model.ContactDetailCard
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.testdata.contact.ContactIdTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetContactDetailsTest {

    private val contactRepository = mockk<ContactRepository>()

    private val getContactDetails = GetContactDetails(contactRepository)

    @Test
    fun `should call the correct repository method`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val contactId = ContactIdTestData.contactId1
        val contactDetailCard = mockk<ContactDetailCard>(relaxed = true)
        coEvery { contactRepository.getContactDetails(userId, contactId) } returns contactDetailCard.right()

        // When
        val result = getContactDetails(userId, contactId)

        // Then
        assertEquals(contactDetailCard.right(), result)
        coVerify { contactRepository.getContactDetails(userId, contactId) }
    }
}
