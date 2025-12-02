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
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.model.GroupedContactListItemsUiModel
import kotlinx.collections.immutable.ImmutableList

sealed interface ContactListState {

    data class Loading(
        val errorLoading: Effect<TextUiModel> = Effect.empty()
    ) : ContactListState

    sealed interface Loaded : ContactListState {

        val bottomSheetVisibilityEffect: Effect<BottomSheetVisibilityEffect>
        val openContactSearch: Effect<Boolean>
        val bottomSheetType: BottomSheetType

        data class Data(
            override val bottomSheetVisibilityEffect: Effect<BottomSheetVisibilityEffect> = Effect.empty(),
            override val openContactSearch: Effect<Boolean> = Effect.empty(),
            override val bottomSheetType: BottomSheetType = BottomSheetType.Menu,
            val showDeleteConfirmDialog: Effect<ContactListItemUiModel.Contact> = Effect.empty(),
            val showDeleteConfirmationSnackbar: Effect<TextUiModel> = Effect.empty(),
            val groupedContacts: ImmutableList<GroupedContactListItemsUiModel>
        ) : Loaded

        data class Empty(
            override val bottomSheetVisibilityEffect: Effect<BottomSheetVisibilityEffect> = Effect.empty(),
            override val openContactSearch: Effect<Boolean> = Effect.empty(),
            override val bottomSheetType: BottomSheetType = BottomSheetType.RedirectToWeb
        ) : Loaded
    }

    sealed interface BottomSheetType {
        data object RedirectToWeb : BottomSheetType
        data object Menu : BottomSheetType
        data object Upselling : BottomSheetType
    }
}

