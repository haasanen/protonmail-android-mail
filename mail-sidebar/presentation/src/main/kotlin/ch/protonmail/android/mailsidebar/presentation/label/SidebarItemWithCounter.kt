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

package ch.protonmail.android.mailsidebar.presentation.label

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import ch.protonmail.android.design.compose.component.ProtonListItem
import ch.protonmail.android.mailcommon.presentation.model.CappedNumberUiModel
import ch.protonmail.android.mailcommon.presentation.model.asDisplayText
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm

@Composable
fun SidebarItemWithCounter(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    iconTint: Color = LocalContentColor.current,
    count: CappedNumberUiModel = CappedNumberUiModel.Empty,
    onClick: () -> Unit = {}
) {

    ProtonListItem(
        modifier = modifier.semantics(mergeDescendants = true) {},
        onClick = onClick,
        isClickable = isClickable,
        isSelected = isSelected,
        icon = { defaultModifier ->
            Icon(
                modifier = defaultModifier.then(iconModifier),
                painter = icon,
                contentDescription = null,
                tint = iconTint
            )
        },
        text = { defaultModifier ->
            Text(
                modifier = defaultModifier.then(textModifier),
                text = text,
                color = if (isSelected) ProtonTheme.colors.sidebarTextSelected else ProtonTheme.colors.sidebarTextNorm,
                maxLines = 1,
                style = if (isSelected) ProtonTheme.typography.titleMedium else ProtonTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis
            )
        },
        count = {
            Text(
                modifier = Modifier
                    .testTag(SidebarItemWithCounterTestTags.Counter)
                    .padding(ProtonDimens.Spacing.Tiny)
                    .padding(horizontal = ProtonDimens.Spacing.Tiny),
                text = count.asDisplayText(),
                color = ProtonTheme.colors.sidebarTextSelected,
                style = ProtonTheme.typography.bodySmallNorm
            )

        }
    )
}

object SidebarItemWithCounterTestTags {

    const val Counter = "SidebarItemWithCounterItemCounter"
}
