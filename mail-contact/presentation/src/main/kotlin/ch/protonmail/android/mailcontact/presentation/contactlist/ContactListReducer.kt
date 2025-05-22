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

package ch.protonmail.android.mailcontact.presentation.contactlist

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetVisibilityEffect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.presentation.R
import javax.inject.Inject

class ContactListReducer @Inject constructor() {

    internal fun newStateFrom(currentState: ContactListState, event: ContactListEvent): ContactListState {
        return when (event) {
            is ContactListEvent.ContactListLoaded -> reduceContactListLoaded(currentState, event)
            is ContactListEvent.ErrorLoadingContactList -> reduceErrorLoadingContactList()
            is ContactListEvent.DismissBottomSheet -> reduceDismissBottomSheet(currentState)
            is ContactListEvent.OpenBottomSheet -> reduceOpenBottomSheet(currentState)
            is ContactListEvent.OpenContactSearch -> reduceOpenContactSearch(currentState)
            is ContactListEvent.DeleteContactConfirmed -> reduceDeleteConfirmed(currentState)
            is ContactListEvent.DeleteContactRequested -> reduceDeleteRequested(currentState, event)
        }
    }

    private fun reduceContactListLoaded(
        currentState: ContactListState,
        event: ContactListEvent.ContactListLoaded
    ): ContactListState {
        return when (currentState) {
            is ContactListState.Loading -> {
                if (event.groupedContactsList.isNotEmpty()) {
                    ContactListState.Loaded.Data(groupedContacts = event.groupedContactsList)
                } else ContactListState.Loaded.Empty()
            }

            is ContactListState.Loaded -> {
                if (event.groupedContactsList.isNotEmpty()) {
                    ContactListState.Loaded.Data(
                        bottomSheetVisibilityEffect = currentState.bottomSheetVisibilityEffect,
                        groupedContacts = event.groupedContactsList
                    )
                } else {
                    ContactListState.Loaded.Empty(
                        bottomSheetVisibilityEffect = currentState.bottomSheetVisibilityEffect
                    )
                }
            }
        }
    }

    private fun reduceErrorLoadingContactList() =
        ContactListState.Loading(errorLoading = Effect.of(TextUiModel(R.string.contact_list_loading_error)))

    private fun reduceOpenBottomSheet(currentState: ContactListState): ContactListState {
        return when (currentState) {
            is ContactListState.Loading -> currentState
            is ContactListState.Loaded.Data -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show),
                bottomSheetType = ContactListState.BottomSheetType.Menu
            )

            is ContactListState.Loaded.Empty -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show),
                bottomSheetType = ContactListState.BottomSheetType.Menu
            )
        }
    }

    private fun reduceOpenContactSearch(currentState: ContactListState): ContactListState {
        return when (currentState) {
            is ContactListState.Loading -> currentState
            is ContactListState.Loaded.Data -> currentState.copy(
                openContactSearch = Effect.of(true)
            )

            is ContactListState.Loaded.Empty -> currentState.copy(
                openContactSearch = Effect.of(true)
            )
        }
    }

    private fun reduceDismissBottomSheet(currentState: ContactListState): ContactListState {
        return when (currentState) {
            is ContactListState.Loading -> currentState
            is ContactListState.Loaded.Data -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
            )

            is ContactListState.Loaded.Empty -> currentState.copy(
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
            )
        }
    }

    private fun reduceDeleteConfirmed(currentState: ContactListState): ContactListState {
        return when (currentState) {
            is ContactListState.Loaded.Data -> currentState.copy(
                showDeleteConfirmDialog = Effect.empty()
            )

            else -> currentState
        }
    }

    private fun reduceDeleteRequested(
        currentState: ContactListState,
        event: ContactListEvent.DeleteContactRequested
    ): ContactListState {
        return when (currentState) {
            is ContactListState.Loaded.Data -> currentState.copy(
                showDeleteConfirmDialog = Effect.of(event.contact)
            )

            else -> currentState
        }
    }
}

