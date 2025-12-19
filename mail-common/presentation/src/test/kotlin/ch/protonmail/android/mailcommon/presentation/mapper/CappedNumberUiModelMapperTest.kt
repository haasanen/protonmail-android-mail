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

package ch.protonmail.android.mailcommon.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.CappedNumberUiModel
import ch.protonmail.android.mailcommon.presentation.model.NullCountPolicy
import ch.protonmail.android.mailcommon.presentation.model.ZeroCountPolicy
import ch.protonmail.android.mailcommon.presentation.model.toCappedNumberUiModel
import org.junit.Test
import kotlin.test.assertEquals

class CappedNumberUiModelMapperTest {

    private val cap = 99

    @Test
    fun `null + nullPolicy Zero maps to Zero`() {
        // Given & When
        val result = (null as Int?).toCappedNumberUiModel(
            cap = cap,
            nullPolicy = NullCountPolicy.Zero
        )

        // Then
        assertEquals(CappedNumberUiModel.Zero, result)
    }

    @Test
    fun `null + nullPolicy Empty maps to Empty`() {
        // Given & When
        val result = (null as Int?).toCappedNumberUiModel(
            cap = cap,
            nullPolicy = NullCountPolicy.Empty
        )

        // Then
        assertEquals(CappedNumberUiModel.Empty, result)
    }

    @Test
    fun `0 + zeroPolicy KeepZero maps to Exact 0`() {
        // Given & When
        val result = 0.toCappedNumberUiModel(
            cap = cap,
            zeroPolicy = ZeroCountPolicy.KeepZero
        )

        // Then
        assertEquals(CappedNumberUiModel.Exact(0), result)
    }

    @Test
    fun `0 + zeroPolicy Empty maps to Empty`() {
        // Given & When
        val result = 0.toCappedNumberUiModel(
            cap = cap,
            zeroPolicy = ZeroCountPolicy.Empty
        )

        // Then
        assertEquals(CappedNumberUiModel.Empty, result)
    }

    @Test
    fun `1 maps to Exact 1`() {
        // Given & When
        val result = 1.toCappedNumberUiModel(cap = cap)

        // Then
        assertEquals(CappedNumberUiModel.Exact(1), result)
    }

    @Test
    fun `value equal to cap maps to Exact cap`() {
        // Given & When
        val result = cap.toCappedNumberUiModel(cap = cap)

        assertEquals(CappedNumberUiModel.Exact(cap), result)
    }

    @Test
    fun `value above cap maps to Capped cap`() {
        // Given & When
        val result = (cap + 1).toCappedNumberUiModel(cap = cap)

        // Then
        assertEquals(CappedNumberUiModel.Capped(cap), result)
    }

    @Test
    fun `nullPolicy does not affect non-null values`() {
        // Given & When
        val result = 5.toCappedNumberUiModel(
            cap = cap,
            nullPolicy = NullCountPolicy.Empty
        )

        // Then
        assertEquals(CappedNumberUiModel.Exact(5), result)
    }

    @Test
    fun `zeroPolicy does not affect non-zero values`() {
        // Given & When
        val result = 5.toCappedNumberUiModel(
            cap = cap,
            zeroPolicy = ZeroCountPolicy.Empty
        )

        // Then
        assertEquals(CappedNumberUiModel.Exact(5), result)
    }
}
