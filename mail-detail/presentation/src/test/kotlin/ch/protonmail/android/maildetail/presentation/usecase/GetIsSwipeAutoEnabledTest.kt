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

package ch.protonmail.android.maildetail.presentation.usecase

import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetIsSwipeAutoEnabledTest {

    private val mockIsSwipeAutoEnabledFlag = mockk<FeatureFlag<Boolean>>()

    private lateinit var sut: GetIsSwipeAutoEnabled

    @Before
    fun setup() {
        sut = GetIsSwipeAutoEnabled(mockIsSwipeAutoEnabledFlag)
    }


    @Test
    fun `given enabled then invoke returns true`() = runTest {
        // Given
        coEvery { mockIsSwipeAutoEnabledFlag.get() } returns true

        // When
        val result = sut()

        assertTrue(result)
        coVerify { mockIsSwipeAutoEnabledFlag.get() }
    }

    @Test
    fun `given not enabled then invoke returns false`() = runTest {
        // Given
        coEvery { mockIsSwipeAutoEnabledFlag.get() } returns false

        // When
        val result = sut()

        // Then
        assertFalse(result)
        coVerify { mockIsSwipeAutoEnabledFlag.get() }
    }
}
