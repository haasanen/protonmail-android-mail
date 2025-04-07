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

package me.proton.android.core.auth.presentation.signup.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.core.presentation.R

@Composable
fun NavigationBackButton(modifier: Modifier = Modifier, onBackClicked: () -> Unit = {}) {
    IconButton(
        modifier = modifier,
        onClick = onBackClicked
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = stringResource(id = R.string.presentation_back)
        )
    }
}

@Preview
@Composable
fun ProtonCloseButtonPreview() {
    me.proton.core.compose.theme.ProtonTheme {
        NavigationBackButton()
    }
}
