package ch.protonmail.android.mailcontact.presentation.contactgroupdetails

import androidx.compose.ui.graphics.Color
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.AvatarUiModel
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class ContactGroupDetailsUiModelMapperTest {

    private val colorMapper = mockk<ColorMapper> {
        every { this@mockk.toColor(any()) } returns Color.Blue.right()
    }

    private val contactGroupDetailsUiModelMapper = ContactGroupDetailsUiModelMapper(colorMapper)

    @Test
    fun `should map contact group to ui model`() {
        // Given
        val contactGroup = ContactMetadata.ContactGroup(
            id = ContactGroupId("contactGroupId"),
            name = "Proton Mail",
            color = "color",
            members = listOf(
                ContactEmail(
                    id = ContactId("contactEmailId1"),
                    email = "proton1@protonmail.com",
                    isProton = false,
                    lastUsedTime = 0,
                    name = "Proton1",
                    avatarInformation = AvatarInformation("P", "color")
                ),
                ContactEmail(
                    id = ContactId("contactEmailId2"),
                    email = "proton2@protonmail.com",
                    isProton = false,
                    lastUsedTime = 0,
                    name = "Proton2",
                    avatarInformation = AvatarInformation("P", "color")
                )
            )
        )
        // When
        val result = contactGroupDetailsUiModelMapper.toUiModel(contactGroup)

        // Then
        val expected = ContactGroupDetailsUiModel(
            color = Color.Blue,
            name = "Proton Mail",
            memberCount = 2,
            members = listOf(
                ContactGroupMemberUiModel(
                    id = ContactId("contactEmailId1"),
                    avatarUiModel = AvatarUiModel.Initials("P", Color.Blue),
                    name = "Proton1",
                    emailAddress = "proton1@protonmail.com"
                ),
                ContactGroupMemberUiModel(
                    id = ContactId("contactEmailId2"),
                    avatarUiModel = AvatarUiModel.Initials("P", Color.Blue),
                    name = "Proton2",
                    emailAddress = "proton2@protonmail.com"
                )
            )
        )
        assertEquals(expected, result)
    }
}
