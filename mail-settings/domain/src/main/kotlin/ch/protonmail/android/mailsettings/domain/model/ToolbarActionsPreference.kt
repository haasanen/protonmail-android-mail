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

package ch.protonmail.android.mailsettings.domain.model

import ch.protonmail.android.mailcommon.domain.model.Action
import me.proton.core.util.kotlin.takeIfNotEmpty

data class ToolbarActionsPreference(
    val actionsList: ToolbarActions
) {

    data class ActionSelection(
        val selected: List<Action>,
        val all: List<Action>
    ) {

        fun canAddMore(): Boolean = recognizedSelectedSize() < Defaults.MAX_ACTIONS
        fun canRemove(): Boolean = recognizedSelectedSize() > Defaults.MIN_ACTIONS

        private fun recognizedSelectedSize() = selected.size

        fun toggleSelection(action: Action, toggled: Boolean): ActionSelection {
            return if (toggled) {
                copy(
                    selected = selected + action
                )
            } else {
                copy(selected = selected.filterNot { it == action })
            }
        }

        fun reorder(fromIndex: Int, toIndex: Int): ActionSelection {
            val reorderedRecognized = selected
                .toMutableList()
                .apply { add(toIndex, removeAt(fromIndex)) }

            var recognizedActionsIdx = 0
            val reordered = reorderedRecognized.map { action ->
                action.also { recognizedActionsIdx++ }
            }
            return copy(selected = reordered)
        }
    }

    fun update(block: (ToolbarActions) -> ToolbarActions) = copy(actionsList = block(actionsList))

    data class ToolbarActions(
        val current: ActionSelection,
        val default: List<Action>
    ) {

        fun resetToDefault() = copy(current = current.copy(selected = default))

        fun reorder(fromIndex: Int, toIndex: Int) = copy(
            current = current.reorder(fromIndex = fromIndex, toIndex = toIndex)
        )

        fun toggleSelection(action: Action, toggled: Boolean) = copy(current = current.toggleSelection(action, toggled))
    }

    companion object {

        fun create(
            from: List<Action>,
            type: ToolbarType,
            allAvailableActions: List<Action>
        ): ToolbarActionsPreference {
            val actions = when (type) {
                ToolbarType.List ->
                    from.createActions(Defaults.MailboxActions, allAvailableActions)

                ToolbarType.Message ->
                    from.createActions(Defaults.MessageConversationActions, allAvailableActions)

                ToolbarType.Conversation ->
                    from.createActions(Defaults.MessageConversationActions, allAvailableActions)
            }

            return ToolbarActionsPreference(actions)
        }

        private fun List<Action>.createActions(default: List<Action>, allAvailableActions: List<Action>) =
            ToolbarActions(
                current = ActionSelection(
                    selected = this.takeIfNotEmpty() ?: default,
                    all = allAvailableActions
                ),
                default = default
            )
    }

    object Defaults {

        const val MIN_ACTIONS = 1
        const val MAX_ACTIONS = 5

        val MessageConversationActions = listOf(
            Action.MarkRead,
            Action.Trash,
            Action.Move,
            Action.Label
        )

        val MailboxActions = listOf(
            Action.MarkRead,
            Action.Trash,
            Action.Move,
            Action.Label
        )
    }
}
