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

package me.proton.android.core.auth.presentation.secondfactor

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.account.domain.usecase.ObserveCoreAccount
import me.proton.android.core.auth.presentation.flow.FlowCache
import me.proton.android.core.auth.presentation.flow.FlowManager
import me.proton.android.core.auth.presentation.flow.FlowManager.CurrentFlow
import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_account_uniffi.PasswordFlow
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetAccountResult
import uniffi.proton_mail_uniffi.MailSessionGetAccountSessionsResult
import uniffi.proton_mail_uniffi.MailSessionResumeLoginFlowResult
import uniffi.proton_mail_uniffi.MailSessionUserSessionFromStoredSessionResult
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionNewPasswordChangeFlowResult
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import uniffi.proton_mail_uniffi.UserSessionError
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FlowManagerTest {

    private lateinit var sessionInterface: MailSession
    private lateinit var flowCache: FlowCache
    private lateinit var observeCoreAccount: ObserveCoreAccount
    private lateinit var manager: FlowManager

    @BeforeTest
    fun setup() {
        sessionInterface = mockk(relaxed = true)
        flowCache = mockk(relaxed = true)
        observeCoreAccount = mockk(relaxed = true)
        manager = FlowManager(sessionInterface, observeCoreAccount, flowCache)
    }

    @Test
    fun getCurrentActiveFlow_returnsCachedFlow() = runTest {
        val flow = CurrentFlow.LoggingIn(mockk())
        coEvery { flowCache.clearIfUserChanged("user1") } just Runs
        every { flowCache.getActiveFlow() } returns flow

        val result = manager.getCurrentActiveFlow(CoreUserId("user1"))

        assertEquals(flow, result)
    }

    @Test
    fun getCurrentActiveFlow_resumesLoginFlowIfNoCache() = runTest {
        val session = mockk<StoredSession> {
            every { sessionId() } returns "session-id"
        }
        val resumedFlow = mockk<LoginFlow>()

        coEvery { flowCache.clearIfUserChanged("user1") } just Runs
        every { flowCache.getActiveFlow() } returns null
        every { flowCache.getCachedSession() } returns session
        coEvery { sessionInterface.resumeLoginFlow("user1", "session-id") } returns
            MailSessionResumeLoginFlowResult.Ok(resumedFlow)
        every { flowCache.setActiveFlow(any(), true) } just Runs

        val result = manager.getCurrentActiveFlow(CoreUserId("user1"))

        assertTrue(result is CurrentFlow.LoggingIn)
    }

    @Test
    fun getCurrentActiveFlow_returnsNullIfResumeFails() = runTest {
        val session = mockk<StoredSession> {
            every { sessionId() } returns "session-id"
        }

        coEvery { flowCache.clearIfUserChanged("user1") } just Runs
        every { flowCache.getActiveFlow() } returns null
        every { flowCache.getCachedSession() } returns session
        coEvery { sessionInterface.resumeLoginFlow("user1", "session-id") } returns
            MailSessionResumeLoginFlowResult.Error(mockk())

        val result = manager.getCurrentActiveFlow(CoreUserId("user1"))

        assertNull(result)
    }

    @Test
    fun tryCreatePasswordFlow_returnsCachedFlow() = runTest {
        val flow = mockk<PasswordFlow>()
        every { flowCache.getActiveFlow() } returns CurrentFlow.ChangingPassword(flow)
        every { flowCache.setActiveFlow(any(), false) } just Runs

        val result = manager.getCurrentActiveFlow(CoreUserId("user1"))

        assertTrue(result is CurrentFlow.ChangingPassword)
        assertEquals(flow, result.flow)
    }

    @Test
    fun tryCreatePasswordFlow_throwsOnUserContextError() = runTest {
        val session = mockk<StoredSession>()

        every { flowCache.getActiveFlow() } returns null
        every { flowCache.getCachedSession() } returns session
        coEvery { sessionInterface.userSessionFromStoredSession(session) } returns
            MailSessionUserSessionFromStoredSessionResult.Error(mockk())

        assertFailsWith<FlowManager.SessionException> {
            manager.getCurrentActiveFlow(CoreUserId("user1"))
        }
    }

    @Test
    fun tryCreatePasswordFlow_throwsOnFlowCreationError() = runTest {
        val session = mockk<StoredSession>()
        val userSession = mockk<MailUserSession>()

        every { flowCache.getActiveFlow() } returns null
        every { flowCache.getCachedSession() } returns session
        coEvery { sessionInterface.userSessionFromStoredSession(session) } returns
            MailSessionUserSessionFromStoredSessionResult.Ok(userSession)
        coEvery { userSession.newPasswordChangeFlow() } returns
            MailUserSessionNewPasswordChangeFlowResult.Error(mockk())

        assertFailsWith<FlowManager.SessionException> {
            manager.getCurrentActiveFlow(CoreUserId("user1"))
        }
    }

    @Test
    fun tryCreatePasswordFlow_returnsNewFlow() = runTest {
        val session = mockk<StoredSession>()
        val userSession = mockk<MailUserSession>()
        val flow = mockk<PasswordFlow>()

        every { flowCache.getActiveFlow() } returns null
        every { flowCache.getCachedSession() } returns session
        coEvery { sessionInterface.userSessionFromStoredSession(session) } returns
            MailSessionUserSessionFromStoredSessionResult.Ok(userSession)
        coEvery { userSession.newPasswordChangeFlow() } returns
            MailUserSessionNewPasswordChangeFlowResult.Ok(flow)
        every { flowCache.setActiveFlow(any(), false) } just Runs

        val result = manager.getCurrentActiveFlow(CoreUserId("user1"))

        assertTrue(result is CurrentFlow.ChangingPassword)
        assertEquals(flow, result.flow)
    }

    @Test
    fun clearCache_delegatesToFlowCache() = runTest {
        coEvery { flowCache.clear(true) } returns true

        val result = manager.clearCache(force = true)

        assertTrue(result)
        coVerify { flowCache.clear(true) }
    }

    @Test
    fun getActiveSession_returnsCachedIfAvailable() = runTest {
        val userId = "user123"
        val mockSession = mockk<StoredSession> {
            every { sessionId() } returns "sessionId123"
        }

        every { flowCache.getCachedSession() } returns mockSession
        coEvery { flowCache.clearIfUserChanged(userId) } just Runs
        every { flowCache.getActiveFlow() } returns null
        coEvery {
            sessionInterface.resumeLoginFlow(userId, "sessionId123")
        } returns MailSessionResumeLoginFlowResult.Error(mockk(relaxed = true))

        val result = manager.getCurrentActiveFlow(CoreUserId(userId))

        coVerify(exactly = 0) { sessionInterface.getAccount(any()) }
        assertNull(result)
    }

    @Test
    fun getActiveSession_fetchesFromSessionInterface() = runTest {
        val account = mockk<StoredAccount> {
            every { userId() } returns "user1"
        }
        val session = mockk<StoredSession> {
            every { sessionId() } returns "id"
        }

        val getAccountResult = MailSessionGetAccountResult.Ok(account)
        val getSessionsResult = MailSessionGetAccountSessionsResult.Ok(listOf(session))

        every { flowCache.getCachedSession() } returns null
        coEvery { sessionInterface.getAccount("user1") } returns getAccountResult
        coEvery { sessionInterface.getAccountSessions(account) } returns getSessionsResult
        every { flowCache.setCachedSession(session, "user1") } just Runs
        coEvery { flowCache.clearIfUserChanged("user1") } just Runs
        every { flowCache.getActiveFlow() } returns null
        coEvery {
            sessionInterface.resumeLoginFlow("user1", "id")
        } returns MailSessionResumeLoginFlowResult.Error(mockk())

        val result = manager.getCurrentActiveFlow(CoreUserId("user1"))

        assertNull(result)
    }


    @Test
    fun getActiveSession_returnsNullIfNoSessions() = runTest {
        val account = mockk<StoredAccount>()

        val getAccountResult = MailSessionGetAccountResult.Ok(account)
        val getSessionsResult = MailSessionGetAccountSessionsResult.Error(mockk<UserSessionError>(relaxed = true))

        every { flowCache.getCachedSession() } returns null
        coEvery { sessionInterface.getAccount("user1") } returns getAccountResult
        coEvery { sessionInterface.getAccountSessions(account) } returns getSessionsResult
        coEvery { flowCache.clearIfUserChanged("user1") } just Runs
        every { flowCache.getActiveFlow() } returns null

        val result = manager.getCurrentActiveFlow(CoreUserId("user1"))

        assertNull(result)
    }
}
