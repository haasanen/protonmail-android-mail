package me.proton.android.core.auth.presentation.twopass

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

class TwoPassInputViewModelTest {

    private lateinit var tested: TwoPassInputViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        tested = TwoPassInputViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @Ignore("Not yet implemented")
    fun `submitting mailbox password`() = runTest {
        tested.state.test {
            assertEquals(TwoPassInputState.Idle, awaitItem())

            // WHEN
            tested.submit(TwoPassInputAction.Unlock("mailbox-password"))

            // THEN
            assertEquals(TwoPassInputState.Loading, awaitItem())
            assertEquals(TwoPassInputState.Success, awaitItem())
        }
    }

    @Test
    fun `submitting empty mailbox password`() = runTest {
        tested.state.test {
            assertEquals(TwoPassInputState.Idle, awaitItem())

            // WHEN
            tested.submit(TwoPassInputAction.Unlock(""))

            // THEN
            assertEquals(TwoPassInputState.Loading, awaitItem())
            assertEquals(TwoPassInputState.PasswordIsEmpty, awaitItem())
        }
    }
}
