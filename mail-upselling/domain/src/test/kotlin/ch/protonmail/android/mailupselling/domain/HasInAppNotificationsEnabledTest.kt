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

package ch.protonmail.android.mailupselling.domain

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.model.UserSettings
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailupselling.domain.usecase.HasInAppNotificationsEnabled
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HasInAppNotificationsEnabledTest {

    private val sessionRepository = mockk<UserSessionRepository>()

    private lateinit var hasInAppNotificationsEnabled: HasInAppNotificationsEnabled

    @BeforeTest
    fun setup() {
        hasInAppNotificationsEnabled = HasInAppNotificationsEnabled(sessionRepository)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return true when bit 14 is set`() = runTest {
        // Given
        coEvery { sessionRepository.getUserSettings(userId) } returns UserSettings(16_384, false)

        // When
        val actual = hasInAppNotificationsEnabled(userId)

        // Then
        assertEquals(true.right(), actual)
    }

    @Test
    fun `should return false when bit 14 is not set`() = runTest {
        // Given
        coEvery { sessionRepository.getUserSettings(userId) } returns UserSettings(0, false)

        // When
        val actual = hasInAppNotificationsEnabled(userId)

        // Then
        assertEquals(false.right(), actual)
    }

    @Test
    fun `should return data error on invalid user settings`() = runTest {
        // Given
        coEvery { sessionRepository.getUserSettings(userId) } returns null

        // When
        val actual = hasInAppNotificationsEnabled(userId)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }

    private companion object {

        val userId = UserId("user-id")
    }
}
