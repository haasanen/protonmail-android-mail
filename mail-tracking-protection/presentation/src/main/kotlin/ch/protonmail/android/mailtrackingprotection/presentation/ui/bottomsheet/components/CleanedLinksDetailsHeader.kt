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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.mailtrackingprotection.presentation.R
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinksUiModel

@Composable
internal fun CleanedLinksDetailsHeader(
    links: CleanedLinksUiModel,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(
                enabled = links.isExpandable,
                onClick = onClick,
                interactionSource = null,
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.number_of_cleaned_links,
                links.items.size,
                links.items.size
            ),
            style = ProtonTheme.typography.bodyLargeNorm
        )

        Spacer(modifier = Modifier.weight(1f))

        if (links.isExpandable) {
            val chevronIcon = when {
                isExpanded -> R.drawable.ic_proton_chevron_down
                else -> R.drawable.ic_proton_chevron_up
            }
            Icon(
                painter = painterResource(id = chevronIcon),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
        }
    }
}
