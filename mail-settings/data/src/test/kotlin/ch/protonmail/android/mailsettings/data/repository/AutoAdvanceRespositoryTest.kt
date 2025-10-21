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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.data.local.AutoAdvanceDataSourceImpl
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AutoAdvanceRespositoryTest {


    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dataSource = mockk<AutoAdvanceDataSourceImpl>()
    private val repository: AutoAdvanceRepositoryImpl = AutoAdvanceRepositoryImpl(dataSource)

    private val userId = UserId("user-123")

    @Test
    fun `gets autoAdvance from data source`() = runTest {
        // Given
        coEvery { dataSource.getAutoAdvance(userId) } returns true.right()

        val result = repository.getAutoAdvance(userId)
        // When
        coVerify(exactly = 1) { dataSource.getAutoAdvance(userId) }
        Assert.assertTrue(result.getOrNull() ?: false)
    }

    @Test
    fun `get autoAdvance returns error when data source fails`() = runTest {
        // Given
        val err = DataError.Local.NoUserSession
        coEvery { dataSource.getAutoAdvance(userId) } returns err.left()

        // When
        val result = repository.getAutoAdvance(userId)

        // Then
        assertEquals(Either.Left(err), result)
        coVerify(exactly = 1) { dataSource.getAutoAdvance(userId) }
    }
}
