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

import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcontact.presentation.model.ManageMembersUiModel
import ch.protonmail.android.mailcontact.presentation.model.ManageMembersUiModelMapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManageMembersUiModelMapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val manageMembersUiModelMapper = ManageMembersUiModelMapper()

    @Test
    fun `maps Contacts to ManageMembersUiModel`() {
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

        val expected = listOf(
            ManageMembersUiModel(
                id = ContactId("contact id 1"),
                name = "Contact name 1",
                email = "test1+alias@protonmail.com",
                initials = "FE",
                isSelected = true,
                isDisplayed = true
            ),
            ManageMembersUiModel(
                id = ContactId("contact id 2"),
                name = "Contact name 2",
                email = "test2+alias@protonmail.com",
                initials = "FE",
                isSelected = false,
                isDisplayed = true
            )
        )

        val actual = manageMembersUiModelMapper.toManageMembersUiModelList(
            contacts,
            listOf(ContactId("contact id 1"))
        )

        assertEquals(expected, actual)
    }
}
