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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.mapper

import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ToolbarActionsUiModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.ToolbarActionsSet
import me.proton.core.mailsettings.domain.entity.ViewMode
import kotlin.test.Test
import ch.protonmail.android.mailsettings.presentation.R
import kotlin.test.assertEquals

internal class CustomizeToolbarActionsUiMapperTest {

    private val actionUiModelMapper = ActionUiModelMapper()

    private val mapper = CustomizeToolbarActionsUiMapper(actionUiModelMapper)

    @Test
    fun `should map the correct set of items based on the viewMode (conversation)`() {
        // Given
        val actionsSet = ToolbarActionsSet(
            list = listActions,
            conversation = conversationActions,
            messages = messageActions
        )

        val expectedItems = listOf(
            expectedListActions,
            expectedConversationActions
        )

        // When
        val actual = mapper.mapToList(actionsSet, ViewMode.ConversationGrouping)

        // Then
        assertEquals(expectedItems, actual)
    }

    @Test
    fun `should map the correct set of items based on the viewMode (message)`() {
        // Given
        val actionsSet = ToolbarActionsSet(
            list = listActions,
            conversation = conversationActions,
            messages = messageActions
        )

        val expectedItems = listOf(
            expectedListActions,
            expectedMessageActions
        )

        // When
        val actual = mapper.mapToList(actionsSet, ViewMode.NoConversationGrouping)

        // Then
        assertEquals(expectedItems, actual)
    }

    private companion object {

        val listActions = listOf(
            Action.Trash,
            Action.Spam,
            Action.MarkRead
        )

        val conversationActions = listOf(
            Action.Label,
            Action.Move,
            Action.Archive
        )

        val messageActions = listOf(
            Action.Reply,
            Action.Label
        )

        private val expectedListActions = ToolbarActionsUiModel(
            headerText = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_list_view_title),
            descriptionText = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_list_view_description),
            type = ToolbarType.List,
            actions = listOf(
                ActionUiModel(Action.Trash),
                ActionUiModel(Action.Spam),
                ActionUiModel(Action.MarkRead)
            )
        )

        private val expectedConversationActions = ToolbarActionsUiModel(
            headerText = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_conversation_view_title),
            descriptionText = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_conversation_view_description),
            type = ToolbarType.Conversation,
            actions = listOf(
                ActionUiModel(Action.Label),
                ActionUiModel(Action.Move),
                ActionUiModel(Action.Archive)
            )
        )

        private val expectedMessageActions = ToolbarActionsUiModel(
            headerText = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_message_view_title),
            descriptionText = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_message_view_description),
            type = ToolbarType.Message,
            actions = listOf(
                ActionUiModel(Action.Reply),
                ActionUiModel(Action.Label)
            )
        )
    }
}
