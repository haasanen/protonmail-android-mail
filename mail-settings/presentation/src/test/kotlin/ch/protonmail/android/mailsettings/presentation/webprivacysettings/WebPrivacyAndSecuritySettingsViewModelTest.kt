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

package ch.protonmail.android.mailsettings.presentation.webprivacysettings

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.model.SessionError
import ch.protonmail.android.mailsession.domain.usecase.ForkSession
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.domain.model.WebSettingsConfig
import ch.protonmail.android.mailsettings.domain.repository.ThemeRepository
import ch.protonmail.android.mailsettings.domain.usecase.ObserveWebSettingsConfig
import ch.protonmail.android.mailsettings.presentation.websettings.WebSettingsState
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebPrivacyAndSecuritySettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val forkedSessionId = ForkedSessionId("test-selector-id")
    private val testTheme = Theme.DARK
    private val testWebSettingsConfig = WebSettingsConfig(
        baseUrl = "https://test.com",
        accountSettingsAction = "account-settings",
        emailSettingsAction = "email-settings",
        labelSettingsAction = "label-settings",
        spamFilterSettingsAction = "spam-settings",
        privacySecuritySettingsAction = "privacy-settings",
        subscriptionDetailsAction = "subscription"
    )

    private val primaryUserId = UserIdTestData.Primary

    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val forkSession = mockk<ForkSession> {
        coEvery { this@mockk(primaryUserId) } returns forkedSessionId.right()
    }
    private val themeRepository = mockk<ThemeRepository>()
    private val observeWebSettingsConfig = mockk<ObserveWebSettingsConfig> {
        every { this@mockk.invoke() } returns flowOf(testWebSettingsConfig)
    }

    @Test
    fun `emits loading state when initialized`() = runTest {
        // Given
        every { observePrimaryUserId.invoke() } returns flowOf(null)
        every { themeRepository.observe() } returns flowOf(testTheme)
        val viewModel =
            WebPrivacyAndSecuritySettingsViewModel(
                observePrimaryUserId = observePrimaryUserId,
                forkSession = forkSession,
                themeRepository = themeRepository,
                observeWebSettingsConfig = observeWebSettingsConfig
            )

        // When & Then
        viewModel.state.test {
            assertEquals(WebSettingsState.Loading, awaitItem())
        }
    }

    @Test
    fun `emits Data state when valid data is provided`() = runTest {
        // Given
        every { observePrimaryUserId.invoke() } returns flowOf(primaryUserId)
        every { themeRepository.observe() } returns flowOf(testTheme)
        val viewModel =
            WebPrivacyAndSecuritySettingsViewModel(
                observePrimaryUserId = observePrimaryUserId,
                forkSession = forkSession,
                themeRepository = themeRepository,
                observeWebSettingsConfig = observeWebSettingsConfig
            )

        // When
        viewModel.state.test {

            // Then
            val actualState = awaitItem() as WebSettingsState.Data
            assertEquals(testTheme, actualState.theme)
        }
    }

    @Test
    fun `emits Error state when user session fork fails`() = runTest {
        // Given
        every { observePrimaryUserId.invoke() } returns flowOf(primaryUserId)
        every { themeRepository.observe() } returns flowOf(testTheme)
        coEvery {
            forkSession(primaryUserId)
        } returns SessionError.Local.KeyChainError.left()
        val viewModel =
            WebPrivacyAndSecuritySettingsViewModel(
                observePrimaryUserId = observePrimaryUserId,
                forkSession = forkSession,
                themeRepository = themeRepository,
                observeWebSettingsConfig = observeWebSettingsConfig
            )

        // When
        viewModel.state.test {

            // Then
            assertTrue(awaitItem() is WebSettingsState.Error)
        }
    }
}
