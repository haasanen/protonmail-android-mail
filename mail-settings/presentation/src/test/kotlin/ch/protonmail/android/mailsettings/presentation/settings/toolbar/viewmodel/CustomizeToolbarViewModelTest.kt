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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.viewmodel

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsRefreshSignal
import ch.protonmail.android.mailsettings.domain.repository.MailSettingsRepository
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.CustomizeToolbarViewModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.mapper.CustomizeToolbarActionsUiMapper
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarState
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.ToolbarActionsSet
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.usecase.GetToolbarActions
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ViewMode
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CustomizeToolbarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val getToolbarActions = mockk<GetToolbarActions>()
    private val mailSettingsRepository = mockk<MailSettingsRepository>()
    private val mapper = mockk<CustomizeToolbarActionsUiMapper>()
    private val refreshSignal = mockk<ToolbarActionsRefreshSignal>()

    private fun viewModel() = CustomizeToolbarViewModel(
        observePrimaryUserId,
        getToolbarActions,
        mailSettingsRepository,
        mapper,
        refreshSignal
    )

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should emit loading on start`() = runTest {
        // Given
        every { observePrimaryUserId() } returns flowOf()

        // When + Then
        viewModel().state.test {
            assertEquals(CustomizeToolbarState.Loading, awaitItem())
            confirmVerified(getToolbarActions)
        }
    }

    @Test
    fun `should emit the correct state on successful loading`() = runTest {
        // Given
        val expectedToolbarActions = ToolbarActionsSet(
            list = listOf(Action.Archive),
            conversation = listOf(Action.Spam),
            messages = listOf(Action.Move)
        )
        every { observePrimaryUserId() } returns flowOf(userId)
        every { refreshSignal.refreshEvents } returns MutableSharedFlow()
        coEvery { mailSettingsRepository.getMailSettings(userId) } returns mockk {
            every { this@mockk.viewMode } returns ViewMode.enumOf(0)
        }
        every {
            mapper.mapToList(expectedToolbarActions, ViewMode.ConversationGrouping)
        } returns listOf(mockk(), mockk())

        coEvery { getToolbarActions.invoke(userId) } returns expectedToolbarActions.right()

        // When + Then
        viewModel().state.test {
            assertTrue { awaitItem() is CustomizeToolbarState.Data }
            coVerify(exactly = 1) { getToolbarActions.invoke(userId) }
            confirmVerified(getToolbarActions)
        }
    }

    @Test
    fun `should emit an error on unsuccessful toolbar actions retrieval`() = runTest {
        // Given
        every { observePrimaryUserId() } returns flowOf(userId)
        every { refreshSignal.refreshEvents } returns MutableSharedFlow()

        coEvery { getToolbarActions.invoke(userId) } returns DataError.Local.NoDataCached.left()

        // When + Then
        viewModel().state.test {
            assertEquals(CustomizeToolbarState.Error, awaitItem())
            coVerify(exactly = 1) { getToolbarActions.invoke(userId) }
            confirmVerified(getToolbarActions)
        }
    }

    private companion object {

        val userId = UserId("user-id")
    }
}
