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

package ch.protonmail.android.mailupselling.presentation.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.mailupselling.presentation.ui.UpsellingLayoutValues
import ch.protonmail.android.mailupselling.presentation.viewmodel.UpsellingButtonViewModel

@Composable
fun UpsellBannerButton(
    ctaText: String,
    modifier: Modifier = Modifier,
    onClick: (type: UpsellingVisibility) -> Unit = {},
    viewModel: UpsellingButtonViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val type = state.value.visibility

    val onNavigateToUpsell: () -> Unit = {
        when (state.value.visibility) {
            UpsellingVisibility.HIDDEN -> Unit

            UpsellingVisibility.PROMO,
            UpsellingVisibility.NORMAL -> onClick(type)
        }
    }

    UpsellBannerButtonContent(modifier = modifier, onClick = onNavigateToUpsell) {
        Text(text = ctaText, style = ProtonTheme.typography.bodyMediumNorm)
    }
}

@Composable
private fun UpsellBannerButtonContent(
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(ProtonDimens.CornerRadius.Huge)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ProtonDimens.Spacing.Massive)
            .border(UpsellingLayoutValues.UpsellCards.outlineBorderStoke, shape)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box { content() }
    }
}

@Preview(name = "Light mode", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UpsellButtonPreview() {
    ProtonTheme {
        UpsellBannerButton(ctaText = "Upgrade to Auto-delete")
    }
}
