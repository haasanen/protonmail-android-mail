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

package me.proton.android.core.payment.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonSolidButton
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing.Medium
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.payment.presentation.R

@Composable
fun ProtonErrorRetryLayout(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.payment_error_title),
    description: String = stringResource(R.string.payment_error_description),
    image: Painter = painterResource(R.drawable.ic_proton_bug),
    buttonText: String = stringResource(R.string.presentation_retry),
    onClick: () -> Unit = {}
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterVertically)
        ) {
            Image(
                modifier = Modifier.align(CenterHorizontally).padding(Medium),
                painter = image,
                contentDescription = null
            )
            Text(
                modifier = Modifier.align(CenterHorizontally).padding(Medium),
                text = title,
                style = ProtonTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.align(CenterHorizontally).padding(Medium),
                text = description,
                style = ProtonTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            ProtonSolidButton(
                modifier = Modifier.align(CenterHorizontally).padding(Medium),
                onClick = { onClick() }
            ) {
                Text(
                    modifier = Modifier.align(CenterHorizontally),
                    text = buttonText
                )
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
private fun ProtonErrorRetryLayoutPreview() {
    ProtonTheme {
        ProtonErrorRetryLayout()
    }
}
