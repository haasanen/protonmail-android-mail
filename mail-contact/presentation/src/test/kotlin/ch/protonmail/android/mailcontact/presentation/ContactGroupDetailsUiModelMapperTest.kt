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
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.usecase.GetInitials
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupDetailsMember
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupDetailsUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupDetailsUiModelMapper
import ch.protonmail.android.maillabel.presentation.getHexStringFromColor
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.label.LabelTestData
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.testdata.contact.ContactGroupIdSample
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ContactGroupDetailsUiModelMapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getInitials = GetInitials()
    private val colorMapper = ColorMapper()
    private val contactGroupDetailsUiModelMapper = ContactGroupDetailsUiModelMapper(getInitials, colorMapper)

    @Test
    fun `maps ContactGroup to ContactGroupDetailsUiModel`() {
        val label = LabelTestData.buildLabel(
            "LabelId1",
            LabelType.ContactGroup,
            "Label 1",
            color = Color.Red.getHexStringFromColor()
        )
        val contact = ContactMetadata.Contact(
            id = ContactId("ContactId1"),
            name = "First name from contact email",
            avatar = AvatarInformation("FE", Color.Red.getHexStringFromColor()),
            emails = listOf(
                ContactEmail(
                    ContactEmailId("contact email id 1"),
                    "test1+alias@protonmail.com",
                    true,
                    lastUsedTime = 0
                ),
                ContactEmail(
                    ContactEmailId("contact email id 2"),
                    "test2+alias@protonmail.com",
                    true,
                    lastUsedTime = 0
                )
            )
        )
        val contactGroup = ContactMetadata.ContactGroup(
            ContactGroupIdSample.School,
            label.name,
            label.color!!,
            listOf(contact)
        )

        val actual = contactGroupDetailsUiModelMapper.toContactGroupDetailsUiModel(contactGroup)

        val expected = ContactGroupDetailsUiModel(
            id = ContactGroupIdSample.School,
            name = "Label 1",
            color = Color.Red,
            memberCount = 1,
            members = listOf(
                ContactGroupDetailsMember(
                    initials = "FE",
                    name = "First name from contact email",
                    email = "test1+alias@protonmail.com"
                )
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `maps empty ContactGroup members to ContactGroupDetailsUiModel`() {
        val label = LabelTestData.buildLabel(
            "LabelId1",
            LabelType.ContactGroup,
            "Label 1",
            color = Color.Red.getHexStringFromColor()
        )
        val contactGroup = ContactMetadata.ContactGroup(
            ContactGroupIdSample.School,
            label.name,
            label.color!!,
            emptyList()
        )

        val actual = contactGroupDetailsUiModelMapper.toContactGroupDetailsUiModel(contactGroup)

        val expected = ContactGroupDetailsUiModel(
            id = ContactGroupIdSample.School,
            name = "Label 1",
            color = Color.Red,
            memberCount = 0,
            members = emptyList()
        )

        assertEquals(expected, actual)
    }
}
