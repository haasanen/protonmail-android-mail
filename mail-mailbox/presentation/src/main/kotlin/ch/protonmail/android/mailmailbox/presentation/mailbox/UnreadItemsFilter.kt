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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.StiffnessHigh
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.PreviewData.DummyUnreadCount
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.UnreadFilterState

@Composable
fun UnreadItemsFilter(
    modifier: Modifier = Modifier,
    state: UnreadFilterState,
    onFilterEnabled: () -> Unit,
    onFilterDisabled: () -> Unit
) {
    when (state) {
        is UnreadFilterState.Loading -> {
            Spacer(modifier = modifier.fillMaxWidth())
        }

        is UnreadFilterState.Data -> {
            FilterChip(
                modifier = modifier
                    .animateContentSize(
                        animationSpec =
                        spring(
                            stiffness = StiffnessHigh,
                            dampingRatio = DampingRatioLowBouncy,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        )
                    )
                    .testTag(UnreadItemsFilterTestTags.UnreadFilterChip),
                colors = chipColors().copy(
                    containerColor = ProtonTheme.colors.backgroundNorm,
                    selectedContainerColor = ProtonTheme.colors.interactionBrandWeakNorm
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
                trailingIcon = addCloseIconForEnabledState(state),
                elevation = null,
                leadingIcon = {},
                label = {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.filter_unread_button_text))
                            append(" ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(state.numUnread.toString())
                            }
                        },
                        style = ProtonTheme.typography.bodyMediumNorm
                    )
                }
            )
        }
    }
}


@Composable
private fun addCloseIconForEnabledState(state: UnreadFilterState.Data): @Composable (() -> Unit)? {
    return if (state.isFilterEnabled) {
        {
            Icon(
                modifier = Modifier.size(ProtonDimens.IconSize.Medium),
                painter = painterResource(R.drawable.ic_proton_cross_small),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
        }
    } else {
        null
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
