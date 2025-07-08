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
import ch.protonmail.android.mailcommon.presentation.mapper.AvatarInformationMapper
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactSearchUiModelMapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.contact.ContactEmailSample
import ch.protonmail.android.testdata.contact.ContactGroupIdSample
import ch.protonmail.android.testdata.contact.ContactIdTestData
import ch.protonmail.android.uicomponents.utils.getHexStringFromColor
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ContactSearchUiModelMapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val colorMapper = ColorMapper()
    private val avatarInformationMapper = AvatarInformationMapper(colorMapper)
    private val contactGroupItemUiModelMapper = mockk<ContactGroupItemUiModelMapper>()
    private val contactItemUiModelMapper = mockk<ContactItemUiModelMapper>()

    private val contactSearchUiModelMapper = ContactSearchUiModelMapper(
        contactGroupItemUiModelMapper, contactItemUiModelMapper
    )

    @Test
    fun `maps list of ContactGroups to list of UiModel`() {
        // Given
        val contact = ContactMetadata.Contact(
            id = ContactIdTestData.contactId1,
            name = "Contact 1",
            avatar = AvatarInformationSample.avatarSample,
            emails = listOf(ContactEmailSample.contactEmail4)
        )
        val contactGroup = ContactMetadata.ContactGroup(
            id = ContactGroupIdSample.Friends,
            name = "Group 1",
            color = Color.Red.getHexStringFromColor(),
            members = listOf(
                ContactEmailSample.contactEmail1,
                ContactEmailSample.contactEmail2,
                ContactEmailSample.contactEmail3
            )
        )
        val expectedContactUiModel = ContactListItemUiModel.Contact(
            id = ContactIdTestData.contactId1,
            name = "Group 1",
            emailSubtext = TextUiModel(ContactEmailSample.contactEmail4.email),
            avatar = avatarInformationMapper.toUiModel(
                AvatarInformationSample.avatarSample,
                ContactEmailSample.contactEmail4.email, null
            )
        )
        val expectedContactGroupUiModel = ContactListItemUiModel.ContactGroup(
            id = ContactGroupIdSample.Friends,
            name = "Group 1",
            memberCount = contactGroup.members.size,
            color = Color.Red
        )
        val expected = listOf(expectedContactUiModel, expectedContactGroupUiModel)

        val contactList = listOf(contact) + listOf(contactGroup)
        every {
            contactGroupItemUiModelMapper.toContactGroupItemUiModel(contactGroup)
        } returns expectedContactGroupUiModel
        every { contactItemUiModelMapper.toContactItemUiModel(contact) } returns expectedContactUiModel

        // When
        val actual = contactSearchUiModelMapper.toContactListItemUiModel(contactList)


        // Then
        assertEquals(expected, actual)
    }
}
