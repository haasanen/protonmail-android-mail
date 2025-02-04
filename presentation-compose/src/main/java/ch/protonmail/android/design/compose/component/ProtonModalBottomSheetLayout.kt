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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ch.protonmail.android.design.compose.theme.ProtonTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtonModalBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismissed: () -> Unit,
    dismissOnBack: Boolean,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {

        content()

        if (sheetState.isVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch { sheetState.hide() }
                    onDismissed()
                },
                sheetState = sheetState,
                shape = ProtonTheme.shapes.bottomSheet,
                containerColor = ProtonTheme.colors.backgroundInvertedNorm,
                contentColor = ProtonTheme.colors.textNorm,
                content = sheetContent,
                properties = ModalBottomSheetProperties(shouldDismissOnBackPress = dismissOnBack)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtonModalBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable () -> Unit
) {
    ProtonModalBottomSheetLayout(
        sheetContent = sheetContent,
        modifier = modifier,
        sheetState = sheetState,
        content = content,
        dismissOnBack = true,
        onDismissed = { }
    )
}

