/*
 * Copyright (c) 2025 Proton Technologies AG
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
package ch.protonmail.android.mailsettings.data.mapper

import ch.protonmail.android.testdata.mailsettings.rust.LocalMailSettingsTestData
import me.proton.core.mailsettings.domain.entity.ShowImage
import kotlin.test.Test
import kotlin.test.assertEquals

class MailSettingsMapperImageTest {

    @Test
    fun `no images should be shown when both remote and embedded are blocked`() {
        // Given
        val local = LocalMailSettingsTestData.mailSettings.copy(
            hideRemoteImages = true,
            hideEmbeddedImages = true
        )

        // When
        val result = with(MailSettingsMapper) {
            local.toMailSettings()
        }

        // Then
        assertEquals(result.showImages?.enum, ShowImage.None)
    }

    @Test
    fun `only remote images should be shown when embedded are blocked`() {
        // Given
        val local = LocalMailSettingsTestData.mailSettings.copy(
            hideRemoteImages = false,
            hideEmbeddedImages = true
        )

        // When
        val result = with(MailSettingsMapper) {
            local.toMailSettings()
        }

        // Then
        assertEquals(result.showImages?.enum, ShowImage.Remote)
    }

    @Test
    fun `only embedded images should be shown when remote are blocked`() {
        // Given
        val local = LocalMailSettingsTestData.mailSettings.copy(
            hideRemoteImages = true,
            hideEmbeddedImages = false
        )

        // When
        val result = with(MailSettingsMapper) {
            local.toMailSettings()
        }

        // Then the mapped enum should be ShowImage.Embedded
        assertEquals(result.showImages?.enum, ShowImage.Embedded)
    }

    @Test
    fun `all images should be shown when nothing is blocked`() {
        // Given
        val local = LocalMailSettingsTestData.mailSettings.copy(
            hideRemoteImages = false,
            hideEmbeddedImages = false
        )

        // When
        val result = with(MailSettingsMapper) {
            local.toMailSettings()
        }

        // Then
        assertEquals(result.showImages?.enum, ShowImage.Both)
    }
}
