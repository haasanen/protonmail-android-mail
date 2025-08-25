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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.string

@Composable
internal fun ToolbarActionSetItem(action: ActionUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProtonTheme.colors.backgroundInvertedSecondary,
        shape = RoundedCornerShape(ProtonDimens.Spacing.Standard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProtonDimens.Spacing.Large, vertical = ProtonDimens.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(action.icon),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Large))

            Text(
                text = action.description.string(),
                style = ProtonTheme.typography.bodyLarge,
                color = ProtonTheme.colors.textNorm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
