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

package ch.protonmail.android.mailtrackingprotection.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens.MessageDetailsHeader.DetailsTitleWidth
import ch.protonmail.android.mailcommon.presentation.compose.SmallNonClickableIcon
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedElementsUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedTrackersState

@Composable
fun BlockedTrackingElements(
    messageId: MessageId,
    onBlockedTrackersClick: (BlockedElementsUiModel) -> Unit,
    onNoBlockedTrackersClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel = hiltViewModel<BlockedTrackersViewModel, BlockedTrackersViewModel.Factory>(
        key = messageId.id
    ) { factory ->
        factory.create(messageId)
    }

    val state = viewModel.state.collectAsStateWithLifecycle()

    when (val current = state.value) {
        BlockedTrackersState.NoTrackersBlocked -> NoBlockedTrackers(onNoBlockedTrackersClick, modifier)
        is BlockedTrackersState.TrackersBlocked ->
            BlockedTrackingElements(current.uiModel, onBlockedTrackersClick, modifier)

        BlockedTrackersState.Unknown -> {}
    }
}

@Composable
private fun BlockedTrackingElements(
    uiModel: BlockedElementsUiModel,
    onBlockedTrackersClick: (BlockedElementsUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
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
                SmallNonClickableIcon(
                    iconId = R.drawable.ic_shield_2_check_filled,
                    iconColor = ProtonTheme.colors.iconNorm
                )
            }
            Column {
                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = stringResource(R.string.trackers_protection),
                    style = ProtonTheme.typography.bodySmallNorm
                )

                val trackersText = pluralStringResource(
                    R.plurals.number_of_blocked_trackers,
                    uiModel.trackers.items.size,
                    uiModel.trackers.items.size
                )
                val linksText = pluralStringResource(
                    R.plurals.number_of_cleaned_links,
                    uiModel.links.items.size,
                    uiModel.links.items.size
                )

                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = "$trackersText, $linksText",
                    style = ProtonTheme.typography.bodySmallNorm
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clickable(onClick = { onBlockedTrackersClick(uiModel) }),
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
private fun NoBlockedTrackers(onNoBlockedTrackersClick: () -> Unit, modifier: Modifier = Modifier) {
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
                SmallNonClickableIcon(
                    iconId = R.drawable.ic_shield_2_check_filled,
                    iconColor = ProtonTheme.colors.iconNorm
                )
            }
            Column {

                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = stringResource(R.string.trackers_protection_no_trackers_detected),
                    style = ProtonTheme.typography.bodySmallNorm
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clickable(onClick = onNoBlockedTrackersClick),
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
private fun PreviewBlockedTrackers() {
    BlockedTrackingElements(
        uiModel = TrackersUiModelSample.trackersAndLinks,
        onBlockedTrackersClick = {}
    )
}

@Preview
@Composable
private fun PreviewNoBlockedTrackers() {
    NoBlockedTrackers(onNoBlockedTrackersClick = {})
}
