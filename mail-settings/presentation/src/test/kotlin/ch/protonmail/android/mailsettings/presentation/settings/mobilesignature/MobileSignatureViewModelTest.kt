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

package ch.protonmail.android.mailsettings.presentation.settings.mobilesignature

import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.mailsettings.domain.model.MobileSignaturePreference
import ch.protonmail.android.mailsettings.domain.model.MobileSignatureStatus
import ch.protonmail.android.mailsettings.domain.repository.MobileSignatureRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.mapper.MobileSignatureUiModelMapper
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureState
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureViewAction
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.reducer.MobileSignatureReducer
import ch.protonmail.android.mailsettings.presentation.testdata.MobileSignatureTestData
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MobileSignatureViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userId = UserId("user-123")
    private val initialPreference = MobileSignatureTestData.PreferenceEnabled

    private val userSessionRepository = mockk<UserSessionRepository> {
        every { observePrimaryUserId() } returns flowOf(userId)
    }

    private val mobileSignatureRepository = mockk<MobileSignatureRepository>(relaxed = true).apply {
        every { observeMobileSignature(userId) } returns flowOf(initialPreference)
    }

    private val reducer = MobileSignatureReducer()

    @Test
    fun `loads and returns the initial signature preference`() = runTest {
        // Given
        val expected = MobileSignatureState.Data(MobileSignatureUiModelMapper.toSettingsUiModel(initialPreference))
        val viewModel = MobileSignatureViewModel(
            userSessionRepository = userSessionRepository,
            mobileSignatureRepository = mobileSignatureRepository,
            reducer = reducer
        )

        // When
        viewModel.state.test {
            val data = awaitItem()

            // Then
            assertEquals(expected, data)
        }
    }

    @Test
    fun `subsequent mobile signature repository emissions update state again`() = runTest {
        // Given
        val pref1 = MobileSignaturePreference("v1", MobileSignatureStatus.Enabled)
        val pref2 = MobileSignaturePreference("v2", MobileSignatureStatus.Disabled)
        val expected1 = MobileSignatureState.Data(MobileSignatureUiModelMapper.toSettingsUiModel(pref1))
        val expected2 = MobileSignatureState.Data(MobileSignatureUiModelMapper.toSettingsUiModel(pref2))

        val mobileSignatureFlow = MutableSharedFlow<MobileSignaturePreference>()
        every { mobileSignatureRepository.observeMobileSignature(userId) } returns mobileSignatureFlow

        val viewModel = MobileSignatureViewModel(
            userSessionRepository = userSessionRepository,
            mobileSignatureRepository = mobileSignatureRepository,
            reducer = reducer
        )

        // When
        viewModel.state.test {
            skipItems(1)
            mobileSignatureFlow.emit(pref1)
            val first = awaitItem()

            // Then
            assertEquals(expected1, first)

            // When
            mobileSignatureFlow.emit(pref2)
            val second = awaitItem() as MobileSignatureState.Data

            // Then
            assertEquals(expected2, second)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle signature enabled state`() = runTest {
        // Given
        val pref = MobileSignaturePreference("sig", MobileSignatureStatus.Disabled)
        every { mobileSignatureRepository.observeMobileSignature(userId) } returns flowOf(pref)
        coEvery { mobileSignatureRepository.setMobileSignatureEnabled(userId, true) } returns Unit.right()
        val viewModel = MobileSignatureViewModel(
            userSessionRepository = userSessionRepository,
            mobileSignatureRepository = mobileSignatureRepository,
            reducer = reducer
        )

        // When
        viewModel.state.test {
            awaitItem()
            viewModel.submit(MobileSignatureViewAction.ToggleSignatureEnabled(true))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { mobileSignatureRepository.setMobileSignatureEnabled(userId, true) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update signature value successfully`() = runTest {
        // Given
        val oldSignature = "old signature"
        val newSignature = "new signature"
        val pref = MobileSignaturePreference(oldSignature, MobileSignatureStatus.Enabled)
        every { mobileSignatureRepository.observeMobileSignature(userId) } returns flowOf(pref)
        coEvery { mobileSignatureRepository.setMobileSignature(userId, newSignature) } returns Unit.right()
        val viewModel = MobileSignatureViewModel(
            userSessionRepository = userSessionRepository,
            mobileSignatureRepository = mobileSignatureRepository,
            reducer = reducer
        )

        // When
        viewModel.state.test {
            awaitItem()
            viewModel.submit(MobileSignatureViewAction.UpdateSignatureValue(newSignature))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { mobileSignatureRepository.setMobileSignature(userId, newSignature) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
