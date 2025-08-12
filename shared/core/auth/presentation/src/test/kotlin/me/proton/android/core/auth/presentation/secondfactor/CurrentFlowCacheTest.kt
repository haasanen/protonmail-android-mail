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

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.android.core.auth.presentation.flow.FlowCache
import me.proton.android.core.auth.presentation.flow.FlowManager
import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_account_uniffi.PasswordFlow
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.StoredSession
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CurrentFlowCacheTest {

    private lateinit var mailSession: MailSession
    private lateinit var cache: FlowCache

    @BeforeTest
    fun setup() {
        mailSession = mockk(relaxed = true)
        cache = FlowCache(mailSession)
    }

    @Test
    fun getActiveFlow_shouldReturnNull_whenNotSet() {
        val result = cache.getActiveFlow()
        assertNull(result)
    }

    @Test
    fun getCachedSession_shouldReturnNull_whenNotSet() {
        val result = cache.getCachedSession()
        assertNull(result)
    }

    @Test
    fun clear_shouldNotCallDeleteAccount_ifFlowIsChangingPassword() = runTest {
        val flow = mockk<PasswordFlow>()

        cache.setActiveFlow(FlowManager.CurrentFlow.ChangingPassword(flow), shouldClearOnNext = true)
        cache.setCachedSession(mockk(), "user123")

        val cleared = cache.clear()

        assertTrue(cleared)
        coVerify(exactly = 0) { mailSession.deleteAccount(any()) }
    }

    @Test
    fun clearIfUserChanged_shouldDoNothing_whenCachedUserIdIsNull() = runTest {
        cache.clearIfUserChanged("someUser")

        coVerify(exactly = 0) { mailSession.deleteAccount(any()) }
    }

    @Test
    fun setActiveFlow_shouldStoreFlowAndFlag() {
        val flow = FlowManager.CurrentFlow.LoggingIn(mockk())

        cache.setActiveFlow(flow, shouldClearOnNext = false)

        assertEquals(flow, cache.getActiveFlow())
    }

    @Test
    fun setCachedSession_shouldStoreSessionAndUserId() {
        val session = mockk<StoredSession>()
        val userId = "user123"

        cache.setCachedSession(session, userId)

        assertEquals(session, cache.getCachedSession())
    }

    @Test
    fun clear_withLoggingIn_shouldClearAllAndCallDeleteAccount() = runTest {
        val flow = mockk<LoginFlow>()
        val session = mockk<StoredSession>()

        cache.setActiveFlow(FlowManager.CurrentFlow.LoggingIn(flow), shouldClearOnNext = true)
        cache.setCachedSession(session, "user123")

        val cleared = cache.clear()

        assertTrue(cleared)
        assertNull(cache.getActiveFlow())
        assertNull(cache.getCachedSession())
        coVerify { mailSession.deleteAccount("user123") }
    }

    @Test
    fun clear_shouldNotClearIfShouldClearOnNextIsFalseAndForceIsFalse() = runTest {
        val flow = mockk<PasswordFlow>()

        cache.setActiveFlow(FlowManager.CurrentFlow.ChangingPassword(flow), shouldClearOnNext = false)

        val cleared = cache.clear()

        assertFalse(cleared)
        assertNotNull(cache.getActiveFlow())
    }

    @Test
    fun clear_shouldForceClearRegardlessOfShouldClearOnNext() = runTest {
        val flow = mockk<PasswordFlow>()
        val session = mockk<StoredSession>()

        cache.setActiveFlow(FlowManager.CurrentFlow.ChangingPassword(flow), shouldClearOnNext = false)
        cache.setCachedSession(session, "user123")

        val cleared = cache.clear(force = true)

        assertTrue(cleared)
        assertNull(cache.getActiveFlow())
    }

    @Test
    fun clearIfUserChanged_shouldClearOnlyIfUserChanged() = runTest {
        cache.setCachedSession(mockk(), "user123")
        cache.setActiveFlow(FlowManager.CurrentFlow.LoggingIn(mockk()), shouldClearOnNext = true)

        cache.clearIfUserChanged("otherUser")

        coVerify { mailSession.deleteAccount("user123") }
        assertNull(cache.getCachedSession())
        assertNull(cache.getActiveFlow())
    }

    @Test
    fun clearIfUserChanged_shouldNotClearIfUserIsSame() = runTest {
        val session = mockk<StoredSession>()
        val flow = mockk<PasswordFlow>()

        cache.setCachedSession(session, "user123")
        cache.setActiveFlow(FlowManager.CurrentFlow.ChangingPassword(flow), shouldClearOnNext = true)

        cache.clearIfUserChanged("user123")

        assertEquals(session, cache.getCachedSession())
        assertEquals(flow, (cache.getActiveFlow() as? FlowManager.CurrentFlow.ChangingPassword)?.flow)
        coVerify(exactly = 0) { mailSession.deleteAccount(any()) }
    }
}
