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

package ch.protonmail.android.mailmailbox.presentation.mailbox.mapper

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.presentation.mapper.AvatarInformationMapper
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailmessage.domain.model.Sender
import ch.protonmail.android.testdata.mailbox.MailboxTestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MailboxAvatarUiModelMapperTest {
    private val avatarInformationMapper: AvatarInformationMapper = mockk()
    private val mailboxAvatarUiModelMapper = MailboxAvatarUiModelMapper(avatarInformationMapper)

    @Test
    fun `should map MailboxItem to AvatarUiModel correctly`() {
        // Given
        val mailboxItem = MailboxTestData.readMailboxItem.copy(
            senders = listOf(
                Sender(
                    address = "sender@example.com",
                    name = "Sender Name",
                    isProton = true,
                    bimiSelector = "bimiSelector"
                )
            ),
            avatarInformation = AvatarInformation(
                initials = "SN",
                color = "#FF5733"
            )
        )
        val expectedAvatarUiModel = AvatarUiModel.ParticipantAvatar(
            initial = "SN",
            address = "sender@example.com",
            bimiSelector = "bimiSelector",
            color = Color(0xFFFF5733)
        )
        every {
            avatarInformationMapper.toUiModel(mailboxItem.avatarInformation, "sender@example.com", "bimiSelector")
        } returns expectedAvatarUiModel

        // When
        val result = mailboxAvatarUiModelMapper(mailboxItem)

        // Then
        assertEquals(expectedAvatarUiModel, result)
        verify {
            avatarInformationMapper.toUiModel(mailboxItem.avatarInformation, "sender@example.com", "bimiSelector")
        }
    }

    @Test
    fun `should return default AvatarUiModel when sender is null`() {
        // Given
        val mailboxItem = MailboxTestData.readMailboxItem.copy(
            senders = emptyList(),
            avatarInformation = AvatarInformation(
                initials = "SN",
                color = "#FF5733"
            )
        )
        val expectedAvatarUiModel = AvatarUiModel.ParticipantAvatar(
            initial = "SN",
            address = "",
            bimiSelector = null,
            color = Color(0xFFFF5733)
        )

        every {
            avatarInformationMapper.toUiModel(mailboxItem.avatarInformation, "", null)
        } returns expectedAvatarUiModel

        // When
        val result = mailboxAvatarUiModelMapper(mailboxItem)

        // Then
        assertEquals(expectedAvatarUiModel, result)
        verify { avatarInformationMapper.toUiModel(mailboxItem.avatarInformation, "", null) }
    }

}
