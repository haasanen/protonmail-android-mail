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
package ch.protonmail.android.design.compose.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterialApi
import androidx.compose.material3.ModalBottomSheetLayout
import androidx.compose.material3.ModalBottomSheetState
import androidx.compose.material3.ModalBottomSheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.protonmail.android.design.compose.theme.ProtonTheme

@ExperimentalMaterialApi
@Composable
fun ProtonModalBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    content: @Composable () -> Unit,
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = sheetState,
        sheetContent = sheetContent,
        sheetShape = ProtonTheme.shapes.bottomSheet,
        sheetBackgroundColor = ProtonTheme.colors.backgroundNorm,
        sheetContentColor = ProtonTheme.colors.textNorm,
        scrimColor = ProtonTheme.colors.blenderNorm,
        content = content
    )
}
