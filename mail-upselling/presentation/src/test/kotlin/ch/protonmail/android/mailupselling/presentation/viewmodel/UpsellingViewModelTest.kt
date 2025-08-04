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

package ch.protonmail.android.mailupselling.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.repository.EventLoopRepository
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.domain.usecase.ObserveMailPlusPlanUpgrades
import ch.protonmail.android.mailupselling.domain.usecase.ResetPlanUpgradesCache
import ch.protonmail.android.mailupselling.presentation.UpsellingContentReducer
import ch.protonmail.android.mailupselling.presentation.model.UpsellingScreenContentOperation.UpsellingScreenContentEvent
import ch.protonmail.android.mailupselling.presentation.model.UpsellingScreenContentState
import ch.protonmail.android.mailupselling.presentation.ui.screen.UpsellingScreen.UpsellingEntryPointKey
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import me.proton.android.core.payment.domain.model.ProductDetail
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UpsellingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedStateHandle = mockk<SavedStateHandle>()
    private val observeMailPlusPlanUpgrades = mockk<ObserveMailPlusPlanUpgrades>()
    private val upsellingContentReducer = mockk<UpsellingContentReducer>()
    private val eventLoopRepository = mockk<EventLoopRepository>(relaxed = true)
    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val resetUpgradeCache = mockk<ResetPlanUpgradesCache>()

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @BeforeTest
    fun setup() {
        every {
            savedStateHandle.get<String>(UpsellingEntryPointKey)
        } returns Json.encodeToString<UpsellingEntryPoint>(UpsellingEntryPoint.Feature.Mailbox)
    }

    @Test
    fun `should return loading error when plans fetching returns an empty list`() = runTest {
        // Given
        coEvery { observeMailPlusPlanUpgrades() } returns flowOf(emptyList())
        val expectedFailure = mockk<UpsellingScreenContentState.Error>()
        every {
            upsellingContentReducer.newStateFrom(UpsellingScreenContentEvent.LoadingError.NoSubscriptions)
        } returns expectedFailure

        // When
        viewModel().state.test {
            // Then
            assertEquals(expectedFailure, awaitItem())
        }

        verify {
            upsellingContentReducer.newStateFrom(UpsellingScreenContentEvent.LoadingError.NoSubscriptions)
        }
        confirmVerified(upsellingContentReducer)
    }

    @Test
    fun `should return data state when plans fetching returns a valid list`() = runTest {
        // Given
        val expectedList = listOf<ProductDetail>(mockk(), mockk())
        coEvery { observeMailPlusPlanUpgrades() } returns flowOf(expectedList)

        val expectedModel = mockk<UpsellingScreenContentState.Data>()
        every {
            upsellingContentReducer.newStateFrom(
                operation = UpsellingScreenContentEvent.DataLoaded(
                    plans = expectedList,
                    upsellingEntryPoint = UpsellingEntryPoint.Feature.Navbar
                )
            )
        } returns expectedModel

        // When
        viewModel().state.test {
            // Then
            assertEquals(expectedModel, awaitItem())
        }

        verify {
            upsellingContentReducer.newStateFrom(
                UpsellingScreenContentEvent.DataLoaded(
                    plans = expectedList,
                    upsellingEntryPoint = UpsellingEntryPoint.Feature.Navbar
                )
            )
        }
        confirmVerified(upsellingContentReducer)
    }

    private fun viewModel() = UpsellingViewModel(
        savedStateHandle,
        observeMailPlusPlanUpgrades,
        upsellingContentReducer,
        eventLoopRepository,
        observePrimaryUserId,
        resetUpgradeCache
    )
}
