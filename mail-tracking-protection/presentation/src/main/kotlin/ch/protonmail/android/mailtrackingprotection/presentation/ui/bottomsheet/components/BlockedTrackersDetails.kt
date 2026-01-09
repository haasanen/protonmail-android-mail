/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailtrackingprotection.presentation.model.TrackersUiModel

@Composable
internal fun BlockedTrackersDetails(
    trackers: TrackersUiModel,
    onUrlClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    val isExpanded = remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.large)
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.large
            )
            .padding(ProtonDimens.Spacing.ModeratelyLarger)
    ) {

        BlockedTrackersDetailsHeader(
            trackers,
            isExpanded.value,
            onClick = { isExpanded.value = !isExpanded.value }
        )

        AnimatedContent(isExpanded.value) {
            if (it) {
                BlockedTrackersDetailsList(trackers, onUrlClick)
            }
        }
    }
}
