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

package ch.protonmail.android.mailcomposer.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.InstantWithFormattedTime
import ch.protonmail.android.mailcomposer.presentation.model.ScheduleSendOptionsUiModel
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.mailupselling.presentation.ui.UpsellingBottomSheetButton
import kotlin.time.Instant

@Composable
fun ScheduleSendBottomSheetContent(
    optionsUiModel: ScheduleSendOptionsUiModel,
    actions: ScheduleSendBottomSheetContent.Actions,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .padding(ProtonDimens.Spacing.Large)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .scrollable(
                    rememberScrollableState(consumeScrollDelta = { 0f }),
                    orientation = Orientation.Vertical
                )
        ) {
            Text(
                modifier = Modifier
                    .background(ProtonTheme.colors.backgroundInvertedNorm)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(R.string.composer_schedule_send_content_description),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ProtonTheme.typography.titleMediumNorm,
                fontWeight = FontWeight.SemiBold
            )

            optionsUiModel.previousScheduleSendTime?.let { previousTime ->

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

                PreviousTimeOption(
                    previousTime = previousTime,
                    onClicked = actions.onScheduleSendConfirmed
                )
            }

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

            DefaultOptions(optionsUiModel = optionsUiModel, onOptionClicked = actions.onScheduleSendConfirmed)

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

            if (optionsUiModel.isCustomTimeOptionAvailable) {
                CustomTimeOption(onPickCustomTime = actions.onPickCustomTimeRequested)
            } else {
                UpsellingBottomSheetButton(
                    text = stringResource(R.string.composer_schedule_send_custom_upsell_title),
                    onUpsellNavigation = { type -> actions.onNavigateToUpsell(type) }
                )
            }
        }
    }
}

object ScheduleSendBottomSheetContent {
    data class Actions(
        val onScheduleSendConfirmed: (Instant) -> Unit,
        val onPickCustomTimeRequested: () -> Unit,
        val onNavigateToUpsell: (type: UpsellingVisibility) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onScheduleSendConfirmed = { _ -> },
                onPickCustomTimeRequested = { },
                onNavigateToUpsell = { _ -> }
            )
        }
    }
}

@Composable
private fun PreviousTimeOption(
    onClicked: (Instant) -> Unit,
    previousTime: InstantWithFormattedTime,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.extraLarge)
            .clickable(
                role = Role.Button,
                onClick = { onClicked(previousTime.instant) }
            )
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(ProtonDimens.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.composer_schedule_send_previously_set),
                style = ProtonTheme.typography.bodyMediumWeak,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

            Text(
                modifier = Modifier,
                text = previousTime.formatted,
                style = ProtonTheme.typography.bodyLargeNorm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CustomTimeOption(onPickCustomTime: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.extraLarge)
            .clickable(
                role = Role.Button,
                onClick = onPickCustomTime
            )
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(ProtonDimens.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.composer_schedule_send_custom_title),
                style = ProtonTheme.typography.bodyLargeNorm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

            Text(
                modifier = Modifier,
                text = stringResource(R.string.composer_schedule_send_custom_description),
                style = ProtonTheme.typography.bodyMediumWeak,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = R.drawable.ic_proton_chevron_right),
            contentDescription = null,
            tint = ProtonTheme.colors.iconNorm
        )
    }
}

@Composable
private fun DefaultOptions(
    optionsUiModel: ScheduleSendOptionsUiModel,
    onOptionClicked: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Large)
    ) {
        MainOptionButton(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.composer_schedule_send_tomorrow_title),
            time = optionsUiModel.tomorrow.formatted,
            icon = R.drawable.ic_proton_sun,
            onClick = { onOptionClicked(optionsUiModel.tomorrow.instant) }
        )

        MainOptionButton(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.composer_schedule_send_monday_title),
            time = optionsUiModel.monday.formatted,
            icon = R.drawable.ic_proton_briefcase,
            onClick = { onOptionClicked(optionsUiModel.monday.instant) }
        )
    }
}

@Composable
private fun MainOptionButton(
    modifier: Modifier = Modifier,
    label: String,
    time: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ProtonTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(vertical = ProtonDimens.Spacing.ExtraLarge)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = ProtonTheme.colors.iconNorm
        )
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))
        Text(
            text = label,
            style = ProtonTheme.typography.bodyLargeNorm,
            maxLines = 1
        )
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))
        Text(
            text = time,
            style = ProtonTheme.typography.bodyMediumWeak,
            maxLines = 1
        )
    }
}

@Preview(
    showBackground = true,
    name = "Schedule send sheet with default options"
)
@Composable
private fun PreviewScheduleSendBottomSheet() {
    ProtonTheme {
        ScheduleSendBottomSheetContent(
            optionsUiModel = ScheduleSendOptionsUiModel(
                tomorrow = InstantWithFormattedTime(Instant.parse("05 Jun at 08:00"), "05 Jun at 08:00"),
                monday = InstantWithFormattedTime(Instant.parse("09 Jun at 08:00"), "09 Jun at 08:00"),
                isCustomTimeOptionAvailable = true,
                previousScheduleSendTime = null
            ),
            ScheduleSendBottomSheetContent.Actions.Empty
        )
    }
}

@Preview(
    showBackground = true,
    name = "Schedule send sheet with previous schedule send time"
)
@Composable
private fun PreviewScheduleSendSheetWithPreviousTime() {
    ProtonTheme {
        ScheduleSendBottomSheetContent(
            optionsUiModel = ScheduleSendOptionsUiModel(
                tomorrow = InstantWithFormattedTime(Instant.parse("05 Jun at 08:00"), "05 Jun at 08:00"),
                monday = InstantWithFormattedTime(Instant.parse("09 Jun at 08:00"), "09 Jun at 08:00"),
                isCustomTimeOptionAvailable = true,
                previousScheduleSendTime = InstantWithFormattedTime(Instant.parse("12 Jun at 09:45"), "12 Jun at 09:45")
            ),
            ScheduleSendBottomSheetContent.Actions.Empty
        )
    }
}
