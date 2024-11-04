/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package ch.protonmail.android.design.compose.component.bottomsheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Stable
@OptIn(ExperimentalMaterial3Api::class)
data class ModalBottomSheetContentState(
    val sheetState: SheetState,
    val sheetContent: MutableState<@Composable ColumnScope.(runAction: RunAction) -> Unit>
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun rememberModalBottomSheetContentState(
    modalBottomSheetState: SheetState =
        rememberModalBottomSheetState(),
    modalBottomSheetContent: MutableState<@Composable ColumnScope.(runAction: RunAction) -> Unit> = mutableStateOf({})
): ModalBottomSheetContentState = remember {
    ModalBottomSheetContentState(
        modalBottomSheetState,
        modalBottomSheetContent
    )
}
