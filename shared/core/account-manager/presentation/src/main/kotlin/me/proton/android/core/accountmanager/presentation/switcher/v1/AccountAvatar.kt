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

package me.proton.android.core.accountmanager.presentation.switcher.v1

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import ch.protonmail.android.design.compose.component.ProtonButton
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.accountmanager.domain.model.CoreAccountAvatarItem

@Composable
fun AccountAvatar(
    accountItem: CoreAccountAvatarItem?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boxColor = accountItem?.color?.toColorInt()?.let { Color(it) }
        ?: LocalColors.current.interactionBrandDefaultNorm
    ProtonButton(
        onClick = onClick,
        elevation = null,
        shape = ProtonTheme.shapes.large,
        border = null,
        colors = ButtonDefaults.buttonColors(containerColor = boxColor),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .size(ProtonDimens.DefaultButtonMinHeight)
            .padding(ProtonDimens.Spacing.Compact)
    ) {
        Text(text = accountItem?.initials ?: "")
    }
}

@Composable
@Preview
fun AccountAvatarPreview() {
    ProtonTheme {
        ProtonTopAppBar(
            title = { Text(text = "Proton Mail") },
            navigationIcon = {},
            actions = {
                AccountAvatar(
                    accountItem = CoreAccountAvatarItem(
                        initials = "J",
                        color = "#fa7bc9"
                    ),
                    onClick = {}
                )
            }
        )
    }
}
