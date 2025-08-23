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

package ch.protonmail.android.mailsession.domain.usecase

import java.time.Instant
import ch.protonmail.android.mailsession.domain.model.UserAccountAge
import ch.protonmail.android.testdata.user.UserTestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetUserAccountCreationDaysTest {

    private val getUserAccountCreationDays = GetUserAccountCreationDays()

    @BeforeTest
    fun setup() {
        mockkStatic(Instant::class)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should return the proper account age (0)`() {
        val user = UserTestData.Primary.copy(createTimeUtc = 0L)
        val currentTime = 1L
        val expected = UserAccountAge(days = 0)

        // Given
        every { Instant.now() } returns mockk { every { toEpochMilli() } returns currentTime }

        // When
        val actual = getUserAccountCreationDays(user)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return the proper account age (past)`() {
        val user = UserTestData.Primary.copy(createTimeUtc = 0L)
        val currentTime = 86_400_000L * 5
        val expected = UserAccountAge(days = 5)

        // Given
        every { Instant.now() } returns mockk { every { toEpochMilli() } returns currentTime }

        // When
        val actual = getUserAccountCreationDays(user)

        // Then
        assertEquals(expected, actual)
    }
}
