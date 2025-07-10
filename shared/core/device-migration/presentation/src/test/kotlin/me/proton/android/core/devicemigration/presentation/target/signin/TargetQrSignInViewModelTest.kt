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

package me.proton.android.core.devicemigration.presentation.target.signin

import android.content.Context
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.devicemigration.presentation.origin.qr.QrBitmapGenerator
import me.proton.android.core.devicemigration.presentation.target.usecase.ObserveForkFromOriginDevice
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TargetQrSignInViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var observeForkFromOriginDevice: ObserveForkFromOriginDevice

    @MockK
    private lateinit var qrBitmapGenerator: QrBitmapGenerator

    private lateinit var tested: TargetQrSignInViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getString(any()) } returns "string-resource"
        coEvery { qrBitmapGenerator.invoke(any(), any(), any(), any()) } returns mockk()

        tested = TargetQrSignInViewModel(
            context = context,
            observeForkFromOriginDevice = observeForkFromOriginDevice,
            qrBitmapGenerator = qrBitmapGenerator
        )
    }

    @Test
    fun `happy path`() = coroutinesTest {
        // GIVEN
        val testUserId = CoreUserId("user-id")
        every { observeForkFromOriginDevice() } returns flowOf(
            ObserveForkFromOriginDevice.Result.Idle(
                errorMessage = null,
                qrCode = "qr-code"
            ),
            ObserveForkFromOriginDevice.Result.SuccessfullySignedIn(testUserId)
        )

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(TargetQrSignInState.Loading, awaitItem())
            assertEquals(
                TargetQrSignInState.Idle(
                    errorMessage = null,
                    qrCode = "qr-code",
                    generateBitmap = qrBitmapGenerator::invoke
                ),
                awaitItem()
            )

            val state = awaitItem()
            assertIs<TargetQrSignInState.SuccessfullySignedIn>(state)
            val signedInEvent = assertIs<TargetQrSignInEvent.SignedIn>(state.effect.peek())
            assertEquals(testUserId, signedInEvent.userId)
        }
    }

    @Test
    fun `error when observing fork code`() = coroutinesTest {
        // GIVEN
        every { observeForkFromOriginDevice() } returns flow {
            error("error")
        }

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(TargetQrSignInState.Loading, awaitItem())
            assertIs<TargetQrSignInState.Failure>(awaitItem())
        }
    }

    @Test
    fun `awaiting the fork`() = coroutinesTest {
        // GIVEN
        val testUserId = CoreUserId("user-id")
        every { observeForkFromOriginDevice() } returns flowOf(
            ObserveForkFromOriginDevice.Result.Idle( // 1
                errorMessage = null,
                qrCode = "qr-code"
            ),
            ObserveForkFromOriginDevice.Result.Idle( // 2
                errorMessage = "no connection",
                qrCode = "qr-code"
            ),
            ObserveForkFromOriginDevice.Result.SuccessfullySignedIn(testUserId) // 3
        )

        // WHEN
        tested.state.test {
            // THEN
            assertIs<TargetQrSignInState.Loading>(awaitItem())
            assertIs<TargetQrSignInState.Idle>(awaitItem()) // 1

            val state2 = assertIs<TargetQrSignInState.Idle>(awaitItem()) // 2
            assertEquals("no connection", state2.errorMessage)

            val state = awaitItem() // 3
            assertIs<TargetQrSignInState.SuccessfullySignedIn>(state)
            val signedInEvent = assertIs<TargetQrSignInEvent.SignedIn>(state.effect.peek())
            assertEquals(testUserId, signedInEvent.userId)
        }
    }

    @Test
    fun `retrying the fork`() = coroutinesTest {
        // GIVEN
        val testUserId = CoreUserId("user-id")
        var pollCallCount = 0
        every { observeForkFromOriginDevice() } answers {
            pollCallCount += 1
            if (pollCallCount == 1) {
                flowOf(
                    ObserveForkFromOriginDevice.Result.Idle(
                        errorMessage = null,
                        qrCode = "qr-code"
                    ),
                    ObserveForkFromOriginDevice.Result.Error("error message")
                )
            } else {
                flowOf(
                    ObserveForkFromOriginDevice.Result.Idle(
                        errorMessage = null,
                        qrCode = "qr-code"
                    ),
                    ObserveForkFromOriginDevice.Result.SuccessfullySignedIn(testUserId)
                )
            }
        }

        // WHEN
        tested.state.test {
            // THEN
            assertIs<TargetQrSignInState.Loading>(awaitItem())
            assertIs<TargetQrSignInState.Idle>(awaitItem())

            val state1 = assertIs<TargetQrSignInState.Failure>(awaitItem())
            assertEquals("error message", state1.message)

            // WHEN
            state1.onRetry?.invoke()

            // THEN
            assertIs<TargetQrSignInState.Loading>(awaitItem())
            assertIs<TargetQrSignInState.Idle>(awaitItem())

            val state = awaitItem()
            assertIs<TargetQrSignInState.SuccessfullySignedIn>(state)
            assertIs<TargetQrSignInEvent.SignedIn>(state.effect.peek())
        }
    }
}
