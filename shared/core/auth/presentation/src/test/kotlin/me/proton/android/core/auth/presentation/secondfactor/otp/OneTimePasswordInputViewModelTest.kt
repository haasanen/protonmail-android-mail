package me.proton.android.core.auth.presentation.secondfactor.otp

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class OneTimePasswordInputViewModelTest {

    private lateinit var tested: OneTimePasswordInputViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        tested = OneTimePasswordInputViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @Ignore("Not implemented")
    fun `submitting one time password`() = runTest {
        tested.state.test {
            assertEquals(OneTimePasswordInputState.Idle, awaitItem())

            // WHEN
            tested.submit(OneTimePasswordInputAction.Authenticate("code", OneTimePasswordInputMode.Totp))

            // THEN
            assertEquals(OneTimePasswordInputState.Loading, awaitItem())
            assertEquals(OneTimePasswordInputState.Success, awaitItem())
        }
    }

    @Test
    fun `code is empty`() = runTest {
        tested.state.test {
            assertEquals(OneTimePasswordInputState.Idle, awaitItem())

            // WHEN
            tested.submit(OneTimePasswordInputAction.Authenticate("", OneTimePasswordInputMode.Totp))

            // THEN
            assertEquals(OneTimePasswordInputState.Loading, awaitItem())
            assertEquals(OneTimePasswordInputState.CodeIsEmpty, awaitItem())
        }
    }

    @Test
    fun `switching mode`() = runTest {
        tested.mode.test {
            assertEquals(OneTimePasswordInputMode.Totp, awaitItem())

            // WHEN
            tested.submit(OneTimePasswordInputAction.SwitchMode(OneTimePasswordInputMode.RecoveryCode))

            // THEN
            assertEquals(OneTimePasswordInputMode.RecoveryCode, awaitItem())
        }
    }
}
