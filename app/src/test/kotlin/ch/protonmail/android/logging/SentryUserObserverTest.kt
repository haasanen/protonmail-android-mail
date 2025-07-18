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

package ch.protonmail.android.logging

import java.util.UUID
import ch.protonmail.android.mailsession.domain.model.UserSettings
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SentryUserObserverTest {

    private val sessionRepository = mockk<UserSessionRepository> {
        coEvery { this@mockk.observePrimaryUserId() } returns flowOf(UserIdTestData.userId)
    }

    private lateinit var sentryUserObserver: SentryUserObserver

    @Before
    fun setUp() {
        Dispatchers.setMain(TestDispatcherProvider().Main)
        mockkStatic(Sentry::class)
        sentryUserObserver = SentryUserObserver(
            scopeProvider = TestCoroutineScopeProvider(),
            sessionRepository = sessionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Sentry::class)
    }

    @Test
    fun `register userId in Sentry for valid primary account`() = runTest {
        // When
        sentryUserObserver.start(onCrashReportSettingChange = {}).join()
        // Then
        val sentryUserSlot = slot<User>()
        verify { Sentry.setUser(capture(sentryUserSlot)) }
        assertEquals(UserIdTestData.userId.id, sentryUserSlot.captured.id)
    }

    @Test
    fun `register random UUID in Sentry when no primary account available`() = runTest {
        // Given
        every { sessionRepository.observePrimaryUserId() } returns flowOf(null)
        // When
        sentryUserObserver.start(onCrashReportSettingChange = {}).join()
        // Then
        val sentryUserSlot = slot<User>()
        verify { Sentry.setUser(capture(sentryUserSlot)) }
        val actual = UUID.fromString(sentryUserSlot.captured.id)
        assertTrue(actual.toString().isNotBlank())
    }

    @Test
    fun `crash reports user setting is disabled`() = runTest {
        // Given
        var isEnabled: Boolean? = null
        coEvery { sessionRepository.getUserSettings(any()) } returns mockk<UserSettings> {
            every { crashReports } returns false
        }
        // When
        sentryUserObserver.start(onCrashReportSettingChange = { isEnabled = it }).join()
        // Then
        assertEquals(false, isEnabled)
    }

    @Test
    fun `crash reports are enabled when no primary account available`() = runTest {
        // Given
        var isEnabled: Boolean? = null
        every { sessionRepository.observePrimaryUserId() } returns flowOf(null)
        // When
        sentryUserObserver.start(onCrashReportSettingChange = { isEnabled = it }).join()
        // Then
        assertEquals(true, isEnabled)
    }
}
