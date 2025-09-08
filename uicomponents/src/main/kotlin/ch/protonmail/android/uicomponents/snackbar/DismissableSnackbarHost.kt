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

package ch.protonmail.android.uicomponents.snackbar

import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ch.protonmail.android.design.compose.component.ProtonSnackbarHost
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState

@Composable
fun DismissableSnackbarHost(modifier: Modifier = Modifier, protonSnackbarHostState: ProtonSnackbarHostState) {
    val currentSnackbarData = protonSnackbarHostState.snackbarHostState.currentSnackbarData

    // Reset the dismiss state when new snackbar appears
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled
    )

    // Reset to settled when snackbar data changes
    LaunchedEffect(currentSnackbarData) {
        if (currentSnackbarData != null) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            protonSnackbarHostState.snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { },
        content = {
            ProtonSnackbarHost(hostState = protonSnackbarHostState)
        }
    )
}
