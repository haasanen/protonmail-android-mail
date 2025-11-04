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

package ch.protonmail.android.mailsettings.data.repository

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.data.local.SwipeNextDataSource
import ch.protonmail.android.mailsettings.domain.model.SwipeNextPreference
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SwipeNextRepositoryImplTest {

    private val dataSource = mockk<SwipeNextDataSource>()

    private lateinit var repository: SwipeNextRepositoryImpl


    @Before
    fun setup() {
        repository = SwipeNextRepositoryImpl(dataSource)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should get the current value from the datasource`() = runTest {
        // Given
        val expected = SwipeNextPreference.Enabled.right()
        coEvery { dataSource.getSwipeNext(userId) } returns expected

        // When
        val actual = repository.getSwipeNext(userId)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should pass the error when getting the current value from the datasource`() = runTest {
        // Given
        val expected = DataError.Local.NoUserSession.left()
        coEvery { dataSource.getSwipeNext(userId) } returns expected

        // When
        val actual = repository.getSwipeNext(userId)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should save the value and trigger new event on the observer`() = runTest {
        // Given
        val expected = SwipeNextPreference.Enabled.right()
        val secondExpected = SwipeNextPreference.NotEnabled.right()
        coEvery { dataSource.getSwipeNext(userId) } returns expected andThen secondExpected
        coEvery { dataSource.setSwipeNextEnabled(userId, false) } returns Unit.right()

        // When
        repository.observeSwipeNext(userId).test {
            assertEquals(expected, awaitItem())
            repository.setSwipeNextEnabled(userId, false)
            assertEquals(secondExpected, awaitItem())
        }

        // Then
        coVerify(exactly = 2) { dataSource.getSwipeNext(userId) }
        coVerify(exactly = 1) { dataSource.setSwipeNextEnabled(userId, false) }
        confirmVerified(dataSource)
    }


    private companion object {

        val userId = UserId("user-id")
    }
}
