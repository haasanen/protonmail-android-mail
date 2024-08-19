/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package me.proton.android.core.auth.presentation.login

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.android.core.auth.presentation.rules.MainDispatcherRule
import me.proton.android.core.auth.presentation.session.UserSessionInitializationCallback
import org.junit.Rule
import uniffi.proton_mail_uniffi.LoginFlow
import uniffi.proton_mail_uniffi.MailSessionInterface
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionInitializationStage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userSession = mockk<MailUserSession>(relaxed = true)
    private val loginFlow = mockk<LoginFlow>(relaxed = true) {
        every { isAwaiting2fa() } returns false
        every { isLoggedIn() } returns false
        every { toUserContext() } returns userSession
    }
    private var sessionInterface = mockk<MailSessionInterface> {
        coEvery { newLoginFlow() } returns loginFlow
    }
    private var callback = mockk<UserSessionInitializationCallback> {
        coEvery { waitFinished() } returns MailUserSessionInitializationStage.FINISHED
    }

    private lateinit var tested: LoginViewModel

    @BeforeTest
    fun setUp() {
        tested = LoginViewModel(sessionInterface, callback)
    }

    @AfterTest
    fun tearDown() {
        tested.submit(LoginAction.Close)
    }

    @Test
    fun login() = runTest {
        tested.state.test {
            // GIVEN
            every { loginFlow.isAwaiting2fa() } returns false
            every { loginFlow.isLoggedIn() } returns true

            // THEN
            assertIs<LoginViewState.Idle>(awaitItem())

            // WHEN
            tested.submit(LoginAction.Login("tester", "pass"))

            // THEN
            coVerify { loginFlow.login("tester", "pass") }
            //assertIs<LoginViewState.LoggingIn>(awaitItem())
            assertEquals(LoginViewState.LoggedIn(userSession), awaitItem())
        }
    }
}
