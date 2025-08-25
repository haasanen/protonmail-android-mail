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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.reducer

import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ToolbarActionUiModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.mapper.CustomizeToolbarEditActionsMapper
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditState
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.SaveEvent
import io.mockk.clearAllMocks
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class CustomizeToolbarEditActionsReducerTest(
    @Suppress("unused") private val testName: String,
    private val selection: ToolbarActionsPreference.ActionSelection,
    private val type: ToolbarType,
    private val saveEvent: SaveEvent,
    private val expected: CustomizeToolbarEditState.Data
) {

    private val mapper = spyk(
        CustomizeToolbarEditActionsMapper(ActionUiModelMapper())
    )

    private lateinit var reducer: CustomizeToolbarEditActionsReducer

    @BeforeTest
    fun setup() {
        reducer = CustomizeToolbarEditActionsReducer(mapper)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should reduce selection to new state`() = runTest {
        // When
        val actual = reducer.toNewState(selection, type, saveEvent)

        // Then
        assertEquals(expected, actual)
    }

    companion object {

        private val actionsSelection = ToolbarActionsPreference.ActionSelection(
            selected = listOf(Action.Archive, Action.Spam),
            all = listOf(Action.Archive, Action.Forward, Action.Spam, Action.ViewHeaders)
        )

        private val actionsRemainingDisabledSelection = ToolbarActionsPreference.ActionSelection(
            selected = listOf(
                Action.Archive,
                Action.Spam,
                Action.Move,
                Action.Label,
                Action.Snooze
            ),
            all = listOf(Action.Forward)
        )

        private val actionsSelectedDisabledSelection = ToolbarActionsPreference.ActionSelection(
            selected = listOf(
                Action.Forward
            ),
            all = listOf(
                Action.Archive,
                Action.Spam,
                Action.Move,
                Action.Label,
                Action.Snooze
            )
        )

        private val baseListExpectedState = CustomizeToolbarEditState.Data(
            toolbarTitle = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_nav_title_list),
            disclaimer = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_header),
            selectedActions = listOf(
                ToolbarActionUiModel(ActionUiModel(Action.Archive), true),
                ToolbarActionUiModel(ActionUiModel(Action.Spam), true)
            ),
            remainingActions = listOf(
                ToolbarActionUiModel(ActionUiModel(Action.Forward), true),
                ToolbarActionUiModel(ActionUiModel(Action.ViewHeaders), true)
            ),
            close = Effect.empty(),
            error = Effect.empty()
        )

        private val baseConversationExpectedState = baseListExpectedState.copy(
            toolbarTitle = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_nav_title_convo)
        )

        private val baseMessageExpectedState = baseListExpectedState.copy(
            toolbarTitle = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_nav_title_message)
        )

        private val baseListRemainingDisabledState = CustomizeToolbarEditState.Data(
            toolbarTitle = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_nav_title_list),
            disclaimer = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_header),
            selectedActions = listOf(
                ToolbarActionUiModel(ActionUiModel(Action.Archive), true),
                ToolbarActionUiModel(ActionUiModel(Action.Spam), true),
                ToolbarActionUiModel(ActionUiModel(Action.Move), true),
                ToolbarActionUiModel(ActionUiModel(Action.Label), true),
                ToolbarActionUiModel(ActionUiModel(Action.Snooze), true)

            ),
            remainingActions = listOf(
                ToolbarActionUiModel(ActionUiModel(Action.Forward), false)
            ),
            close = Effect.empty(),
            error = Effect.empty()
        )

        private val baseListSelectedDisabledState = CustomizeToolbarEditState.Data(
            toolbarTitle = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_nav_title_list),
            disclaimer = TextUiModel.TextRes(R.string.mail_settings_custom_toolbar_header),
            selectedActions = listOf(
                ToolbarActionUiModel(ActionUiModel(Action.Forward), false)
            ),
            remainingActions = listOf(
                ToolbarActionUiModel(ActionUiModel(Action.Archive), true),
                ToolbarActionUiModel(ActionUiModel(Action.Spam), true),
                ToolbarActionUiModel(ActionUiModel(Action.Move), true),
                ToolbarActionUiModel(ActionUiModel(Action.Label), true),
                ToolbarActionUiModel(ActionUiModel(Action.Snooze), true)
            ),
            close = Effect.empty(),
            error = Effect.empty()
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "list selection to state - no save",
                actionsSelection,
                ToolbarType.List,
                SaveEvent.None,
                baseListExpectedState
            ),
            arrayOf(
                "list selection to state - no selection enabled",
                actionsSelectedDisabledSelection,
                ToolbarType.List,
                SaveEvent.None,
                baseListSelectedDisabledState
            ),
            arrayOf(
                "list selection to state - no remaining enabled",
                actionsRemainingDisabledSelection,
                ToolbarType.List,
                SaveEvent.None,
                baseListRemainingDisabledState
            ),
            arrayOf(
                "list selection to state - successful save",
                actionsSelection,
                ToolbarType.List,
                SaveEvent.Success,
                baseListExpectedState.copy(close = Effect.of(Unit))
            ),
            arrayOf(
                "list selection to state - non successful save",
                actionsSelection,
                ToolbarType.List,
                SaveEvent.Error,
                baseListExpectedState.copy(error = Effect.of(Unit))
            ),

            arrayOf(
                "convo selection to state - no save",
                actionsSelection,
                ToolbarType.Conversation,
                SaveEvent.None,
                baseConversationExpectedState
            ),
            arrayOf(
                "convo selection to state - successful save",
                actionsSelection,
                ToolbarType.Conversation,
                SaveEvent.Success,
                baseConversationExpectedState.copy(close = Effect.of(Unit))
            ),
            arrayOf(
                "convo selection to state - non successful save",
                actionsSelection,
                ToolbarType.Conversation,
                SaveEvent.Error,
                baseConversationExpectedState.copy(error = Effect.of(Unit))
            ),
            arrayOf(
                "message selection to state - no save",
                actionsSelection,
                ToolbarType.Message,
                SaveEvent.None,
                baseMessageExpectedState
            ),
            arrayOf(
                "message selection to state - successful save",
                actionsSelection,
                ToolbarType.Message,
                SaveEvent.Success,
                baseMessageExpectedState.copy(close = Effect.of(Unit))
            ),
            arrayOf(
                "message selection to state - non successful save",
                actionsSelection,
                ToolbarType.Message,
                SaveEvent.Error,
                baseMessageExpectedState.copy(error = Effect.of(Unit))
            )
        )
    }
}
