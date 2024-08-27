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
import ch.protonmail.android.maillabel.data.usecase.CreateRustSidebar
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
import uniffi.proton_mail_uniffi.LabelType
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Sidebar
import uniffi.proton_mail_uniffi.WatchHandle
import kotlin.test.assertEquals

class RustLabelDataSourceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val loggingTestRule = LoggingTestRule()

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)
    private val createRustSidebar = mockk<CreateRustSidebar>()

    private val labelDataSource = RustLabelDataSource(
        userSessionRepository,
        createRustSidebar,
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
        val systemCallbackSlot = slot<LiveQueryCallback>()
        val userSessionMock = mockk<MailUserSession>()
        val labelsWatcherMock = mockk<WatchHandle>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock
        val sidebarMock = mockk<Sidebar> {
            coEvery { this@mockk.systemLabels() } returns expected
            coEvery { this@mockk.watchLabels(LabelType.SYSTEM, capture(systemCallbackSlot)) } returns labelsWatcherMock
        }
        every { createRustSidebar(userSessionMock) } returns sidebarMock

        labelDataSource.observeSystemLabels(userId).test {
            // When
            systemCallbackSlot.captured.onUpdate()

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
        val watcherMock = mockk<WatchHandle> {
            every { disconnect() } just Runs
        }
        val firstUserSessionMock = mockk<MailUserSession>()
        val secondUserSessionMock = mockk<MailUserSession>()
        val sidebarMock = mockk<Sidebar> {
            coEvery { this@mockk.systemLabels() } returns expected
            coEvery { this@mockk.watchLabels(LabelType.SYSTEM, any()) } returns watcherMock
        }
        every { createRustSidebar(firstUserSessionMock) } returns sidebarMock
        every { createRustSidebar(secondUserSessionMock) } returns sidebarMock
        coEvery { userSessionRepository.getUserSession(firstUserId) } returns firstUserSessionMock
        coEvery { userSessionRepository.getUserSession(secondUserId) } returns secondUserSessionMock

        labelDataSource.observeSystemLabels(firstUserId)
        // When
        labelDataSource.observeSystemLabels(secondUserId)
        // Then
        verify { watcherMock.disconnect() }
        coVerify { createRustSidebar(firstUserSessionMock) }
        coVerify { createRustSidebar(secondUserSessionMock) }
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
        val messageLabelsCallbackSlot = slot<LiveQueryCallback>()
        val userSessionMock = mockk<MailUserSession>()
        val labelsWatcherMock = mockk<WatchHandle>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock
        val sidebarMock = mockk<Sidebar> {
            coEvery { this@mockk.customLabels() } returns expected
            coEvery {
                this@mockk.watchLabels(
                    LabelType.LABEL,
                    capture(messageLabelsCallbackSlot)
                )
            } returns labelsWatcherMock
        }
        every { createRustSidebar(userSessionMock) } returns sidebarMock


        labelDataSource.observeMessageLabels(userId).test {
            // When
            messageLabelsCallbackSlot.captured.onUpdate()

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
        val watcherMock = mockk<WatchHandle> {
            every { disconnect() } just Runs
        }
        val firstUserSessionMock = mockk<MailUserSession>()
        val secondUserSessionMock = mockk<MailUserSession>()
        val sidebarMock = mockk<Sidebar> {
            coEvery { this@mockk.customLabels() } returns expected
            coEvery { this@mockk.watchLabels(LabelType.LABEL, any()) } returns watcherMock
        }
        every { createRustSidebar(firstUserSessionMock) } returns sidebarMock
        every { createRustSidebar(secondUserSessionMock) } returns sidebarMock
        coEvery { userSessionRepository.getUserSession(firstUserId) } returns firstUserSessionMock
        coEvery { userSessionRepository.getUserSession(secondUserId) } returns secondUserSessionMock

        labelDataSource.observeMessageLabels(firstUserId)
        // When
        labelDataSource.observeMessageLabels(secondUserId)
        // Then
        verify { watcherMock.disconnect() }
        coVerify { createRustSidebar(firstUserSessionMock) }
        coVerify { createRustSidebar(secondUserSessionMock) }
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
        val messageFoldersCallbackSlot = slot<LiveQueryCallback>()
        val userSessionMock = mockk<MailUserSession>()
        val labelsWatcherMock = mockk<WatchHandle>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock
        val sidebarMock = mockk<Sidebar> {
            coEvery { this@mockk.allCustomFolders() } returns expected
            coEvery {
                this@mockk.watchLabels(LabelType.FOLDER, capture(messageFoldersCallbackSlot))
            } returns labelsWatcherMock
        }
        every { createRustSidebar(userSessionMock) } returns sidebarMock


        labelDataSource.observeMessageFolders(userId).test {
            // When
            messageFoldersCallbackSlot.captured.onUpdate()

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
        val watcherMock = mockk<WatchHandle> {
            every { disconnect() } just Runs
        }
        val firstUserSessionMock = mockk<MailUserSession>()
        val secondUserSessionMock = mockk<MailUserSession>()
        val sidebarMock = mockk<Sidebar> {
            coEvery { this@mockk.allCustomFolders() } returns expected
            coEvery { this@mockk.watchLabels(LabelType.FOLDER, any()) } returns watcherMock
        }
        every { createRustSidebar(firstUserSessionMock) } returns sidebarMock
        every { createRustSidebar(secondUserSessionMock) } returns sidebarMock
        coEvery { userSessionRepository.getUserSession(firstUserId) } returns firstUserSessionMock
        coEvery { userSessionRepository.getUserSession(secondUserId) } returns secondUserSessionMock

        labelDataSource.observeMessageFolders(firstUserId)
        // When
        labelDataSource.observeMessageFolders(secondUserId)
        // Then
        verify { watcherMock.disconnect() }
        coVerify { createRustSidebar(firstUserSessionMock) }
        coVerify { createRustSidebar(secondUserSessionMock) }
    }
}
