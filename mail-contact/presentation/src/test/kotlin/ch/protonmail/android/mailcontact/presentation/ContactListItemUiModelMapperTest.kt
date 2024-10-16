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

package ch.protonmail.android.mailcontact.presentation

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModelMapper
import ch.protonmail.android.testdata.user.UserIdTestData
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactItemUiModelMapper
import ch.protonmail.android.testdata.contact.ContactGroupIdSample
import ch.protonmail.android.testdata.contact.ContactIdTestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals

class ContactListItemUiModelMapperTest {

    private val contactGroupItemUiModelMapper = mockk<ContactGroupItemUiModelMapper>()
    private val contactItemUiModelMapper = mockk<ContactItemUiModelMapper>()
    private val contactListItemUiModelMapper = ContactListItemUiModelMapper(
        contactGroupItemUiModelMapper = contactGroupItemUiModelMapper,
        contactItemUiModelMapper = contactItemUiModelMapper
    )

    @Test
    fun `return correct contact items and group items with headers`() {
        // Arrange: Create mock contact and group objects
        val contact1 = ContactMetadata.Contact(
            userId = UserIdTestData.userId,
            id = ContactIdTestData.contactId1,
            avatar = AvatarInformationSample.avatarSample,
            name = "first contact",
            emails = listOf(
                ContactEmail(
                    UserIdTestData.userId,
                    ContactEmailId("contact email id 1"),
                    "First contact email",
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

        val contactGroup1 = ContactMetadata.ContactGroup(
            id = ContactGroupIdSample.Friends,
            name = "Family Group",
            color = "Blue",
            emails = emptyList()
        )

        val groupedContacts = listOf(
            GroupedContacts(
                groupedBy = "F",
                contacts = listOf(contact1)
            ),
            GroupedContacts(
                groupedBy = "Groups",
                contacts = listOf(contactGroup1)
            )
        )

        // Mock the behavior of mappers
        every { contactItemUiModelMapper.toContactItemUiModel(contact1) } returns ContactListItemUiModel.Contact(
            id = contact1.id,
            name = contact1.name,
            emailSubtext = TextUiModel(contact1.emails.first().email),
            avatar = AvatarUiModel.ParticipantAvatar(
                initial = "FC",
                address = contact1.emails.first().email,
                bimiSelector = null,
                color = Color.Unspecified
            )
        )

        every {
            contactGroupItemUiModelMapper.toContactGroupItemUiModel(contactGroup1)
        } returns ContactListItemUiModel.ContactGroup(
            id = contactGroup1.id,
            name = contactGroup1.name,
            memberCount = 0, // Assume 0 members for simplicity in the test
            color = Color.Blue
        )

        // Act: Call the mapper method
        val actual = contactListItemUiModelMapper.toContactListItemUiModel(groupedContacts)

        // Assert: Verify the result includes headers and the mapped contacts
        val expected = listOf(
            ContactListItemUiModel.Header(value = "F"),
            ContactListItemUiModel.Contact(
                id = contact1.id,
                name = contact1.name,
                emailSubtext = TextUiModel(contact1.emails.first().email),
                avatar = AvatarUiModel.ParticipantAvatar(
                    initial = "FC",
                    address = contact1.emails.first().email,
                    bimiSelector = null,
                    color = Color.Unspecified
                )
            ),
            ContactListItemUiModel.Header(value = "Groups"),
            ContactListItemUiModel.ContactGroup(
                id = contactGroup1.id,
                name = contactGroup1.name,
                memberCount = 0,
                color = Color.Blue
            )
        )

        assertEquals(expected, actual)

        // Verify that the mappers were called
        verify { contactItemUiModelMapper.toContactItemUiModel(contact1) }
        verify { contactGroupItemUiModelMapper.toContactGroupItemUiModel(contactGroup1) }
    }
}

