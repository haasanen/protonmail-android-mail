/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.design.compose.component.appbar

import android.content.res.Configuration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.R
import ch.protonmail.android.design.compose.theme.ProtonTheme

@Composable
fun ProtonNavigationIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    painter: Painter = painterResource(id = R.drawable.ic_proton_close),
    contentDescription: String? = stringResource(id = R.string.presentation_close)
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            tint = ProtonTheme.colors.iconNorm,
            painter = painter,
            contentDescription = contentDescription
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
private fun ProtonNavigationIconPreview() {
    ProtonTheme { ProtonNavigationIcon() }
}
