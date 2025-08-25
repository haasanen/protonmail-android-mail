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
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.domain.repository.ToolbarActionsRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UpdateToolbarPreferencesTest {

    private val repository = mockk<ToolbarActionsRepository>()

    private lateinit var updateToolbarPreferences: UpdateToolbarPreferences

    @BeforeTest
    fun setup() {
        updateToolbarPreferences = UpdateToolbarPreferences(repository)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should call the repository and return Unit (success)`() = runTest {
        // Given
        coEvery {
            repository.saveActions(userId, ToolbarType.Conversation, selectedPrefs)
        } returns Unit.right()

        // When
        val actual = updateToolbarPreferences(userId, ToolbarType.Conversation, actionsPreference)

        // Then
        assertEquals(Unit.right(), actual)
        coVerify(exactly = 1) { repository.saveActions(userId, ToolbarType.Conversation, selectedPrefs) }
        confirmVerified(repository)
    }

    @Test
    fun `should call the repository and return the error (failure)`() = runTest {
        // Given
        coEvery {
            repository.saveActions(userId, ToolbarType.Conversation, selectedPrefs)
        } returns DataError.Local.Unknown.left()

        // When
        val actual = updateToolbarPreferences(userId, ToolbarType.Conversation, actionsPreference)

        // Then
        assertEquals(DataError.Local.Unknown.left(), actual)
        coVerify(exactly = 1) { repository.saveActions(userId, ToolbarType.Conversation, selectedPrefs) }
        confirmVerified(repository)
    }

    private companion object {

        val userId = UserId("user-id")
        val actionsPreference = ToolbarActionsPreference(
            ToolbarActionsPreference.ToolbarActions(
                ToolbarActionsPreference.ActionSelection(listOf(Action.Forward), listOf()),
                listOf()
            )
        )

        val selectedPrefs = actionsPreference.actionsList.current.selected
    }
}
