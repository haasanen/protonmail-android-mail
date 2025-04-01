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

package ch.protonmail.android.maillabel.presentation.bottomsheet

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.model.LabelAsBottomSheetUiModel
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import ch.protonmail.android.testdata.label.LabelTestData
import kotlinx.collections.immutable.toImmutableList
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class LabelAsReducerTest(
    @Suppress("unused") private val testName: String,
    val state: LabelAsState,
    val operation: LabelAsOperation,
    val expectedState: LabelAsState
) {

    private val reducer = LabelAsReducer()

    @Test
    fun `should reduce the state correctly`() {
        val updatedState = reducer.newStateFrom(state, operation)
        assertEquals(expectedState, updatedState)
    }

    companion object {

        private val loadedEvent = LabelAsOperation.LabelAsEvent.InitialData(
            actions = LabelAsActions(
                labels = listOf(
                    LabelTestData.buildLabel(id = "label1", name = "selected", color = null, order = 0, path = ""),
                    LabelTestData.buildLabel(
                        id = "label2",
                        name = "partially selected",
                        color = null,
                        order = 1,
                        path = ""
                    ),
                    LabelTestData.buildLabel(id = "label3", name = "not selected", color = null, order = 2, path = "")
                ),
                selected = listOf(LabelId("label1")),
                partiallySelected = listOf(LabelId("label2"))
            ),
            entryPoint = LabelAsBottomSheetEntryPoint.Conversation
        )

        private val loadedUiModel = listOf(
            buildUiModel(
                id = "label1",
                text = "selected",
                selectedState = LabelSelectedState.Selected
            ),
            buildUiModel(
                id = "label2",
                text = "partially selected",
                selectedState = LabelSelectedState.PartiallySelected
            ),
            buildUiModel(
                id = "label3",
                text = "not selected",
                selectedState = LabelSelectedState.NotSelected
            )
        )

        private val loadedUiModel2 = listOf(
            buildUiModel(
                id = "label1",
                text = "selected",
                selectedState = LabelSelectedState.Selected
            ),
            buildUiModel(
                id = "label2",
                text = "partially selected",
                selectedState = LabelSelectedState.PartiallySelected
            ),
            buildUiModel(
                id = "label3",
                text = "not selected",
                selectedState = LabelSelectedState.Selected
            )
        )

        private val initialState = LabelAsState.Data(
            entryPoint = LabelAsBottomSheetEntryPoint.Conversation,
            labelUiModels = loadedUiModel.toImmutableList(),
            shouldDismissEffect = Effect.Companion.empty(),
            errorEffect = Effect.Companion.empty()
        )

        private fun buildUiModel(
            id: String,
            text: String,
            selectedState: LabelSelectedState
        ) = LabelUiModelWithSelectedState(
            labelUiModel = LabelAsBottomSheetUiModel(
                id = MailLabelId.Custom.Label(LabelId(id)),
                text = TextUiModel.Text(text),
                icon = R.drawable.ic_proton_circle_filled_small,
                iconTint = null
            ),
            selectedState = selectedState
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "from loading state to initial error loading",
                LabelAsState.Loading,
                LabelAsOperation.LabelAsEvent.LoadingError,
                LabelAsState.Error
            ),
            arrayOf(
                "from loading on initial successful loading",
                LabelAsState.Loading,
                loadedEvent,
                initialState
            ),
            arrayOf(
                "from loaded to selection changed state",
                initialState,
                LabelAsOperation.LabelAsAction.LabelToggled(LabelId("label3")),
                initialState.copy(labelUiModels = loadedUiModel2.toImmutableList())
            ),
            arrayOf(
                "from loaded data to error labeling state",
                initialState,
                LabelAsOperation.LabelAsEvent.ErrorLabeling,
                initialState.copy(
                    errorEffect = Effect.Companion.of(TextUiModel(R.string.bottom_sheet_label_as_error_apply)),
                    shouldDismissEffect = Effect.Companion.of(Unit)
                )
            ),
            arrayOf(
                "from loaded data to completed state",
                initialState,
                LabelAsOperation.LabelAsEvent.LabelingComplete,
                initialState.copy(
                    shouldDismissEffect = Effect.Companion.of(Unit)
                )
            )
        )
    }
}
