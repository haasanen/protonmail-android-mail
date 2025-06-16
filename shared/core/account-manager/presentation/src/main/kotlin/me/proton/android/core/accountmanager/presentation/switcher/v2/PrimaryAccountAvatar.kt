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

package me.proton.android.core.accountmanager.presentation.switcher.v2

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.toColorInt
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.headlineSmallNorm
import me.proton.android.core.accountmanager.presentation.AccountDimens
import me.proton.android.core.accountmanager.presentation.R

@Composable
fun PrimaryAccountAvatar(
    modifier: Modifier = Modifier,
    backgroundColor: String?,
    iconResId: Int? = null,
    initials: String
) {
    val boxColor = backgroundColor?.toColorInt()?.let { Color(it) } ?: ProtonTheme.colors.interactionBrandDefaultNorm
    Box(
        modifier = modifier
            .sizeIn(
                minWidth = AccountDimens.PrimaryAccountAvatarSize,
                minHeight = AccountDimens.PrimaryAccountAvatarSize
            )
            .background(color = boxColor, shape = ProtonTheme.shapes.jumbo)
            .clip(shape = ProtonTheme.shapes.jumbo),
        contentAlignment = Alignment.Center
    ) {
        if (iconResId == null) {
            Text(
                textAlign = TextAlign.Center,
                style = ProtonTheme.typography.headlineSmallNorm,
                color = ProtonTheme.colors.textInverted,
                text = initials
            )
        } else {
            Icon(
                tint = Color.White,
                painter = painterResource(id = iconResId),
                contentDescription = null
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun PrimaryAccountInitialsAvatarPreview() {
    PrimaryAccountAvatar(
        initials = "DK",
        backgroundColor = null
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun PrimaryAccountIconAvatarPreview() {
    PrimaryAccountAvatar(
        iconResId = R.drawable.ic_proton_users,
        initials = "DK",
        backgroundColor = null
    )
}
