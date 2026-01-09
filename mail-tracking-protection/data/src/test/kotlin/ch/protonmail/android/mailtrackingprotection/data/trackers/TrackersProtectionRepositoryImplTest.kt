/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.data.trackers

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.mailtrackingprotection.domain.model.BlockedTracker
import ch.protonmail.android.mailtrackingprotection.domain.repository.TrackersProtectionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.TrackerDomain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TrackersProtectionRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TrackersProtectionRepository

    private val mockUserSessionRepository = mockk<UserSessionRepository>()
    private val mockDataSource = mockk<RustTrackersDataSource>()
    private val mockUserSession = mockk<MailUserSessionWrapper>(relaxed = true)
    private val mockRustSession = mockk<MailUserSession>()

    private val testUserId = UserId("test-user-id")
    private val testMessageId = MessageId("123")

    @BeforeTest
    fun setup() {
        repository = TrackersProtectionRepositoryImpl(
            userSessionRepository = mockUserSessionRepository,
            dataSource = mockDataSource
        )

        every { mockUserSession.getRustUserSession() } returns mockRustSession
    }

    @Test
    fun `observe trackers returns flow of blocked trackers successfully`() = runTest {
        // Given
        val trackerDomains = listOf(
            TrackerDomain("tracker1.com", listOf("https://tracker1.com/pixel")),
            TrackerDomain("tracker2.com", listOf("https://tracker2.com/pixel"))
        )
        val expectedTrackers = listOf(
            BlockedTracker("tracker1.com", listOf("https://tracker1.com/pixel")),
            BlockedTracker("tracker2.com", listOf("https://tracker2.com/pixel"))
        )

        coEvery { mockUserSessionRepository.getUserSession(testUserId) } returns mockUserSession
        every {
            mockDataSource.observeTrackers(any(), any())
        } returns flowOf(trackerDomains.right())

        // When/Then
        repository.observeTrackersForMessage(testUserId, testMessageId).test {
            val blockedTrackers = awaitItem()

            // Then
            assertTrue(blockedTrackers.isRight())
            val trackers = blockedTrackers.getOrNull()!!
            assertEquals(expectedTrackers, trackers)
            awaitComplete()
        }
    }

    @Test
    fun `observe trackers returns empty list when no trackers found`() = runTest {
        // Given
        coEvery { mockUserSessionRepository.getUserSession(testUserId) } returns mockUserSession
        every {
            mockDataSource.observeTrackers(any(), any())
        } returns flowOf(emptyList<TrackerDomain>().right())

        // When/Then
        repository.observeTrackersForMessage(testUserId, testMessageId).test {
            val blockedTrackers = awaitItem()

            // Then
            assertTrue(blockedTrackers is Either.Right<List<BlockedTracker>>)
            assertTrue(blockedTrackers.value.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `observe trackers returns flow of error when user session is null`() = runTest {
        // Given
        coEvery { mockUserSessionRepository.getUserSession(testUserId) } returns null

        // When/Then
        repository.observeTrackersForMessage(testUserId, testMessageId).test {
            val result = awaitItem()

            // Then
            assertTrue(result.isLeft())
            assertEquals(DataError.Local.NoUserSession, result.swap().getOrNull())
            awaitComplete()
        }
    }

    @Test
    fun `observe trackers propagates data source error`() = runTest {
        // Given
        val expectedError = DataError.Remote.NoNetwork
        coEvery { mockUserSessionRepository.getUserSession(testUserId) } returns mockUserSession
        every {
            mockDataSource.observeTrackers(any(), any())
        } returns flowOf(expectedError.left())

        // When/Then
        repository.observeTrackersForMessage(testUserId, testMessageId).test {
            val result = awaitItem()

            // Then
            assertTrue(result.isLeft())
            assertEquals(expectedError, result.swap().getOrNull())
            awaitComplete()
        }
    }
}
