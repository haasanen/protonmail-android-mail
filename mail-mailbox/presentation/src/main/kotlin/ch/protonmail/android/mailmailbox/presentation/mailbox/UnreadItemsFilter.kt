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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.mapper.UnreadCountValueMapper
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.PreviewData.DummyUnreadCount
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.UnreadFilterState
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak

@Composable
fun UnreadItemsFilter(
    modifier: Modifier = Modifier,
    state: UnreadFilterState,
    onFilterEnabled: () -> Unit,
    onFilterDisabled: () -> Unit
) {
    when (state) {
        is UnreadFilterState.Loading -> {
            ProtonCenteredProgress(modifier = Modifier.size(MailDimens.ProgressDefaultSize))
        }

        is UnreadFilterState.Data -> {
            FilterChip(
                modifier = modifier.testTag(UnreadItemsFilterTestTags.UnreadFilterChip),
                colors = chipColors().copy(
                    containerColor = ProtonTheme.colors.backgroundNorm,
                    selectedContainerColor = ProtonTheme.colors.interactionWeakPressed
                ),
                shape = ProtonTheme.shapes.huge,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = state.isFilterEnabled,
                    borderWidth = ProtonDimens.OutlinedBorderSize,
                    borderColor = ProtonTheme.colors.borderNorm
                ),
                selected = state.isFilterEnabled,
                onClick = {
                    if (state.isFilterEnabled) {
                        onFilterDisabled()
                    } else {
                        onFilterEnabled()
                    }
                },
                elevation = null,
                leadingIcon = {},
                label = {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.filter_unread_button_text,
                            count = state.numUnread,
                            UnreadCountValueMapper.toCappedValue(state.numUnread)
                        ),
                        style = ProtonTheme.typography.bodyMediumWeak
                    )
                }
            )
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = ProtonTheme.colors.iconAccent,
    selectedLabelColor = ProtonTheme.colors.textInverted,
    containerColor = ProtonTheme.colors.backgroundSecondary,
    labelColor = ProtonTheme.colors.textAccent
)

@Preview(showBackground = true)
@Composable
fun InactiveUnreadFilterButtonPreview() {
    UnreadItemsFilter(
        state = UnreadFilterState.Data(DummyUnreadCount, false),
        onFilterEnabled = {},
        onFilterDisabled = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ActiveUnreadFilterButtonPreview() {
    UnreadItemsFilter(
        state = UnreadFilterState.Data(DummyUnreadCount, true),
        onFilterEnabled = {},
        onFilterDisabled = {}
    )
}

private object PreviewData {

    const val DummyUnreadCount = 4
}

object UnreadItemsFilterTestTags {

    const val UnreadFilterChip = "UnreadFilterChip"
}
