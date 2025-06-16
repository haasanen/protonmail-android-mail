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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.protonmail.android.design.compose.theme.ProtonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtonModalBottomSheetLayout(
    showBottomSheet: Boolean,
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismissed: () -> Unit,
    dismissOnBack: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {

        content()

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissed,
                sheetState = sheetState,
                shape = ProtonTheme.shapes.bottomSheet,
                containerColor = ProtonTheme.colors.backgroundInvertedNorm,
                contentColor = ProtonTheme.colors.textNorm,
                dragHandle = { BottomSheetDefaults.DragHandle(color = ProtonTheme.colors.backgroundDeep) },
                content = sheetContent,
                properties = ModalBottomSheetProperties(shouldDismissOnBackPress = dismissOnBack)
            )
        }
    }
}
