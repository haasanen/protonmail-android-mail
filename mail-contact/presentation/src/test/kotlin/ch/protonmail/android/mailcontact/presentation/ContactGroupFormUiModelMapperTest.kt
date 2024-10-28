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
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupFormMember
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupFormUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupFormUiModelMapper
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

class ContactGroupFormUiModelMapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getInitials = GetInitials()
    private val colorMapper = ColorMapper()
    private val contactGroupFormUiModelMapper = ContactGroupFormUiModelMapper(colorMapper)

    @Test
    fun `maps ContactGroup to ContactGroupFormUiModel`() {
        val label = LabelTestData.buildLabel(
            "LabelId1",
            LabelType.ContactGroup,
            "Label 1",
            color = Color.Red.getHexStringFromColor()
        )
        val contacts = listOf(
            ContactMetadata.Contact(
                id = ContactId("contact id 1"),
                name = "Contact name 1",
                avatar = AvatarInformation("FE", "#000000"),
                emails = listOf(
                    ContactEmail(
                        ContactEmailId("contact email id 1"),
                        "test1+alias@protonmail.com",
                        true,
                        lastUsedTime = 0
                    )
                )
            ),
            ContactMetadata.Contact(
                id = ContactId("contact id 2"),
                name = "Contact name 2",
                avatar = AvatarInformation("FE", "#000000"),
                emails = listOf(
                    ContactEmail(
                        ContactEmailId("contact email id 2"),
                        "test2+alias@protonmail.com",
                        true,
                        lastUsedTime = 0
                    )
                )
            )
        )
        val contactGroup = ContactMetadata.ContactGroup(
            ContactGroupIdSample.Work,
            label.name,
            label.color!!,
            contacts
        )

        val actual = contactGroupFormUiModelMapper.toContactGroupFormUiModel(contactGroup)

        val expected = ContactGroupFormUiModel(
            id = ContactGroupIdSample.Work,
            name = "Label 1",
            color = Color.Red,
            memberCount = 2,
            members = listOf(
                ContactGroupFormMember(
                    id = ContactId("contact id 1"),
                    initials = "FE",
                    name = "Contact name 1",
                    email = "test1+alias@protonmail.com"
                ),
                ContactGroupFormMember(
                    id = ContactId("contact id 2"),
                    initials = "FE",
                    name = "Contact name 2",
                    email = "test2+alias@protonmail.com"
                )
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `maps empty ContactGroup members to ContactGroupFormUiModel`() {
        val label = LabelTestData.buildLabel(
            "LabelId1",
            LabelType.ContactGroup,
            "Label 1",
            color = Color.Red.getHexStringFromColor()
        )
        val contactGroup = ContactMetadata.ContactGroup(
            ContactGroupIdSample.Work,
            label.name,
            label.color!!,
            emptyList()
        )

        val actual = contactGroupFormUiModelMapper.toContactGroupFormUiModel(contactGroup)

        val expected = ContactGroupFormUiModel(
            id = ContactGroupIdSample.Work,
            name = "Label 1",
            color = Color.Red,
            memberCount = 0,
            members = emptyList()
        )

        assertEquals(expected, actual)
    }
}
