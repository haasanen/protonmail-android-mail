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

package me.proton.android.core.devicemigration.presentation.origin.intro

import android.content.Context
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import me.proton.android.core.devicemigration.presentation.origin.qr.QrScanOutput
import me.proton.android.core.devicemigration.presentation.origin.usecase.ForkSessionIntoTargetDevice
import me.proton.core.biometric.data.StrongAuthenticatorsResolver
import me.proton.core.biometric.domain.BiometricAuthErrorCode
import me.proton.core.biometric.domain.BiometricAuthResult
import me.proton.core.domain.entity.Product
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class OriginQrSignInViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var forkSessionIntoTargetDevice: ForkSessionIntoTargetDevice

    @MockK
    private lateinit var strongAuthenticatorsResolver: StrongAuthenticatorsResolver

    private lateinit var tested: OriginQrSignInViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = OriginQrSignInViewModel(
            context = context,
            forkSessionIntoTargetDevice = forkSessionIntoTargetDevice,
            product = Product.Mail,
            strongAuthenticatorsResolver = strongAuthenticatorsResolver
        )
    }

    @Test
    fun `starting the flow with biometrics available`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(OriginQrSignInAction.Start)

            // THEN
            val state = awaitItem()
            assertEquals(OriginQrSignInState.Loading, state.state)

            val event = assertIs<OriginQrSignInEvent.LaunchBiometricsCheck>(state.effect?.peek())
            assertSame(strongAuthenticatorsResolver, event.resolver)
        }
    }

    @Test
    fun `on biometrics auth result success`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(OriginQrSignInAction.OnBiometricAuthResult(BiometricAuthResult.Success))

            // THEN
            val state = awaitItem()
            assertEquals(OriginQrSignInState.Loading, state.state)
            assertEquals(OriginQrSignInEvent.LaunchQrScanner, state.effect?.peek())
        }
    }

    @Test
    fun `on biometrics auth result user cancelled`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(
                OriginQrSignInAction.OnBiometricAuthResult(
                    BiometricAuthResult.AuthError(BiometricAuthErrorCode.UserCanceled, "User cancelled")
                )
            )

            // THEN
            expectNoEvents() // no state changes - state is Idle
        }
    }

    @Test
    fun `on biometrics auth result error`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(
                OriginQrSignInAction.OnBiometricAuthResult(
                    BiometricAuthResult.AuthError(BiometricAuthErrorCode.Lockout, "Locked out")
                )
            )

            // THEN
            val state = awaitItem()
            assertEquals(OriginQrSignInState.Idle, state.state)
            assertEquals(OriginQrSignInEvent.ErrorMessage("Locked out"), state.effect?.peek())
        }
    }

    @Test
    fun `on qr scan result success`() = coroutinesTest {
        // GIVEN
        coEvery { forkSessionIntoTargetDevice(any()) } returns ForkSessionIntoTargetDevice.Result.Success

        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(OriginQrSignInAction.OnQrScanResult(QrScanOutput.Success("code")))

            // THEN
            assertEquals(
                OriginQrSignInStateHolder(state = OriginQrSignInState.Verifying),
                awaitItem()
            )

            val finalState = awaitItem()
            assertEquals(OriginQrSignInEvent.SignedInSuccessfully, finalState.effect?.peek())
            assertEquals(OriginQrSignInState.SignedInSuccessfully, finalState.state)
        }
    }

    @Test
    fun `on qr scan result cancelled`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(OriginQrSignInAction.OnQrScanResult(QrScanOutput.Cancelled()))

            // THEN
            expectNoEvents() // no state changes - state is Idle
        }
    }

    @Test
    fun `on qr scan result manual input requested`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(OriginQrSignInAction.OnQrScanResult(QrScanOutput.ManualInputRequested()))

            // THEN
            val state = awaitItem()
            assertEquals(OriginQrSignInState.Idle, state.state)
            assertEquals(OriginQrSignInEvent.LaunchManualCodeInput, state.effect?.peek())
        }
    }

    @Test
    fun `on qr scan result success but pushing fork throws error`() = coroutinesTest {
        // GIVEN
        val errorMsg = "API error"
        coEvery { forkSessionIntoTargetDevice(any()) } returns ForkSessionIntoTargetDevice.Result.Error(errorMsg)

        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(OriginQrSignInAction.OnQrScanResult(QrScanOutput.Success("code")))

            // THEN
            assertEquals(
                OriginQrSignInStateHolder(state = OriginQrSignInState.Verifying),
                awaitItem()
            )

            val finalState = awaitItem()
            val finalEvent = assertIs<OriginQrSignInEvent.ErrorMessage>(finalState.effect?.peek())
            assertEquals(errorMsg, finalEvent.message)
            assertEquals(OriginQrSignInState.Idle, finalState.state)
        }
    }

    private suspend fun ReceiveTurbine<OriginQrSignInStateHolder>.assertInitialState() {
        assertEquals(
            OriginQrSignInStateHolder(state = OriginQrSignInState.Loading),
            awaitItem()
        )
        assertEquals(
            OriginQrSignInStateHolder(state = OriginQrSignInState.Idle),
            awaitItem()
        )
    }
}
