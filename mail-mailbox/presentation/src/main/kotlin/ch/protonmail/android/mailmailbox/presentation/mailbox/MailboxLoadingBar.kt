/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.LoadingBarUiState
import ch.protonmail.android.uicomponents.progress.CyclingProgressBar

@Composable
fun MailboxLoadingBar(state: LoadingBarUiState, modifier: Modifier = Modifier) {

    when (state) {
        is LoadingBarUiState.Hide -> {
            CyclingProgressBar(
                modifier = modifier,
                isAnimating = false
            )
        }

        is LoadingBarUiState.Show -> {

            CyclingProgressBar(
                modifier = modifier,
                isAnimating = true,
                animationDurationMillis = state.cycleDurationMs.toInt()
            )
        }
    }
}
