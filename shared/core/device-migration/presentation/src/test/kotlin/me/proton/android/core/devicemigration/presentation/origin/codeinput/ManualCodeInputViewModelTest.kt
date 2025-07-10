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

package me.proton.android.core.devicemigration.presentation.origin.codeinput

import android.content.Context
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.android.core.devicemigration.presentation.origin.usecase.ForkSessionIntoTargetDevice
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCodeInputViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var forkSessionIntoTargetDevice: ForkSessionIntoTargetDevice

    private lateinit var tested: ManualCodeInputViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ManualCodeInputViewModel(
            context = context,
            forkSessionIntoTargetDevice = forkSessionIntoTargetDevice
        )
    }

    @Test
    fun `submit QR code`() = runTest {
        // GIVEN
        coEvery { forkSessionIntoTargetDevice(any()) } returns ForkSessionIntoTargetDevice.Result.Success

        tested.state.test {
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading), awaitItem())
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Idle), awaitItem())

            // WHEN
            tested.perform(ManualCodeInputAction.Submit(""))

            // THEN
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Error.EmptyCode), awaitItem())
        }
    }

    @Test
    fun `submit invalid code`() = runTest {
        // GIVEN
        val errMsg = "Cannot parse"
        coEvery { forkSessionIntoTargetDevice(any()) } returns ForkSessionIntoTargetDevice.Result.Error(errMsg)

        tested.state.test {
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading), awaitItem())
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Idle), awaitItem())

            // WHEN
            tested.perform(ManualCodeInputAction.Submit("invalid-qr-code"))

            // THEN
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading), awaitItem())
            assertEquals(
                ManualCodeInputStateHolder(state = ManualCodeInputState.Error.InvalidCode(errMsg)),
                awaitItem()
            )
        }
    }
}
