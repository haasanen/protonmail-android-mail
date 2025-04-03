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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonHorizontallyCenteredProgress
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.presentation.R
import me.proton.core.domain.entity.UserId

@Composable
fun LabelAsBottomSheetScreen(
    providedData: LabelAsBottomSheet.InitialData,
    actions: LabelAsBottomSheet.Actions,
    modifier: Modifier = Modifier
) {

    val viewModel = hiltViewModel<LabelAsViewModel, LabelAsViewModel.Factory>(
        key = providedData.identifier()
    ) { factory ->
        factory.create(providedData)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    val contentActions = LabelAsBottomSheetContent.Actions.Empty.copy(
        onDoneClick = { archiveSelected ->
            viewModel.submit(LabelAsOperation.LabelAsAction.OperationConfirmed(archiveSelected))
        },
        onCreateNewLabelClick = actions.onCreateNewLabelClick,
        onLabelToggled = { viewModel.submit(LabelAsOperation.LabelAsAction.LabelToggled(it)) },
        onLabelAsComplete = actions.onLabelAsComplete,
        onError = actions.onError
    )

    when (val currentState = state) {
        is LabelAsState.Data -> LabelAsBottomSheetContent(
            labelAsDataState = currentState,
            actions = contentActions,
            modifier = modifier
        )

        is LabelAsState.Loading -> ProtonHorizontallyCenteredProgress()

        is LabelAsState.Error -> {
            actions.onError(stringResource(R.string.bottom_sheet_label_as_error_fetch))
            actions.onDismiss()
        }
    }
}

object LabelAsBottomSheet {

    data class Actions(
        val onCreateNewLabelClick: () -> Unit,
        val onError: (String) -> Unit,
        val onDismiss: () -> Unit,
        val onLabelAsComplete: (shouldExit: Boolean, entryPoint: LabelAsBottomSheetEntryPoint) -> Unit
    )

    data class InitialData(
        val userId: UserId,
        val currentLocationLabelId: LabelId,
        val items: List<LabelAsItemId>,
        val entryPoint: LabelAsBottomSheetEntryPoint
    ) {

        fun identifier() = "${this.hashCode()}"
    }
}
