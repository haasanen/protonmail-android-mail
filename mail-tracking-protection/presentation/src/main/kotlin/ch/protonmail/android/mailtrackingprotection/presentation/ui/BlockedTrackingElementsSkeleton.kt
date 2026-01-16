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

package ch.protonmail.android.mailtrackingprotection.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.component.ShimmerBox
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens.MessageDetailsHeader.DetailsTitleWidth

private val SkeletonTextHeight = 14.dp
private val TitleWidth = 120.dp
private val SubtitleWidth = 180.dp
private val LinkWidth = 80.dp

@Composable
internal fun BlockedTrackingElementsSkeleton(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.ModeratelyLarge)
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier.width(DetailsTitleWidth),
                contentAlignment = Alignment.Center
            ) {
                ShimmerBox(
                    modifier = Modifier.size(ProtonDimens.IconSize.Small)
                )
            }
            Column {
                ShimmerBox(
                    modifier = Modifier.size(width = TitleWidth, height = SkeletonTextHeight)
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraTiny))

                ShimmerBox(
                    modifier = Modifier.size(width = SubtitleWidth, height = SkeletonTextHeight)
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

                ShimmerBox(
                    modifier = Modifier.size(width = LinkWidth, height = SkeletonTextHeight)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBlockedTrackingElementsSkeleton() {
    ProtonTheme {
        BlockedTrackingElementsSkeleton()
    }
}
