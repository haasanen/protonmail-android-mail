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

package ch.protonmail.android.mailblockedtrackers.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.design.compose.theme.bodySmallWeak
import ch.protonmail.android.mailblockedtrackers.presentation.model.BlockedTrackersState
import ch.protonmail.android.mailblockedtrackers.presentation.model.TrackersUiModel
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens.MessageDetailsHeader.DetailsTitleWidth
import ch.protonmail.android.mailcommon.presentation.compose.SmallNonClickableIcon
import ch.protonmail.android.mailmessage.domain.model.MessageId

@Composable
fun BlockedTrackers(
    messageId: MessageId,
    viewModel: BlockedTrackersViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {

    val state = viewModel.state.collectAsState()

    LaunchedEffect(messageId) {
        viewModel.loadBlockedTrackers(messageId)
    }

    when (val current = state.value) {
        BlockedTrackersState.NoTrackersBlocked -> NoBlockedTrackers(modifier)
        is BlockedTrackersState.TrackersBlocked -> BlockedTrackers(current.uiModel, modifier)
        BlockedTrackersState.Unknown -> { }
    }
}

@Composable
fun BlockedTrackers(uiModel: TrackersUiModel, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.ModeratelyLarge)) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(DetailsTitleWidth),
                contentAlignment = Alignment.Center
            ) {
                SmallNonClickableIcon(
                    iconId = R.drawable.ic_shield_2_check_filled
                )
            }
            Column {

                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = stringResource(R.string.trackers_protection),
                    style = ProtonTheme.typography.bodySmallWeak
                )

                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = stringResource(
                        R.string.trackers_blocked_links_cleaned_numbers,
                        uiModel.blocked.size,
                        uiModel.links.size
                    ),
                    style = ProtonTheme.typography.bodySmallWeak
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = stringResource(R.string.show_details),
                    style = ProtonTheme.typography.bodySmallNorm.copy(
                        color = ProtonTheme.colors.iconAccent
                    )
                )
            }
        }
    }
}

@Composable
fun NoBlockedTrackers(modifier: Modifier = Modifier) {

    Column(verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.ModeratelyLarge)) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(DetailsTitleWidth),
                contentAlignment = Alignment.Center
            ) {
                SmallNonClickableIcon(
                    iconId = R.drawable.ic_shield_2_check_filled
                )
            }
            Column {

                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = stringResource(R.string.trackers_protection_no_trackers_detected),
                    style = ProtonTheme.typography.bodySmallWeak
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = stringResource(R.string.show_details),
                    style = ProtonTheme.typography.bodySmallNorm.copy(
                        color = ProtonTheme.colors.iconAccent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBlockedTrackers() {
    BlockedTrackers(
        uiModel = TrackersUiModelSample.oneTrackerBlocked
    )
}

@Preview
@Composable
fun PreviewNoBlockedTrackers() {
    NoBlockedTrackers()
}
