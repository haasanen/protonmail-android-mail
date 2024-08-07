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

package ch.protonmail.android.navigation

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.navigation.model.LauncherState
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlin.test.assertEquals
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test

@ExperimentalCoroutinesApi
class LauncherViewModelTest {


    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userSessionRepository: UserSessionRepository = mockk()

    private lateinit var viewModel: LauncherViewModel

    @Test
    fun `state should be AccountNeeded when userSession is not available`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            every { userSessionRepository.observeCurrentUserId() } returns flowOf(null)

            // When
            viewModel = LauncherViewModel(userSessionRepository)

            // Then
            viewModel.state.test {
                assertEquals(LauncherState.AccountNeeded, awaitItem())
            }
        }

    @Test
    fun `state should be PrimaryExist when userSession is available`() = runTest(mainDispatcherRule.testDispatcher) {
        // Given
        every { userSessionRepository.observeCurrentUserId() } returns flowOf(UserIdSample.Primary)

        // When
        viewModel = LauncherViewModel(userSessionRepository)

        // Then
        viewModel.state.test {
            assertEquals(LauncherState.PrimaryExist, awaitItem())
        }
    }

}
