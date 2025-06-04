/*
 * Copyright (c) 2022 Proton Technologies AG
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

package protonmail.android.mailpinlock.presentation.autolock

import app.cash.turbine.test
import ch.protonmail.android.mailpinlock.domain.AutolockRepository
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsViewModel
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class AutoLockSettingsViewModelTest {

    private val autolockRepository: AutolockRepository = mockk()

    private val viewModel by lazy {
        AutoLockSettingsViewModel(
            autolockRepository
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should return loading state when first launched`() = runTest {
        // Given
        // When + Then
       /* viewModel.state.test {
            val loadingState = awaitItem()
        }*/
    }


    private companion object {


    }
}
