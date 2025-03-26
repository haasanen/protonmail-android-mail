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

package ch.protonmail.android.uicomponents.chips

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.material3.InputChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.uicomponents.chips.icons.LeadingChipIcon
import ch.protonmail.android.uicomponents.chips.item.ChipItem
import ch.protonmail.android.uicomponents.chips.item.inputChipColor
import ch.protonmail.android.uicomponents.chips.item.inputChipBorder
import ch.protonmail.android.uicomponents.chips.item.suggestionChipColor
import ch.protonmail.android.uicomponents.chips.item.suggestionsTextStyle
import ch.protonmail.android.uicomponents.chips.item.textStyle

@Composable
internal fun FocusedChipsList(
    chipItems: List<ChipItem>,
    animateChipsCreation: Boolean = false,
    textMaxWidth: Dp,
    onDeleteItem: (Int) -> Unit
) {
    chipItems.forEachIndexed { index, chipItem ->
        val scale by remember { mutableStateOf(Animatable(0F)) }
        val alpha by remember { mutableStateOf(Animatable(0F)) }
        InputChip(
            modifier = Modifier
                .testTag("${ChipsTestTags.InputChip}$index")
                .semantics { isValidField = chipItem !is ChipItem.Invalid }
                .padding(horizontal = 4.dp)
                .thenIf(animateChipsCreation) {
                    scale(scale.value)
                    alpha(alpha.value)
                },
            selected = false,
            onClick = { onDeleteItem(index) },
            label = {
                Text(
                    modifier = Modifier
                        .testTag(ChipsTestTags.InputChipText)
                        .widthIn(max = textMaxWidth - 64.dp)
                        .padding(vertical = ProtonDimens.Spacing.MediumLight),
                    text = chipItem.value,
                    style = chipItem.textStyle()
                )

            },
            shape = ProtonTheme.shapes.huge,
            colors = inputChipColor(),
            border = inputChipBorder(chipItem),
            leadingIcon = { LeadingChipIcon(chipItem) }

        )
        LaunchedEffect(key1 = index) {
            if (animateChipsCreation) {
                scale.animateTo(1F)
                alpha.animateTo(1F)
            }
        }
    }
}

@Composable
@Suppress("MagicNumber")
internal fun UnFocusedChipsList(
    itemChip: ChipItem,
    counterChip: ChipItem? = null,
    onChipClick: () -> Unit = {}
) {
    Row {
        SuggestionChip(
            modifier = Modifier
                .testTag(ChipsTestTags.BaseSuggestionChip)
                .semantics { isValidField = itemChip !is ChipItem.Invalid }
                .weight(1f, fill = false)
                .padding(horizontal = ProtonDimens.Spacing.Small),
            onClick = onChipClick,
            label = {
                Text(
                    modifier = Modifier
                        .testTag(ChipsTestTags.InputChipText)
                        .padding(vertical = ProtonDimens.Spacing.MediumLight),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = itemChip.value,
                    style = itemChip.textStyle()
                )
            },
            colors = suggestionChipColor(),
            icon = { LeadingChipIcon(itemChip) },
            shape = ProtonTheme.shapes.huge,
            border = inputChipBorder(itemChip)
        )
        if (counterChip != null) {
            SuggestionChip(
                modifier = Modifier
                    .testTag(ChipsTestTags.AdditionalSuggestionChip)
                    .semantics { isValidField = itemChip !is ChipItem.Invalid }
                    .padding(horizontal = ProtonDimens.Spacing.Small),
                onClick = onChipClick,
                label = {
                    Text(
                        modifier = Modifier
                            .testTag(ChipsTestTags.InputChipText)
                            .padding(vertical = ProtonDimens.Spacing.MediumLight),
                        maxLines = 1,
                        text = counterChip.value,
                        style = itemChip.suggestionsTextStyle()
                    )
                },
                colors = suggestionChipColor(),
                shape = ProtonTheme.shapes.huge,
                border = inputChipBorder(itemChip)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun FocusedChipsListPreview() {
    ProtonTheme {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            FocusedChipsList(
                chipItems = listOf(
                    ChipItem.Valid("john@example.com"),
                    ChipItem.Invalid("not-an-email"),
                    ChipItem.Counter("+3")
                ),
                animateChipsCreation = false,
                textMaxWidth = 200.dp,
                onDeleteItem = {}
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun UnfocusedValidChipsListPreview() {
    ProtonTheme {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            UnFocusedChipsList(
                itemChip = ChipItem.Valid("john@example.com")
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun UnfocusedInvalidChipsListPreview() {
    ProtonTheme {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            UnFocusedChipsList(
                itemChip = ChipItem.Invalid("invalid@email.com")
            )
        }
    }
}
