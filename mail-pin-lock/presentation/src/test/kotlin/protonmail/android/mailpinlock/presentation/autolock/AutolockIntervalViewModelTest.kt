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
import ch.protonmail.android.mailpinlock.domain.AutolockRepository
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.FifteenMinutes
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.FiveMinutes
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.Immediately
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.OneDay
import ch.protonmail.android.mailpinlock.model.AutoLockInterval.OneHour
import ch.protonmail.android.mailpinlock.model.Autolock
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.AutolockIntervalEffects
import ch.protonmail.android.mailpinlock.presentation.autolock.AutolockIntervalState
import ch.protonmail.android.mailpinlock.presentation.autolock.AutolockIntervalViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AutolockIntervalViewModelTest {

    private val autoLockFlow = MutableSharedFlow<Autolock>()
    private val autolockRepository: AutolockRepository = mockk {
        coEvery {
            this@mockk.observeAppLock()
        } returns autoLockFlow

        coEvery {
            this@mockk.updateAutolockInterval(any())
        } returns Unit.right()
    }

    private lateinit var viewModel: AutolockIntervalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = AutolockIntervalViewModel(
            autolockRepository
        )
    }

    @Test
    fun `close effect emitted when interval is updated`() = runTest {
        // Given
        coEvery { autolockRepository.updateAutolockInterval(any()) } returns Unit.right()

        // When
        viewModel.onIntervalSelected(FifteenMinutes)
        // then
        Assert.assertEquals(
            AutolockIntervalEffects(close = Effect.Companion.of(Unit)),
            viewModel.effects.value
        )
    }

    @Test
    fun `state returns interval with 15 minutes selected when saved interval is 15 minutes`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()

            // When
            autoLockFlow.emit(Autolock(autolockInterval = FifteenMinutes))

            // Then
            Assert.assertEquals(
                AutolockIntervalState.Data(
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
            autoLockFlow.emit(Autolock(autolockInterval = FifteenMinutes))
            // Then
            Assert.assertEquals(
                AutolockIntervalState.Data(
                    currentInterval = FifteenMinutes,
                    intervalsToChoices = uiIntervals
                ),
                awaitItem()
            )

            // When
            autoLockFlow.emit(Autolock(autolockInterval = OneDay))
            // Then
            Assert.assertEquals(
                AutolockIntervalState.Data(
                    currentInterval = OneDay,
                    intervalsToChoices = uiIntervals
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `updates autolockRepository on repository when intervalSelected`() = runTest {
        // Given
        coEvery { autolockRepository.updateAutolockInterval(any()) } returns Unit.right()

        // When
        viewModel.onIntervalSelected(FifteenMinutes)

        // Then
        coVerify { autolockRepository.updateAutolockInterval(FifteenMinutes) }
    }

    private suspend fun ReceiveTurbine<AutolockIntervalState>.initialStateEmitted() {
        awaitItem() as AutolockIntervalState.Loading
    }

    companion object {

        val uiIntervals = mapOf(
            Immediately to TextUiModel(R.string.mail_pinlock_settings_autolock_immediately),
            FiveMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_five_minutes),
            FifteenMinutes to TextUiModel(R.string.mail_pinlock_settings_autolock_description_fifteen_minutes),
            OneHour to TextUiModel(R.string.mail_pinlock_settings_autolock_description_one_hour),
            OneDay to TextUiModel(R.string.mail_pinlock_settings_autolock_description_one_day)
        )
    }
}
