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

package ch.protonmail.android.initializer.prefetch

import androidx.lifecycle.LifecycleOwner
import ch.protonmail.android.mailmailbox.domain.usecase.PrefetchDataForUser
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.AccountTestData
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test

internal class DataPrefetchLifecycleObserverTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val prefetchDataForUser = mockk<PrefetchDataForUser>()

    private val observer = DataPrefetchLifecycleObserver(
        userSessionRepository,
        prefetchDataForUser,
        testCoroutineScope
    )

    private val lifecycleOwner = mockk<LifecycleOwner>()

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should call prefetch when the primary account is in ready state`() = runTest {
        // Given
        val account = AccountTestData.readyAccount
        val userId = account.userId
        val mockedSession = mockk<MailUserSessionWrapper>()
        every { userSessionRepository.observePrimaryAccount() } returns flowOf(account)
        coEvery { userSessionRepository.getUserSession(userId) } returns mockedSession
        coEvery { prefetchDataForUser.invoke(mockedSession) } just runs

        // When
        observer.onResume(lifecycleOwner)

        // Then
        coVerify(exactly = 1) { prefetchDataForUser(mockedSession) }
        confirmVerified(prefetchDataForUser)
    }

    @Test
    fun `should not call prefetch when the primary account is not in a ready state`() = runTest {
        // Given
        val account = AccountTestData.notReadyAccount
        every { userSessionRepository.observePrimaryAccount() } returns flowOf(account)

        // When
        observer.onResume(lifecycleOwner)

        // Then
        coVerify { prefetchDataForUser wasNot called }
    }
}
