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

package ch.protonmail.android.mailcontact.data.mapper

import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.testdata.contact.rust.LocalContactTestData
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactGroupItemMapperTest {

    private val contactGroupItemMapper = ContactGroupItemMapper()

    @Test
    fun `should map local contact group to domain model correctly`() {
        // Given
        val localContactGroup = LocalContactTestData.contactGroup1
        val expectedContactGroup = ContactMetadata.ContactGroup(
            id = localContactGroup.v1.id.toContactGroupId(),
            name = localContactGroup.v1.name,
            color = "#FFD700",
            members = listOf(
                ContactEmail(
                    id = ContactId("1"),
                    "family@example.com",
                    isProton = false,
                    lastUsedTime = 0,
                    name = "Family",
                    avatarInformation = AvatarInformation("Fam", "#FFD700")
                )
            )
        )

        // When
        val result = contactGroupItemMapper.toContactGroup(localContactGroup)

        // Then
        assertEquals(expectedContactGroup, result)
    }

}
