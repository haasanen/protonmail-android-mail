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
import ch.protonmail.android.testdata.label.rust.LocalLabelTestData
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
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
            every { userSessionRepository.observeCurrentUserSession() } returns flowOf(null)

            // When
            labelDataSource.observeSystemLabels().test {
                // Then
                loggingTestRule.assertErrorLogged("rustLib: system labels: trying to load messages with a null session")
                expectNoEvents()
            }
        }

    @Test
    fun `observe system labels emits items when returned by the rust library`() = runTest {
        // Given
        val expected = listOf(LocalLabelTestData.localSystemLabelWithCount)
        val systemLabelsCallbackSlot = slot<MailboxLiveQueryUpdatedCallback>()
        val userSessionMock = mockk<MailUserSession> {
            val liveQueryMock = mockk<MailLabelsLiveQuery> {
                every { value() } returns expected
            }
            every { this@mockk.newSystemLabelsObservedQuery(capture(systemLabelsCallbackSlot)) } returns liveQueryMock
        }
        every { userSessionRepository.observeCurrentUserSession() } returns flowOf(userSessionMock)

        labelDataSource.observeSystemLabels().test {
            // When
            systemLabelsCallbackSlot.captured.onUpdated()

            // Then
            assertEquals(expected, awaitItem())
        }

    }
}
