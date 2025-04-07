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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonBanner
import ch.protonmail.android.design.compose.component.ProtonBannerWithButton
import ch.protonmail.android.design.compose.component.ProtonBannerWithLink
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel

@Composable
internal fun ClearAllOperationBanner(
    viewModel: ClearAllOperationViewModel = hiltViewModel(),
    showMissingFeatureSnackbar: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    val actions = ClearAllOperationBanner.Actions.Empty.copy(
        onClear = showMissingFeatureSnackbar
    )

    if (state != ClearAllState.Hidden) {
        ClearAllOperationBannerContent(state, actions)
    }
}

internal object ClearAllOperationBanner {

    data class Actions(
        val onUpselling: () -> Unit,
        val onClear: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onUpselling = {},
                onClear = {}
            )
        }
    }
}

@Composable
private fun ClearAllOperationBannerContent(state: ClearAllStateUiModel, actions: ClearAllOperationBanner.Actions) {
    when (state) {
        is ClearAllStateUiModel.Visible.ClearAllBannerWithButton -> ClearBannerWithButton(state, actions.onClear)
        is ClearAllStateUiModel.Visible.InfoBanner -> ClearInfoBanner(state.text)
        is ClearAllStateUiModel.Visible.UpsellBannerWithLink -> ClearUpsellBannerWithLink(state, actions)
        is ClearAllStateUiModel.Hidden -> Unit
    }
}

@Composable
private fun ClearInfoBanner(text: TextUiModel) {
    ProtonBanner(
        text = text.string(),
        textStyle = ProtonTheme.typography.bodyMediumWeak,
        backgroundColor = ProtonTheme.colors.backgroundNorm
    )
}

@Composable
private fun ClearBannerWithButton(
    clearBannerState: ClearAllStateUiModel.Visible.ClearAllBannerWithButton,
    onClick: () -> Unit
) {
    ProtonBannerWithButton(
        bannerText = clearBannerState.bannerText.string(),
        buttonText = clearBannerState.buttonText.string(),
        icon = clearBannerState.icon,
        onButtonClicked = onClick
    )
}

@Composable
private fun ClearUpsellBannerWithLink(
    upsellBannerState: ClearAllStateUiModel.Visible.UpsellBannerWithLink,
    actions: ClearAllOperationBanner.Actions
) {
    ProtonBannerWithLink(
        bannerText = upsellBannerState.bannerText.string(),
        linkText = upsellBannerState.linkText.string(),
        icon = upsellBannerState.icon,
        contentPadding = PaddingValues(
            start = ProtonDimens.Spacing.ModeratelyLarge,
            end = ProtonDimens.Spacing.ModeratelyLarge,
            top = ProtonDimens.Spacing.ModeratelyLarge,
            bottom = 0.dp
        ),
        onLinkClicked = actions.onUpselling
    )
}
