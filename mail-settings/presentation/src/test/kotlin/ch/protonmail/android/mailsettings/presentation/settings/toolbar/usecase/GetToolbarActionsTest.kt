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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.domain.repository.ToolbarActionsRepository
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.ToolbarActionsSet
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetToolbarActionsTest {

    private val repository = mockk<ToolbarActionsRepository>()

    private lateinit var getToolbarActions: GetToolbarActions

    @BeforeTest
    fun setup() {
        getToolbarActions = GetToolbarActions(repository)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return a set of toolbar actions`() = runTest {
        // Given
        val expectedList = listOf(Action.ReportPhishing)
        val expectedConversation = listOf(Action.Archive)
        val expectedMessage = listOf(Action.Move)
        val expectedSet = ToolbarActionsSet(expectedList, expectedConversation, expectedMessage)

        coEvery { repository.getToolbarActions(userId, ToolbarType.List) } returns expectedList.right()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Conversation) } returns expectedConversation.right()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Message) } returns expectedMessage.right()

        // When
        val actual = getToolbarActions(userId)

        // Then
        assertEquals(expectedSet.right(), actual)
    }

    @Test
    fun `should return error when list toolbar actions fails`() = runTest {
        // Given
        val expectedError = DataError.Local.NoDataCached
        val expectedConversation = listOf(Action.Archive)
        val expectedMessage = listOf(Action.Move)

        coEvery { repository.getToolbarActions(userId, ToolbarType.List) } returns expectedError.left()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Conversation) } returns expectedConversation.right()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Message) } returns expectedMessage.right()

        // When
        val actual = getToolbarActions(userId)

        // Then
        assertEquals(expectedError.left(), actual)
    }

    @Test
    fun `should return error when conversation toolbar actions fails`() = runTest {
        // Given
        val expectedList = listOf(Action.ReportPhishing)
        val expectedError = DataError.Local.NoDataCached
        val expectedMessage = listOf(Action.Move)

        coEvery { repository.getToolbarActions(userId, ToolbarType.List) } returns expectedList.right()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Conversation) } returns expectedError.left()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Message) } returns expectedMessage.right()

        // When
        val actual = getToolbarActions(userId)

        // Then
        assertEquals(expectedError.left(), actual)
    }

    @Test
    fun `should return error when message toolbar actions fails`() = runTest {
        // Given
        val expectedList = listOf(Action.ReportPhishing)
        val expectedConversation = listOf(Action.Archive)
        val expectedError = DataError.Local.NoDataCached

        coEvery { repository.getToolbarActions(userId, ToolbarType.List) } returns expectedList.right()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Conversation) } returns expectedConversation.right()
        coEvery { repository.getToolbarActions(userId, ToolbarType.Message) } returns expectedError.left()

        // When
        val actual = getToolbarActions(userId)

        // Then
        assertEquals(expectedError.left(), actual)
    }


    private companion object {

        val userId = UserId("user-id")
    }
}
