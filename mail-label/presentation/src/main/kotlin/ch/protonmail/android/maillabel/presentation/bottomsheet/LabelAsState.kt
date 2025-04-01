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
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import kotlinx.collections.immutable.ImmutableList

sealed interface LabelAsState {

    data object Loading : LabelAsState
    data class Data(
        val entryPoint: LabelAsBottomSheetEntryPoint,
        val labelUiModels: ImmutableList<LabelUiModelWithSelectedState>,
        val shouldDismissEffect: Effect<Unit>,
        val errorEffect: Effect<TextUiModel>
    ) : LabelAsState

    data object Error : LabelAsState
}

sealed interface LabelAsOperation {
    sealed interface LabelAsEvent : LabelAsOperation {

        data object LoadingError : LabelAsEvent
        data class InitialData(val actions: LabelAsActions, val entryPoint: LabelAsBottomSheetEntryPoint) : LabelAsEvent
        data object ErrorLabeling : LabelAsEvent

        data object LabelingComplete : LabelAsEvent
    }

    sealed interface LabelAsAction : LabelAsOperation {
        data class LabelToggled(val labelId: LabelId) : LabelAsAction
        data class OperationConfirmed(val alsoArchive: Boolean) : LabelAsAction
    }
}
