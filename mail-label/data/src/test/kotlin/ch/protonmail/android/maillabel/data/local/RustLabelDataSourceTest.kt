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

package ch.protonmail.android.maillabel.data.local

import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.label.rust.LocalLabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.MailLabelsLiveQuery
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import kotlin.test.assertEquals

class RustLabelDataSourceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val loggingTestRule = LoggingTestRule()

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val labelDataSource = RustLabelDataSource(
        userSessionRepository,
        testCoroutineScope
    )

    @Test
    fun `observe system labels fails and logs error when session is invalid`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val userId = UserIdTestData.userId
            coEvery { userSessionRepository.getUserSession(userId) } returns null

            // When
            labelDataSource.observeSystemLabels(userId).test {
                // Then
                loggingTestRule.assertErrorLogged("rust-label: trying to load labels with a null session")
                expectNoEvents()
            }
        }

    @Test
    fun `observe system labels emits items when returned by the rust library`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expected = listOf(LocalLabelTestData.localSystemLabelWithCount)
        val systemLabelsCallbackSlot = slot<MailboxLiveQueryUpdatedCallback>()
        val userSessionMock = mockk<MailUserSession> {
            val liveQueryMock = mockk<MailLabelsLiveQuery> {
                every { value() } returns expected
            }
            every { this@mockk.newSystemLabelsObservedQuery(capture(systemLabelsCallbackSlot)) } returns liveQueryMock
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock

        labelDataSource.observeSystemLabels(userId).test {
            // When
            systemLabelsCallbackSlot.captured.onUpdated()

            // Then
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `re initializes system labels query when userId changes`() = runTest {
        // Given
        val firstUserId = UserIdTestData.userId
        val secondUserId = UserIdTestData.userId1
        val expected = listOf(LocalLabelTestData.localSystemLabelWithCount)
        val firstLiveQueryMock = mockk<MailLabelsLiveQuery> {
            every { value() } returns expected
            every { disconnect() } just Runs
        }
        val firstUserSessionMock = mockk<MailUserSession> {
            every { this@mockk.newSystemLabelsObservedQuery(any()) } returns firstLiveQueryMock
        }
        val secondUserSessionMock = mockk<MailUserSession> {
            val liveQueryMock = mockk<MailLabelsLiveQuery> {
                every { value() } returns expected
            }
            every { this@mockk.newSystemLabelsObservedQuery(any()) } returns liveQueryMock
        }
        coEvery { userSessionRepository.getUserSession(firstUserId) } returns firstUserSessionMock
        coEvery { userSessionRepository.getUserSession(secondUserId) } returns secondUserSessionMock

        labelDataSource.observeSystemLabels(firstUserId)
        // When
        labelDataSource.observeSystemLabels(secondUserId)
        // Then
        verify { firstLiveQueryMock.disconnect() }
        coVerify { userSessionRepository.getUserSession(firstUserId) }
        coVerify { userSessionRepository.getUserSession(secondUserId) }
    }

    @Test
    fun `observe message custom labels fails and logs error when session is invalid`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val userId = UserIdTestData.userId
            coEvery { userSessionRepository.getUserSession(userId) } returns null

            // When
            labelDataSource.observeMessageLabels(userId).test {
                // Then
                loggingTestRule.assertErrorLogged("rust-label: trying to load labels with a null session")
                expectNoEvents()
            }
        }

    @Test
    fun `observe message custom labels emits items when returned by the rust library`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expected = listOf(LocalLabelTestData.localMessageLabelWithCount)
        val messageLabelsCallbackSlot = slot<MailboxLiveQueryUpdatedCallback>()
        val userSessionMock = mockk<MailUserSession> {
            val liveQueryMock = mockk<MailLabelsLiveQuery> {
                every { value() } returns expected
            }
            every { this@mockk.newLabelLabelsObservedQuery(capture(messageLabelsCallbackSlot)) } returns liveQueryMock
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock

        labelDataSource.observeMessageLabels(userId).test {
            // When
            messageLabelsCallbackSlot.captured.onUpdated()

            // Then
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `re initializes message custom labels query when userId changes`() = runTest {
        // Given
        val firstUserId = UserIdTestData.userId
        val secondUserId = UserIdTestData.userId1
        val expected = listOf(LocalLabelTestData.localMessageLabelWithCount)
        val firstLiveQueryMock = mockk<MailLabelsLiveQuery> {
            every { value() } returns expected
            every { disconnect() } just Runs
        }
        val firstUserSessionMock = mockk<MailUserSession> {
            every { this@mockk.newLabelLabelsObservedQuery(any()) } returns firstLiveQueryMock
        }
        val secondUserSessionMock = mockk<MailUserSession> {
            val liveQueryMock = mockk<MailLabelsLiveQuery> {
                every { value() } returns expected
            }
            every { this@mockk.newLabelLabelsObservedQuery(any()) } returns liveQueryMock
        }
        coEvery { userSessionRepository.getUserSession(firstUserId) } returns firstUserSessionMock
        coEvery { userSessionRepository.getUserSession(secondUserId) } returns secondUserSessionMock

        labelDataSource.observeMessageLabels(firstUserId)
        // When
        labelDataSource.observeMessageLabels(secondUserId)
        // Then
        verify { firstLiveQueryMock.disconnect() }
        coVerify { userSessionRepository.getUserSession(firstUserId) }
        coVerify { userSessionRepository.getUserSession(secondUserId) }
    }

    @Test
    fun `observe message custom folders fails and logs error when session is invalid`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val userId = UserIdTestData.userId
            coEvery { userSessionRepository.getUserSession(userId) } returns null

            // When
            labelDataSource.observeMessageFolders(userId).test {
                // Then
                loggingTestRule.assertErrorLogged("rust-label: trying to load labels with a null session")
                expectNoEvents()
            }
        }

    @Test
    fun `observe message custom folders emits items when returned by the rust library`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expected = listOf(LocalLabelTestData.localMessageFolderWithCount)
        val messageFoldersCallbackSlot = slot<MailboxLiveQueryUpdatedCallback>()
        val userSessionMock = mockk<MailUserSession> {
            val liveQueryMock = mockk<MailLabelsLiveQuery> {
                every { value() } returns expected
            }
            every { this@mockk.newFolderLabelsObservedQuery(capture(messageFoldersCallbackSlot)) } returns liveQueryMock
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock

        labelDataSource.observeMessageFolders(userId).test {
            // When
            messageFoldersCallbackSlot.captured.onUpdated()

            // Then
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `re initializes message custom folders query when userId changes`() = runTest {
        // Given
        val firstUserId = UserIdTestData.userId
        val secondUserId = UserIdTestData.userId1
        val expected = listOf(LocalLabelTestData.localMessageFolderWithCount)
        val firstLiveQueryMock = mockk<MailLabelsLiveQuery> {
            every { value() } returns expected
            every { disconnect() } just Runs
        }
        val firstUserSessionMock = mockk<MailUserSession> {
            every { this@mockk.newFolderLabelsObservedQuery(any()) } returns firstLiveQueryMock
        }
        val secondUserSessionMock = mockk<MailUserSession> {
            val liveQueryMock = mockk<MailLabelsLiveQuery> {
                every { value() } returns expected
            }
            every { this@mockk.newFolderLabelsObservedQuery(any()) } returns liveQueryMock
        }
        coEvery { userSessionRepository.getUserSession(firstUserId) } returns firstUserSessionMock
        coEvery { userSessionRepository.getUserSession(secondUserId) } returns secondUserSessionMock

        labelDataSource.observeMessageFolders(firstUserId)
        // When
        labelDataSource.observeMessageFolders(secondUserId)
        // Then
        verify { firstLiveQueryMock.disconnect() }
        coVerify { userSessionRepository.getUserSession(firstUserId) }
        coVerify { userSessionRepository.getUserSession(secondUserId) }
    }
}
