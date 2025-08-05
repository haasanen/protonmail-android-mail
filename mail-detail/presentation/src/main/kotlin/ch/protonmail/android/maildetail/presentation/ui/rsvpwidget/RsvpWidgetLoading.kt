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

package ch.protonmail.android.maildetail.presentation.ui.rsvpwidget

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens

@Composable
fun RsvpWidgetLoading() {
    Column(
        modifier = Modifier
            .padding(horizontal = ProtonDimens.Spacing.Large)
            .background(
                color = ProtonTheme.colors.backgroundNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .border(
                width = ProtonDimens.OutlinedBorderSize,
                color = ProtonTheme.colors.borderNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(ProtonDimens.Spacing.ExtraLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                SkeletonItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ExtraLargeItem)
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
                SkeletonItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MediumItem)
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
                SkeletonItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MediumSmallItem)
                )
            }
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))
            SkeletonItem(modifier = Modifier.size(MailDimens.RsvpCalendarLogoSize))
        }
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
        SkeletonItem(
            modifier = Modifier
                .fillMaxWidth(WIDTH_FRACTION)
                .height(SmallItem)
        )
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            SkeletonItem(
                modifier = Modifier
                    .weight(1f)
                    .height(MailDimens.RsvpButtonHeight),
                shape = ProtonTheme.shapes.massive
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
            SkeletonItem(
                modifier = Modifier
                    .weight(1f)
                    .height(MailDimens.RsvpButtonHeight),
                shape = ProtonTheme.shapes.massive
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
            SkeletonItem(
                modifier = Modifier
                    .weight(1f)
                    .height(MailDimens.RsvpButtonHeight),
                shape = ProtonTheme.shapes.massive
            )
        }
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
        repeat(REPEAT_ITEMS_VALUE) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                SkeletonItem(
                    modifier = Modifier.size(width = LargeItem, height = MediumSmallItem)
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))
                SkeletonItem(
                    modifier = Modifier
                        .weight(1f)
                        .height(MediumSmallItem)
                )
            }
            if (it != REPEAT_ITEMS_VALUE - 1) {
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
            }
        }
    }
}

@Composable
fun SkeletonItem(modifier: Modifier = Modifier, shape: Shape = ProtonTheme.shapes.extraLarge) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")

    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(ProtonTheme.colors.backgroundDeep.copy(alpha = alpha.value))
    )
}

@Preview
@Composable
fun RsvpWidgetLoadingPreview() {
    RsvpWidgetLoading()
}

private const val REPEAT_ITEMS_VALUE = 3
private const val WIDTH_FRACTION = 0.20f
private val SmallItem = 12.dp
private val MediumSmallItem = 14.dp
private val MediumItem = 16.dp
private val LargeItem = 20.dp
private val ExtraLargeItem = 22.dp
