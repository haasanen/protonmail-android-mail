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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodySmallWeak
import ch.protonmail.android.mailtrackingprotection.presentation.model.TrackersUiModel

@Composable
internal fun BlockedTrackersDetailsList(trackers: TrackersUiModel, onUrlClick: (String, String) -> Unit) {

    Column {

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        for (tracker in trackers.items) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tracker.domain,
                    style = ProtonTheme.typography.bodyLargeNorm
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${tracker.urls.size}",
                    style = ProtonTheme.typography.bodyLargeNorm
                )
            }

            for (url in tracker.urls) {
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))

                Text(
                    modifier = Modifier
                        .clickable(
                            onClick = { onUrlClick(tracker.domain, url) },
                            interactionSource = null,
                            indication = null
                        ),
                    text = url,
                    style = ProtonTheme.typography.bodySmallWeak,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
        }
    }
}
