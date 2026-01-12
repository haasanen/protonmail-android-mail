/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.data.wrapper

import uniffi.proton_mail_uniffi.PrivacyInfo
import uniffi.proton_mail_uniffi.StrippedUtmInfo
import uniffi.proton_mail_uniffi.TrackerDomain
import uniffi.proton_mail_uniffi.TrackerInfo
import uniffi.proton_mail_uniffi.UtmLink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PrivacyInfoWrapperTest {

    @Test
    fun `should return null on null trackerInfo`() {
        // Given
        val trackerInfo = null
        val links = StrippedUtmInfo(listOf())
        val privacyInfo = PrivacyInfo(trackerInfo, links)

        // When
        val result = PrivacyInfoWrapper(privacyInfo)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null on null links`() {
        // Given
        val trackerInfo = TrackerInfo(listOf(), 0UL)
        val links = null
        val privacyInfo = PrivacyInfo(trackerInfo, links)

        // When
        val result = PrivacyInfoWrapper(privacyInfo)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null on both null fields`() {
        // Given
        val trackerInfo = null
        val links = null
        val privacyInfo = PrivacyInfo(trackerInfo, links)

        // When
        val result = PrivacyInfoWrapper(privacyInfo)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return the proper wrapped value`() {
        // Given
        val trackerInfo = TrackerInfo(
            trackers = listOf(
                TrackerDomain("example.com", listOf("https://example.com"))
            ),
            lastCheckedAt = 0UL
        )

        val links = StrippedUtmInfo(
            links = listOf(UtmLink("example.com/?utm=123456", "example.com"))
        )

        val privacyInfo = PrivacyInfo(trackerInfo, links)

        // When
        val result = PrivacyInfoWrapper(privacyInfo)

        // Then
        assertNotNull(result)
        assertEquals(trackerInfo, result.trackerInfo)
        assertEquals(links, result.strippedUtmInfo)
    }
}
