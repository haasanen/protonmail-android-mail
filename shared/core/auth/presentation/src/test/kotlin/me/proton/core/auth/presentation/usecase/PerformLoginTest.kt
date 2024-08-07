package me.proton.core.auth.presentation.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import uniffi.proton_mail_uniffi.LoginFlow
import uniffi.proton_mail_uniffi.MailSessionInterface
import uniffi.proton_mail_uniffi.MailUserSession
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PerformLoginTest {
    @MockK
    private lateinit var session: MailSessionInterface
    private lateinit var tested: PerformLogin

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = PerformLogin(session)
    }

    @AfterTest
    fun tearDown() {
        tested.close()
    }

    @Test
    fun login() = runTest {
        // GIVEN
        val userSession = mockk<MailUserSession>()
        val loginFlow = mockk<LoginFlow>(relaxed = true) {
            every { isAwaiting2fa() } returns false
            every { isLoggedIn() } returns true
            every { toUserContext() } returns userSession
        }
        coEvery { session.newLoginFlow() } returns loginFlow

        tested.state.test {
            // THEN
            assertIs<PerformLogin.State.Idle>(awaitItem())

            // WHEN
            tested(PerformLogin.Action.LogIn("tester", "pass"))

            // THEN
            coVerify { loginFlow.login("tester", "pass") }
            assertIs<PerformLogin.State.LoggingIn>(awaitItem())
            assertEquals(PerformLogin.State.LoggedIn(userSession), awaitItem())
        }
    }

    @Test
    fun `login with 2fa code`() = runTest {
        // GIVEN
        val userSession = mockk<MailUserSession>()
        val loginFlow = mockk<LoginFlow>(relaxed = true) {
            every { isAwaiting2fa() } returns true
            every { isLoggedIn() } returns false
            every { toUserContext() } returns userSession
            coEvery { submitTotp("code") } answers {
                every { isAwaiting2fa() } returns false
                every { isLoggedIn() } returns true
            }
        }
        coEvery { session.newLoginFlow() } returns loginFlow

        tested.state.test {
            // THEN
            assertIs<PerformLogin.State.Idle>(awaitItem())

            // WHEN
            tested(PerformLogin.Action.LogIn("tester", "pass"))

            // THEN
            assertIs<PerformLogin.State.LoggingIn>(awaitItem())
            assertIs<PerformLogin.State.Awaiting2fa>(awaitItem())

            // WHEN
            tested(PerformLogin.Action.SubmitTotp("code"))

            // THEN
            coVerify { loginFlow.submitTotp("code") }
            assertIs<PerformLogin.State.SubmittingTotp>(awaitItem())
            assertEquals(PerformLogin.State.LoggedIn(userSession), awaitItem())
        }
    }
}
