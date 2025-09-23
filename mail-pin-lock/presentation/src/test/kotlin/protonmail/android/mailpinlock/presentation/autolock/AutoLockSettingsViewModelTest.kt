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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsUiState
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsViewAction
import ch.protonmail.android.mailpinlock.presentation.autolock.model.BiometricsOperationFollowUp
import ch.protonmail.android.mailpinlock.presentation.autolock.reducer.AutoLockSettingsReducer
import ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel.AutoLockSettingsViewModel
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AutoLockSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val autoLockRepository: AutoLockRepository = mockk {
        coEvery {
            this@mockk.updateAutoLockInterval(any())
        } returns Unit.right()

        coEvery {
            this@mockk.setBiometricProtection(any())
        } returns Unit.right()
    }

    private val autoLockSettingsReducer = AutoLockSettingsReducer()

    private val viewModel by lazy {
        AutoLockSettingsViewModel(
            autoLockRepository,
            autoLockSettingsReducer
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
        expectUnsetAutoLock()

        // When + Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertEquals(AutoLockSettingsUiState.Loading, loadingState)
        }
    }

    @Test
    fun `should return mapped data when flow emits value`() = runTest {
        // Given
        expectAutoLock(AutoLock())

        // When + Then
        viewModel.state.test {
            assertTrue { awaitItem() is AutoLockSettingsUiState.Data }
        }
    }

    @Test
    fun `RequestProtectionRemoval with Pin protection should emit PinRemovalRequested`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Pin))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestProtectionRemoval)

            val effects = awaitItem()
            assertEquals(Effect.of(Unit), effects.pinLockRemovalRequested)
        }
    }

    @Test
    fun `RequestProtectionRemoval with Biometrics protection should emit BiometricAuth request`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Biometrics))
        val expectedEffect = Effect.of(BiometricsOperationFollowUp.SetNone)

        // When + Then
        viewModel.effects.test {
            awaitItem()
            viewModel.submit(AutoLockSettingsViewAction.RequestProtectionRemoval)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.requestBiometricsAuth)
        }
    }

    @Test
    fun `RequestProtectionRemoval with None protection should not emit any effect`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.None))

        // When + Then
        viewModel.effects.test {
            val initialEffects = awaitItem()
            viewModel.submit(AutoLockSettingsViewAction.RequestProtectionRemoval)

            // Should not emit new effects since protection is already None
            expectNoEvents()
        }
    }

    @Test
    fun `RequestProtectionRemoval with unknown lock policy should emit error`() = runTest {
        // Given
        expectUnsetAutoLock()
        val expectedError = Effect.of(TextUiModel(R.string.mail_settings_biometrics_unknown_lock))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestProtectionRemoval)

            val effects = awaitItem()
            assertEquals(expectedError, effects.updateError)
        }
    }

    @Test
    fun `RequestPinProtection with None protection should emit PinCreationRequested`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.None))

        // When + THen
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestPinProtection)

            val effects = awaitItem()
            assertEquals(Effect.of(Unit), effects.openPinCreation)
        }
    }

    @Test
    fun `RequestPinProtection with existing Pin protection should emit nothing`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Pin))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestPinProtection)
            expectNoEvents()
        }
    }

    @Test
    fun `RequestPinProtection with Biometrics protection should emit BiometricAuth request`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Biometrics))
        val expectedEffect = Effect.of(BiometricsOperationFollowUp.SetPin)

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestPinProtection)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.requestBiometricsAuth)
        }
    }

    @Test
    fun `RequestPinProtection with unknown lock policy should emit error`() = runTest {
        // Given
        expectUnsetAutoLock()
        val expectedEffect = Effect.of(TextUiModel(R.string.mail_settings_biometrics_unknown_lock))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestPinProtection)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.updateError)
        }
    }

    @Test
    fun `RequestPinProtectionChange should emit PinChangeRequested`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Pin))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestPinProtectionChange)

            val effects = awaitItem()
            assertEquals(Effect.of(Unit), effects.pinLockChangeRequested)
        }
    }

    @Test
    fun `MigrateFromPinToBiometrics should emit PinLockToBiometricsRequested`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Pin))

        // When + Then
        viewModel.effects.test {
            awaitItem()
            viewModel.submit(AutoLockSettingsViewAction.MigrateFromPinToBiometrics)

            val effects = awaitItem()
            assertEquals(Effect.of(Unit), effects.pinLockToBiometricsRequested)
        }

        coVerify(exactly = 0) { autoLockRepository.setBiometricProtection(any()) }
    }

    @Test
    fun `RequestBiometricsProtection with None protection should emit BiometricAuth request`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.None))
        val expectedEffect = Effect.of(BiometricsOperationFollowUp.SetBiometrics)

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestBiometricsProtection)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.requestBiometricsAuth)
        }
    }

    @Test
    fun `RequestBiometricsProtection with Pin protection should emit PinLockToBiometricsRequested`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Pin))
        val expectedEffect = Effect.of(Unit)

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestBiometricsProtection)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.pinLockToBiometricsRequested)
        }
    }

    @Test
    fun `RequestBiometricsProtection with existing Biometrics protection should not emit any effect`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Biometrics))

        // When + Then
        viewModel.effects.test {
            awaitItem()
            viewModel.submit(AutoLockSettingsViewAction.RequestBiometricsProtection)

            // Should not emit new effects since biometrics is already active
            expectNoEvents()
        }
    }

    @Test
    fun `RequestBiometricsProtection with unknown lock policy should emit error`() = runTest {
        // Given
        expectUnsetAutoLock()
        val expectedEffect = Effect.of(TextUiModel(R.string.mail_settings_biometrics_unknown_lock))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RequestBiometricsProtection)

            // Then
            val effects = awaitItem()
            assertEquals(expectedEffect, effects.updateError)
        }
    }

    @Test
    fun `SetPinPreference with non-Pin protection should emit PinCreationRequested`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.None))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.SetPinPreference)

            val effects = awaitItem()
            assertEquals(Effect.of(Unit), effects.openPinCreation)
        }
    }

    @Test
    fun `SetPinPreference with existing Pin protection should not emit any effect`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Pin))

        // When + Then
        viewModel.effects.test {
            awaitItem()
            viewModel.submit(AutoLockSettingsViewAction.SetPinPreference)

            // Should not emit new effects since pin is already set
            expectNoEvents()
        }
    }

    @Test
    fun `SetPinPreference with unknown lock policy should emit error`() = runTest {
        // Given
        expectUnsetAutoLock()
        val expectedEffect = Effect.of(TextUiModel(R.string.mail_settings_biometrics_unknown_lock))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.SetPinPreference)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.updateError)
        }
    }

    @Test
    fun `SetBiometricsPreference with non-Biometrics protection should set biometric protection`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.None))

        // When
        viewModel.submit(AutoLockSettingsViewAction.SetBiometricsPreference)

        // Then
        coVerify { autoLockRepository.setBiometricProtection(true) }
    }

    @Test
    fun `SetBiometricsPreference with existing Biometrics protection should not call repository`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Biometrics))

        // When
        viewModel.submit(AutoLockSettingsViewAction.SetBiometricsPreference)

        // Then
        coVerify(exactly = 0) { autoLockRepository.setBiometricProtection(any()) }
    }

    @Test
    fun `SetBiometricsPreference should emit error when biometric setting fails`() = runTest {
        // Given
        coEvery { autoLockRepository.setBiometricProtection(true) } returns DataError.Local.CryptoError.left()
        expectAutoLock(AutoLock(protectionType = Protection.None))

        val expectedEffect = Effect.of(TextUiModel(R.string.mail_settings_biometrics_unable_to_set))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.SetBiometricsPreference)
            advanceUntilIdle()

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.updateError)
        }
    }

    @Test
    fun `SetBiometricsPreference with unknown lock policy should emit error`() = runTest {
        // Given
        expectUnsetAutoLock()
        val expectedEffect = Effect.of(TextUiModel(R.string.mail_settings_biometrics_unknown_lock))

        // When + Then
        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.SetBiometricsPreference)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.updateError)
        }
    }

    @Test
    fun `RemoveBiometricsProtection should disable biometric protection`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Biometrics))

        // When
        viewModel.submit(AutoLockSettingsViewAction.RemoveBiometricsProtection)

        // Then
        coVerify { autoLockRepository.setBiometricProtection(false) }
    }

    @Test
    fun `RemoveBiometricsProtection should emit error when biometric removal fails`() = runTest {
        // Given
        expectAutoLock(AutoLock(protectionType = Protection.Biometrics))
        coEvery { autoLockRepository.setBiometricProtection(false) } returns DataError.Local.CryptoError.left()
        val expectedEffect = Effect.of(TextUiModel(R.string.mail_settings_biometrics_unable_to_unset))

        viewModel.effects.test {
            awaitItem() // initial empty effects
            viewModel.submit(AutoLockSettingsViewAction.RemoveBiometricsProtection)

            val effects = awaitItem()
            assertEquals(expectedEffect, effects.updateError)
        }
    }

    private fun expectAutoLock(autoLock: AutoLock) {
        every { autoLockRepository.observeAppLock() } returns flowOf(autoLock)
    }

    private fun expectUnsetAutoLock() {
        every { autoLockRepository.observeAppLock() } returns flowOf()
    }
}
