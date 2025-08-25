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

package ch.protonmail.android.mailsettings.data.repository

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.data.InMemoryToolbarActionsRepositoryImpl
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.domain.repository.InMemoryToolbarActionsRepository
import ch.protonmail.android.mailsettings.domain.repository.ToolbarActionsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class InMemoryToolbarActionsRepositoryImplTest {

    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk() } returns flowOf(userId)
    }

    private val toolbarActionsRepository = mockk<ToolbarActionsRepository>()

    private val repo: InMemoryToolbarActionsRepositoryImpl =
        InMemoryToolbarActionsRepositoryImpl(observePrimaryUserId, toolbarActionsRepository)

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `returns error if user is not logged in`() = runTest {
        // Given
        coEvery { observePrimaryUserId() } returns flowOf(null)

        // When + Then
        repo.inMemoryPreferences(ToolbarType.List).test {
            val item = awaitItem()
            assertEquals(InMemoryToolbarActionsRepository.Error.UserNotLoggedIn.left(), item)
            coVerify(exactly = 0) { toolbarActionsRepository.getToolbarActions(userId, ToolbarType.List) }
            awaitComplete()
        }
    }

    @Test
    fun `returns correctly mapped actions on preferences set`() = runTest {
        // Given
        val userActions = listOf(Action.MarkRead, Action.Label, Action.Move)
        val remainingActions = listOf(Action.ReportPhishing, Action.Archive)
        val allActions = userActions + remainingActions

        setupMocks(ToolbarType.List, userActions, allActions)

        // When + Then
        assertPreferencesResult(ToolbarType.List) { result ->
            assertEquals(userActions, result.actionsList.current.selected)
            assertEquals(allActions, result.actionsList.current.all)
        }
    }

    @Test
    fun `returns default actions when user preference is empty`() = runTest {
        // Given
        val userActions = emptyList<Action>()
        val defaults = listOf(Action.MarkRead, Action.Trash, Action.Move, Action.Label)
        val allActions = userActions + defaults

        setupMocks(ToolbarType.List, userActions, allActions)

        // When + Then
        assertPreferencesResult(ToolbarType.List) { result ->
            assertEquals(defaults, result.actionsList.current.selected)
            assertEquals(allActions, result.actionsList.current.all)
        }
    }

    @Test
    fun `adds an action to the selection when selected`() = runTest {
        // Given
        val initialActions = listOf(Action.MarkRead, Action.Label, Action.Move)
        val expectedAfterToggle = listOf(Action.MarkRead, Action.Label, Action.Move, Action.ReportPhishing)
        val remainingActions = listOf(Action.ReportPhishing, Action.Archive)
        val allActions = initialActions + remainingActions

        setupMocks(ToolbarType.Conversation, initialActions, allActions)

        // When + Then
        testToggleAction(
            toolbarType = ToolbarType.Conversation,
            actionToToggle = Action.ReportPhishing,
            toggleValue = true,
            initialExpected = initialActions,
            finalExpected = expectedAfterToggle,
            allActions = allActions
        )
    }

    @Test
    fun `removes an action from the selection when unselected`() = runTest {
        // Given
        val initialActions = listOf(Action.MarkRead, Action.Label, Action.Move, Action.ReportPhishing)
        val expectedAfterToggle = listOf(Action.MarkRead, Action.Label, Action.Move)
        val remainingActions = listOf(Action.Archive)
        val allActions = initialActions + remainingActions

        setupMocks(ToolbarType.Conversation, initialActions, allActions)

        // When + Then
        testToggleAction(
            toolbarType = ToolbarType.Conversation,
            actionToToggle = Action.ReportPhishing,
            toggleValue = false,
            initialExpected = initialActions,
            finalExpected = expectedAfterToggle,
            allActions = allActions
        )
    }

    @Test
    fun `reorders an action when requested`() = runTest {
        // Given
        val initialActions = listOf(Action.MarkRead, Action.Label, Action.Move, Action.ReportPhishing)
        val expectedReordered = listOf(Action.Label, Action.Move, Action.MarkRead, Action.ReportPhishing)
        val remainingActions = listOf(Action.MarkRead, Action.Trash, Action.Move, Action.Label)
        val allActions = initialActions + remainingActions

        setupMocks(ToolbarType.Conversation, initialActions, allActions)

        // When + Then
        repo.inMemoryPreferences(ToolbarType.Conversation).test {
            // Initial state
            val result = awaitValidResult()
            assertEquals(initialActions, result.actionsList.current.selected)
            assertEquals(allActions, result.actionsList.current.all)

            repo.reorder(fromIndex = 0, toIndex = 2)

            val updatedResult = awaitValidResult()
            assertEquals(expectedReordered, updatedResult.actionsList.current.selected)
            assertEquals(allActions, updatedResult.actionsList.current.all)
        }
    }

    @Test
    fun `resets to defaults when reset`() = runTest {
        // Given
        val initialActions = listOf(Action.MarkRead, Action.Label, Action.Move, Action.ReportPhishing)
        val defaults = listOf(Action.MarkRead, Action.Trash, Action.Move, Action.Label)
        val allActions = initialActions + defaults

        setupMocks(ToolbarType.Conversation, initialActions, allActions)

        // When + Then
        repo.inMemoryPreferences(ToolbarType.Conversation).test {
            // Initial state
            val result = awaitValidResult()
            assertEquals(initialActions, result.actionsList.current.selected)
            assertEquals(allActions, result.actionsList.current.all)

            repo.resetToDefault()

            val updatedResult = awaitValidResult()
            assertEquals(defaults, updatedResult.actionsList.current.selected)
            assertEquals(allActions, updatedResult.actionsList.current.all)
        }
    }

    private fun setupMocks(
        toolbarType: ToolbarType,
        userActions: List<Action>,
        allActions: List<Action>
    ) {
        coEvery { toolbarActionsRepository.getToolbarActions(userId, toolbarType) } returns userActions.right()
        coEvery { toolbarActionsRepository.getAllActions(toolbarType) } returns allActions
    }

    private suspend fun assertPreferencesResult(
        toolbarType: ToolbarType,
        assertion: (result: ToolbarActionsPreference) -> Unit
    ) {
        repo.inMemoryPreferences(toolbarType).test {
            val result = awaitValidResult()
            assertion(result)
        }
    }

    @Suppress("LongParameterList")
    private suspend fun testToggleAction(
        toolbarType: ToolbarType,
        actionToToggle: Action,
        toggleValue: Boolean,
        initialExpected: List<Action>,
        finalExpected: List<Action>,
        allActions: List<Action>
    ) {
        repo.inMemoryPreferences(toolbarType).test {
            // Initial state
            val result = awaitValidResult()
            assertEquals(initialExpected, result.actionsList.current.selected)
            assertEquals(allActions, result.actionsList.current.all)

            // After toggle
            repo.toggleSelection(actionToToggle, toggled = toggleValue)

            val updatedResult = awaitValidResult()

            assertEquals(finalExpected, updatedResult.actionsList.current.selected)
            assertEquals(allActions, updatedResult.actionsList.current.all)
        }
    }

    @Suppress("ExpressionBodySyntax")
    private suspend inline fun <L, R> ReceiveTurbine<Either<L, R>>.awaitValidResult(): R {
        return awaitItem().getOrNull() ?: fail("Expected a valid result, got an error.")
    }

    private companion object {

        val userId = UserId("user-id")
    }
}
