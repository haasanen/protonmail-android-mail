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

package ch.protonmail.android.uicomponents.chips.item

import androidx.compose.material3.ChipColors
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeInverted
import ch.protonmail.android.design.compose.theme.bodyLargeNorm

@Composable
internal fun ChipItem.textStyle() = when (this) {
    is ChipItem.Invalid -> ProtonTheme.typography.bodyLargeInverted
    else -> ProtonTheme.typography.bodyLargeNorm
}

@Composable
internal fun ChipItem.suggestionsTextStyle() = ProtonTheme.typography.bodyLargeNorm

@Composable
internal fun inputChipColor(chipItem: ChipItem): SelectableChipColors {
    val containerColor = when (chipItem) {
        is ChipItem.Invalid -> ProtonTheme.colors.notificationError
        is ChipItem.Counter,
        is ChipItem.Valid -> ProtonTheme.colors.shade20
    }

    return InputChipDefaults.inputChipColors().copy(containerColor = containerColor)
}

@Composable
internal fun suggestionChipColor(chipItem: ChipItem): ChipColors {
    val containerColor = when (chipItem) {
        is ChipItem.Invalid -> ProtonTheme.colors.notificationError
        is ChipItem.Counter,
        is ChipItem.Valid -> ProtonTheme.colors.shade20
    }

    return SuggestionChipDefaults.suggestionChipColors().copy(containerColor = containerColor)
}
