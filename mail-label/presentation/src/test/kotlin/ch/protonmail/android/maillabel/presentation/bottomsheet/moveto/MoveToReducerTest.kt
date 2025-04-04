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

package ch.protonmail.android.maillabel.presentation.bottomsheet.moveto

import androidx.compose.ui.unit.dp
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.iconRes
import ch.protonmail.android.maillabel.presentation.iconTintColor
import ch.protonmail.android.maillabel.presentation.model.MailLabelText
import ch.protonmail.android.maillabel.presentation.text
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class MoveToReducerTest(
    @Suppress("unused") private val testName: String,
    val state: MoveToState,
    val operation: MoveToOperation,
    val expectedState: MoveToState
) {

    private val reducer = MoveToReducer()

    @Test
    fun `should reduce the state correctly`() {
        val updatedState = reducer.newStateFrom(state, operation)
        assertEquals(expectedState, updatedState)
    }

    companion object {

        private val archiveSystemUiModel = MailLabelTestData.archiveSystemLabel.let {
            MoveToBottomSheetDestinationUiModel.System(it.id, it.text(), it.iconRes(), it.iconTintColor())
        }

        private val label2021UiModel = MailLabelTestData.label2021.let {
            MoveToBottomSheetDestinationUiModel.Custom(
                it.id,
                it.text(),
                it.iconRes(),
                it.iconTintColor(),
                iconPaddingStart = 0.dp
            )
        }

        private val loadedEvent = MoveToOperation.MoveToEvent.InitialData(
            moveToDestinations = listOf(
                MailLabelTestData.archiveSystemLabel,
                MailLabelTestData.label2021
            ).toImmutableList(),
            entryPoint = MoveToBottomSheetEntryPoint.Conversation
        )

        private val initialState = MoveToState.Data(
            entryPoint = MoveToBottomSheetEntryPoint.Conversation,
            systemDestinations = listOf(archiveSystemUiModel).toImmutableList(),
            customDestinations = listOf(label2021UiModel).toImmutableList(),
            shouldDismissEffect = Effect.empty(),
            errorEffect = Effect.empty()
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "from loading state to initial error loading",
                MoveToState.Loading,
                MoveToOperation.MoveToEvent.LoadingError,
                MoveToState.Error
            ),
            arrayOf(
                "from loading on initial successful loading",
                MoveToState.Loading,
                loadedEvent,
                initialState
            ),
            arrayOf(
                "from loaded data to error moving state",
                initialState,
                MoveToOperation.MoveToEvent.ErrorMoving,
                initialState.copy(
                    errorEffect = Effect.Companion.of(TextUiModel(R.string.bottom_sheet_move_to_action_error))
                )
            ),
            arrayOf(
                "from loaded data to completed state",
                initialState,
                MoveToOperation.MoveToEvent.MoveComplete(MailLabelText("123")),
                initialState.copy(
                    shouldDismissEffect = Effect.of(MoveToState.MoveToDismissData(MailLabelText("123")))
                )
            )
        )
    }
}
