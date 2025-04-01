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
import ch.protonmail.android.maillabel.domain.model.toMailLabelCustom
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import ch.protonmail.android.maillabel.presentation.toCustomUiModel
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

internal class LabelAsReducer @Inject constructor() {

    fun newStateFrom(contentState: LabelAsState, event: LabelAsOperation) = when (contentState) {
        is LabelAsState.Data -> when (event) {
            is LabelAsOperation.LabelAsAction.LabelToggled -> reduceLabelSelected(contentState, event)
            is LabelAsOperation.LabelAsEvent.ErrorLabeling -> reduceLabelingError(contentState)
            is LabelAsOperation.LabelAsEvent.LabelingComplete -> reduceLabelingComplete(contentState)
            else -> contentState
        }

        LabelAsState.Error,
        LabelAsState.Loading -> when (event) {
            is LabelAsOperation.LabelAsEvent.InitialData -> reduceInitialState(event)
            is LabelAsOperation.LabelAsEvent.LoadingError -> LabelAsState.Error
            else -> contentState
        }
    }

    private fun reduceInitialState(event: LabelAsOperation.LabelAsEvent.InitialData): LabelAsState {
        val customLabels = event.actions.labels.toMailLabelCustom().map {
            it.toCustomUiModel(emptyMap(), null)
        }.toImmutableList()

        val labelWithSelectionState = customLabels.map { label ->
            LabelUiModelWithSelectedState(
                labelUiModel = LabelAsBottomSheetUiModelMapper.toUiModel(label),
                selectedState = LabelAsBottomSheetUiModelMapper.mapIdToSelectedState(
                    mailLabelId = label.id,
                    selectedLabels = event.actions.selected.toSet(),
                    partiallySelectedLabels = event.actions.partiallySelected.toSet()
                )
            )
        }.toImmutableList()

        return LabelAsState.Data(
            entryPoint = event.entryPoint,
            labelUiModels = labelWithSelectionState,
            shouldDismissEffect = Effect.empty(),
            errorEffect = Effect.empty()
        )
    }

    private fun reduceLabelSelected(
        contentState: LabelAsState.Data,
        action: LabelAsOperation.LabelAsAction.LabelToggled
    ): LabelAsState {
        val newLabelList = contentState.labelUiModels.map { labelUiModelWithSelectedState ->
            if (labelUiModelWithSelectedState.labelUiModel.id.labelId.id == action.labelId.id) {
                labelUiModelWithSelectedState.copy(
                    selectedState = when (labelUiModelWithSelectedState.selectedState) {
                        is LabelSelectedState.NotSelected,
                        is LabelSelectedState.PartiallySelected -> LabelSelectedState.Selected

                        is LabelSelectedState.Selected -> LabelSelectedState.NotSelected
                    }
                )
            } else {
                labelUiModelWithSelectedState
            }
        }.toImmutableList()

        return contentState.copy(labelUiModels = newLabelList)
    }

    private fun reduceLabelingError(contentState: LabelAsState.Data) = contentState.copy(
        errorEffect = Effect.of(
            TextUiModel(R.string.bottom_sheet_label_as_error_apply)
        ),
        shouldDismissEffect = Effect.of(Unit)
    )

    private fun reduceLabelingComplete(contentState: LabelAsState.Data) =
        contentState.copy(shouldDismissEffect = Effect.of(Unit))
}
