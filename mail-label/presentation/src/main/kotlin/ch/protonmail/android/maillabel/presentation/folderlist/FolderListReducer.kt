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

package ch.protonmail.android.maillabel.presentation.folderlist

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.BottomSheetVisibilityEffect
import javax.inject.Inject

class FolderListReducer @Inject constructor() {

    internal fun newStateFrom(currentState: FolderListState, event: FolderListEvent): FolderListState {
        return when (event) {
            is FolderListEvent.FolderListLoaded -> reduceFolderListLoaded(currentState, event)
            is FolderListEvent.ErrorLoadingFolderList -> reduceErrorLoadingFolderList()
            is FolderListEvent.OpenFolderForm -> reduceOpenFolderForm(currentState)
            is FolderListEvent.DismissSettings -> reduceDismissSettings(currentState)
            is FolderListEvent.OpenSettings -> reduceOpenSettings(currentState)
        }
    }

    private fun reduceFolderListLoaded(
        currentState: FolderListState,
        event: FolderListEvent.FolderListLoaded
    ): FolderListState {
        return when (currentState) {
            is FolderListState.Loading -> {
                if (event.folderList.isNotEmpty()) {
                    FolderListState.ListLoaded.Data(
                        folders = event.folderList
                    )
                } else FolderListState.ListLoaded.Empty()
            }
            is FolderListState.ListLoaded -> {
                if (event.folderList.isNotEmpty()) {
                    FolderListState.ListLoaded.Data(
                        bottomSheetVisibilityEffect = currentState.bottomSheetVisibilityEffect,
                        openFolderForm = currentState.openFolderForm,
                        folders = event.folderList
                    )
                } else {
                    FolderListState.ListLoaded.Empty(
                        bottomSheetVisibilityEffect = currentState.bottomSheetVisibilityEffect,
                        openFolderForm = currentState.openFolderForm
                    )
                }
            }
        }
    }

    private fun reduceErrorLoadingFolderList() =
        FolderListState.Loading(errorLoading = Effect.of(TextUiModel(R.string.folder_list_loading_error)))

    private fun reduceOpenFolderForm(currentState: FolderListState): FolderListState {
        return when (currentState) {
            is FolderListState.Loading -> currentState
            is FolderListState.ListLoaded.Data -> currentState.copy(openFolderForm = Effect.of(Unit))
            is FolderListState.ListLoaded.Empty -> currentState.copy(openFolderForm = Effect.of(Unit))
        }
    }

    private fun reduceOpenSettings(currentState: FolderListState): FolderListState {
        return when (currentState) {
            is FolderListState.Loading -> currentState
            is FolderListState.ListLoaded.Data -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show)
            )
            is FolderListState.ListLoaded.Empty -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show)
            )
        }
    }

    private fun reduceDismissSettings(currentState: FolderListState): FolderListState {
        return when (currentState) {
            is FolderListState.Loading -> currentState
            is FolderListState.ListLoaded.Data -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
            )
            is FolderListState.ListLoaded.Empty -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
            )
        }
    }
}
