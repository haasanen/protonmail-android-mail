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
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.contact.ContactEmailSample
import ch.protonmail.android.uicomponents.utils.getHexStringFromColor
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ContactGroupItemUiModelMapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val colorMapper = ColorMapper()
    private val contactGroupItemUiModelMapper = ContactGroupItemUiModelMapper(colorMapper)

    @Test
    fun `return correct contact group`() {
        val contactGroup = ContactMetadata.ContactGroup(
            id = ContactGroupId("LabelId1"),
            name = "Label 1",
            color = Color.Red.getHexStringFromColor(),
            members = listOf(
                ContactEmailSample.contactEmail1,
                ContactEmailSample.contactEmail2,
                ContactEmailSample.contactEmail3
            )
        )

        val actual = contactGroupItemUiModelMapper.toContactGroupItemUiModel(contactGroup)

        val expected = ContactListItemUiModel.ContactGroup(
            id = ContactGroupId("LabelId1"),
            name = "Label 1",
            memberCount = 3,
            color = Color.Red
        )

        assertEquals(expected, actual)
    }
}
