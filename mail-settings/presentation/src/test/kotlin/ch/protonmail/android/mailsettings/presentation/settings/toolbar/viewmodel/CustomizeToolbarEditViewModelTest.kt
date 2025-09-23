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

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsRefreshSignal
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.domain.repository.InMemoryToolbarActionsRepository
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.CustomizeToolbarEditViewModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditOperation
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditState
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.SaveEvent
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.reducer.CustomizeToolbarEditActionsReducer
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui.CustomizeToolbarEditScreen
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.usecase.UpdateToolbarPreferences
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.serialize
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CustomizeToolbarEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val inMemoryRepo = mockk<InMemoryToolbarActionsRepository>()
    private val reducer = mockk<CustomizeToolbarEditActionsReducer>()
    private val updateToolbarPreferences = mockk<UpdateToolbarPreferences>()
    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val toolbarRefreshSignal = mockk<ToolbarActionsRefreshSignal>()

    private val savedStateHandle: SavedStateHandle = mockk<SavedStateHandle>()

    private fun viewModel() = CustomizeToolbarEditViewModel(
        inMemoryRepo,
        reducer,
        updateToolbarPreferences,
        observePrimaryUserId,
        toolbarRefreshSignal,
        savedStateHandle
    )

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return the initial state from the in memory repository when queried`() = runTest {
        // Given
        val type = ToolbarType.Conversation
        expectedOpenMode(type)

        val expectedState = mockk<CustomizeToolbarEditState.Data>()
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        every { reducer.toNewState(any(), type, any()) } returns expectedState

        // When + Then
        viewModel().state.test {
            assertEquals(expectedState, awaitItem())
            coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
            coVerify(exactly = 1) { reducer.toNewState(temporaryPrefs.actionsList.current, type, SaveEvent.None) }
            confirmVerified(inMemoryRepo, reducer)
        }
    }

    @Test
    fun `should toggle selection when an action is selected`() = runTest {
        // Given
        val type = ToolbarType.Conversation
        expectedOpenMode(type)
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        coEvery { inMemoryRepo.toggleSelection(Action.Forward, true) } just runs

        // When + Then
        val viewModel = viewModel()
        viewModel.submit(CustomizeToolbarEditOperation.ActionSelected(Action.Forward))

        coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
        coVerify(exactly = 1) { inMemoryRepo.toggleSelection(Action.Forward, true) }
        confirmVerified(inMemoryRepo)
    }

    @Test
    fun `should toggle selection when an action is removed`() = runTest {
        // Given
        val type = ToolbarType.Conversation
        expectedOpenMode(type)
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        coEvery { inMemoryRepo.toggleSelection(Action.Spam, false) } just runs

        // When + Then
        val viewModel = viewModel()
        viewModel.submit(CustomizeToolbarEditOperation.ActionRemoved(Action.Spam))

        coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
        coVerify(exactly = 1) { inMemoryRepo.toggleSelection(Action.Spam, false) }
        confirmVerified(inMemoryRepo)
    }

    @Test
    fun `should reorder when requested`() = runTest {
        // Given
        val type = ToolbarType.Conversation
        expectedOpenMode(type)
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        coEvery { inMemoryRepo.reorder(fromIndex = 1, toIndex = 2) } just runs

        // When + Then
        val viewModel = viewModel()
        viewModel.submit(CustomizeToolbarEditOperation.ActionMoved(fromIndex = 1, toIndex = 2))

        coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
        coVerify(exactly = 1) { inMemoryRepo.reorder(fromIndex = 1, toIndex = 2) }
        confirmVerified(inMemoryRepo)
    }

    @Test
    fun `should toggle defaults when reset is requested`() = runTest {
        // Given
        val type = ToolbarType.Conversation
        expectedOpenMode(type)
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        coEvery { inMemoryRepo.resetToDefault() } just runs

        // When + Then
        val viewModel = viewModel()
        viewModel.submit(CustomizeToolbarEditOperation.ResetToDefaultConfirmed)

        coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
        coVerify(exactly = 1) { inMemoryRepo.resetToDefault() }
        confirmVerified(inMemoryRepo)
    }

    @Test
    fun `should perform a save when requested and update the state when succeeds`() = runTest {
        // Given
        val userId = UserId("user-id")
        val type = ToolbarType.List
        expectedOpenMode(type)

        every { observePrimaryUserId() } returns flowOf(userId)
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        coEvery { updateToolbarPreferences(userId, type, temporaryPrefs) } returns Unit.right()
        every { toolbarRefreshSignal.refresh() } just runs
        every { reducer.toNewState(any(), type, any()) } returns mockk()

        // When + Then
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem() // Skip

            viewModel.submit(CustomizeToolbarEditOperation.SaveClicked)
            advanceUntilIdle()

            coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
            coVerifySequence {
                reducer.toNewState(any(), type, SaveEvent.None)
                updateToolbarPreferences(userId, type, temporaryPrefs)
                toolbarRefreshSignal.refresh()
                reducer.toNewState(any(), type, SaveEvent.Success)

            }
            confirmVerified(inMemoryRepo, updateToolbarPreferences, toolbarRefreshSignal, reducer)
        }
    }

    @Test
    fun `should perform a save when requested and update the state when it fails (userId)`() = runTest {
        // Given
        val type = ToolbarType.List
        expectedOpenMode(type)

        every { observePrimaryUserId() } returns flowOf(null)
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        every { reducer.toNewState(any(), type, any()) } returns mockk()

        // When + Then
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem() // Skip

            viewModel.submit(CustomizeToolbarEditOperation.SaveClicked)
            advanceUntilIdle()

            coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
            coVerifySequence {
                reducer.toNewState(any(), type, SaveEvent.None)
                reducer.toNewState(any(), type, SaveEvent.Error)
            }
            confirmVerified(inMemoryRepo, updateToolbarPreferences, toolbarRefreshSignal, reducer)
        }
    }

    @Test
    fun `should perform a save when requested and update the state when it fails (save)`() = runTest {
        // Given
        val userId = UserId("user-id")
        val type = ToolbarType.List
        expectedOpenMode(type)

        every { observePrimaryUserId() } returns flowOf(userId)
        coEvery { inMemoryRepo.inMemoryPreferences(any()) } returns flowOf(temporaryPrefs.right())
        every { toolbarRefreshSignal.refresh() } just runs
        coEvery { updateToolbarPreferences(userId, type, temporaryPrefs) } returns DataError.Local.CryptoError.left()
        every { reducer.toNewState(any(), type, any()) } returns mockk()

        // When + Then
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem() // Skip

            viewModel.submit(CustomizeToolbarEditOperation.SaveClicked)
            advanceUntilIdle()

            coVerify(exactly = 1) { inMemoryRepo.inMemoryPreferences(type) }
            coVerifySequence {
                reducer.toNewState(any(), type, SaveEvent.None)
                updateToolbarPreferences(userId, type, temporaryPrefs)
                reducer.toNewState(any(), type, SaveEvent.Error)
            }
            confirmVerified(inMemoryRepo, updateToolbarPreferences, toolbarRefreshSignal, reducer)
        }
    }

    private fun expectedOpenMode(toolbarType: ToolbarType) {
        every { savedStateHandle.get<String>(CustomizeToolbarEditScreen.OpenMode) } returns toolbarType.serialize()
    }

    private companion object {

        val temporaryPrefs = ToolbarActionsPreference(
            actionsList = ToolbarActionsPreference.ToolbarActions(
                ToolbarActionsPreference.ActionSelection(
                    listOf(Action.Snooze, Action.Spam),
                    listOf(Action.Snooze, Action.Forward, Action.Spam)
                ),
                default = listOf(Action.Snooze, Action.Forward)
            )
        )
    }
}
