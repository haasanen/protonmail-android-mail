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

import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailupselling.domain.model.BlackFridayPhase
import ch.protonmail.android.mailupselling.domain.usecase.GetCurrentBlackFridayPhase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetCurrentBlackFridayPhaseTest {

    private val blackFridayEnabledFlag1 = mockk<FeatureFlag<Boolean>>()
    private val blackFridayEnabledFlag2 = mockk<FeatureFlag<Boolean>>()

    private lateinit var getCurrentBlackFridayPhase: GetCurrentBlackFridayPhase

    @BeforeTest
    fun setup() {
        getCurrentBlackFridayPhase = GetCurrentBlackFridayPhase(
            wave1Flag = blackFridayEnabledFlag1,
            wave2Flag = blackFridayEnabledFlag2
        )
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return none when both FFs are off`() = runTest {
        // Given
        coEvery { blackFridayEnabledFlag1.get() } returns false
        coEvery { blackFridayEnabledFlag2.get() } returns false

        // When
        val actual = getCurrentBlackFridayPhase()

        // Then
        assertEquals(BlackFridayPhase.None, actual)
    }

    @Test
    fun `should return wave1 when wave1 FF is on`() = runTest {
        // Given
        coEvery { blackFridayEnabledFlag1.get() } returns true
        coEvery { blackFridayEnabledFlag2.get() } returns false

        // When
        val actual = getCurrentBlackFridayPhase()

        // Then
        assertEquals(BlackFridayPhase.Active.Wave1, actual)
    }

    @Test
    fun `should return wave1 when wave2 FF is on`() = runTest {
        // Given
        coEvery { blackFridayEnabledFlag1.get() } returns false
        coEvery { blackFridayEnabledFlag2.get() } returns true

        // When
        val actual = getCurrentBlackFridayPhase()

        // Then
        assertEquals(BlackFridayPhase.Active.Wave2, actual)
    }

    @Test
    fun `should return wave2 when both FFs are on`() = runTest {
        // Given
        coEvery { blackFridayEnabledFlag1.get() } returns true
        coEvery { blackFridayEnabledFlag2.get() } returns true

        // When
        val actual = getCurrentBlackFridayPhase()

        // Then
        assertEquals(BlackFridayPhase.Active.Wave2, actual)
    }
}
