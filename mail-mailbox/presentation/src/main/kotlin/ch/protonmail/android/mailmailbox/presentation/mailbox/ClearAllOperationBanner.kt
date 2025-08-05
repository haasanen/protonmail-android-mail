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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonBanner
import ch.protonmail.android.design.compose.component.ProtonBannerWithButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailcommon.presentation.AdaptivePreviews
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.mailupselling.presentation.ui.screen.UpsellBannerButton

@Composable
internal fun ClearAllOperationBanner(
    viewModel: ClearAllOperationViewModel = hiltViewModel(),
    actions: ClearAllOperationBanner.Actions
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is ClearAllStateUiModel.Hidden -> Unit
        else -> ClearAllOperationBannerContent(state, actions)
    }
}

internal object ClearAllOperationBanner {

    data class Actions(
        val onUpselling: (type: UpsellingVisibility) -> Unit,
        val onClearAll: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onUpselling = { _ -> },
                onClearAll = {}
            )
        }
    }
}

@Composable
private fun ClearAllOperationBannerContent(state: ClearAllStateUiModel, actions: ClearAllOperationBanner.Actions) {
    when (state) {
        is ClearAllStateUiModel.Visible.ClearAllBannerWithButton -> ClearBannerWithButton(state, actions.onClearAll)
        is ClearAllStateUiModel.Visible.UpsellBanner -> ClearUpsellBanner(state, actions.onUpselling)
        is ClearAllStateUiModel.Hidden -> Unit
    }
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
private fun ClearUpsellBanner(
    upsellBannerState: ClearAllStateUiModel.Visible.UpsellBanner,
    onButtonClicked: (type: UpsellingVisibility) -> Unit
) {
    val contentPadding = PaddingValues(ProtonDimens.Spacing.ModeratelyLarge)

    ProtonBanner(
        modifier = Modifier,
        icon = upsellBannerState.icon,
        iconTint = ProtonTheme.colors.iconWeak,
        iconSize = ProtonDimens.IconSize.Medium,
        text = upsellBannerState.bannerText.string(),
        textStyle = ProtonTheme.typography.bodyMediumWeak,
        backgroundColor = ProtonTheme.colors.backgroundNorm,
        contentPadding = contentPadding
    ) {
        UpsellBannerButton(
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            ctaText = upsellBannerState.upgradeButtonText.string(), onClick = onButtonClicked
        )
    }
}

@AdaptivePreviews
@Composable
private fun ClearAllBannerPreview(
    @PreviewParameter(ClearAllOperationBannerPreviewData::class) uiModel: ClearAllStateUiModel
) {
    ProtonTheme {
        ClearAllOperationBannerContent(
            state = uiModel,
            actions = ClearAllOperationBanner.Actions.Empty
        )
    }
}
