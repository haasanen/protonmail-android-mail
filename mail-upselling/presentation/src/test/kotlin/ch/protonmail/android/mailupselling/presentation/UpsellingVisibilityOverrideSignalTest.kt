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

package ch.protonmail.android.mailupselling.presentation

import app.cash.turbine.test
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibilityOverrideSignal
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UpsellingVisibilityOverrideSignalTest {

    private lateinit var upsellingVisibilityOverrideSignal: UpsellingVisibilityOverrideSignal

    @BeforeTest
    fun setup() {
        upsellingVisibilityOverrideSignal = UpsellingVisibilityOverrideSignal()
    }

    @Test
    fun `should emit false on start`() = runTest {
        // When
        upsellingVisibilityOverrideSignal.shouldHideUpselling().test {
            // Then
            assertFalse(awaitItem(), "Initial state is supposed to be false")
            expectNoEvents()
        }
    }

    @Test
    fun `should emit true when hide is called`() = runTest {
        upsellingVisibilityOverrideSignal.shouldHideUpselling().test {
            assertFalse(awaitItem(), "Initial state is supposed to be false")

            // When
            upsellingVisibilityOverrideSignal.hideUpselling()

            // Then
            assertTrue(awaitItem(), "Final state is supposed to be true")
            expectNoEvents()
        }
    }
}
