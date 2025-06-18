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

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.FifteenMinutes
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.FiveMinutes
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.Immediately
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.OneMinute
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.SixtyMinutes
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.TenMinutes
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.ThirtyMinutes
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.TwoMinutes
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockIntervalEffects
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockIntervalState
import ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel.AutoLockIntervalViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoLockIntervalViewModelTest {

    private val autoLockFlow = MutableSharedFlow<AutoLock>()
    private val autolockRepository: AutoLockRepository = mockk {
        coEvery {
            this@mockk.observeAppLock()
        } returns autoLockFlow

        coEvery {
            this@mockk.updateAutoLockInterval(any())
        } returns Unit.right()
    }

    private lateinit var viewModel: AutoLockIntervalViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = AutoLockIntervalViewModel(
            autolockRepository
        )
    }

    @Test
    fun `close effect emitted when interval is updated`() = runTest {
        // Given
        coEvery { autolockRepository.updateAutoLockInterval(any()) } returns Unit.right()

        // When
        viewModel.onIntervalSelected(FifteenMinutes)
        // then
        assertEquals(
            AutoLockIntervalEffects(close = Effect.Companion.of(Unit)),
            viewModel.effects.value
        )
    }

    @Test
    fun `state returns interval with 15 minutes selected when saved interval is 15 minutes`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()

            // When
            autoLockFlow.emit(AutoLock(autolockInterval = FifteenMinutes))

            // Then
            assertEquals(
                AutoLockIntervalState.Data(
                    currentInterval = FifteenMinutes,
                    intervalsToChoices = uiIntervals
                ),
                awaitItem()
            )
        }
    }


    @Test
    fun `state is updated when repository emits an updated interval`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()
            // When
            autoLockFlow.emit(AutoLock(autolockInterval = FifteenMinutes))
            // Then
            assertEquals(
                AutoLockIntervalState.Data(
                    currentInterval = FifteenMinutes,
                    intervalsToChoices = uiIntervals
                ),
                awaitItem()
            )

            // When
            autoLockFlow.emit(AutoLock(autolockInterval = FiveMinutes))
            // Then
            assertEquals(
                AutoLockIntervalState.Data(
                    currentInterval = FiveMinutes,
                    intervalsToChoices = uiIntervals
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `updates autolockRepository on repository when intervalSelected`() = runTest {
        // Given
        coEvery { autolockRepository.updateAutoLockInterval(any()) } returns Unit.right()

        // When
        viewModel.onIntervalSelected(FifteenMinutes)

        // Then
        coVerify { autolockRepository.updateAutoLockInterval(FifteenMinutes) }
    }

    private suspend fun ReceiveTurbine<AutoLockIntervalState>.initialStateEmitted() {
        awaitItem() as AutoLockIntervalState.Loading
    }

    companion object {

        val uiIntervals = mapOf(
            Immediately to TextUiModel(R.string.mail_pinlock_settings_autolock_immediately),
            OneMinute to TextUiModel(R.string.mail_pinlock_settings_autolock_description_one_minute),
            TwoMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_two_minutes),
            FiveMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_five_minutes),
            TenMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_ten_minutes),
            FifteenMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_fifteen_minutes),
            ThirtyMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_thirty_minutes),
            SixtyMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_sixty_minutes)
        )
    }
}
