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

package ch.protonmail.android.mailcomposer.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.labelLargeInverted
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.uicomponents.thenIf

@Composable
@Suppress("UseComposableActions")
internal fun ComposerTopBar(
    onCloseComposerClick: () -> Unit,
    onSendMessageComposerClick: () -> Unit,
    onScheduleSendClick: () -> Unit,
    isSendMessageEnabled: Boolean
) {
    ProtonTopAppBar(
        modifier = Modifier.testTag(ComposerTestTags.TopAppBar),
        title = {},
        navigationIcon = {
            IconButton(
                modifier = Modifier
                    .testTag(ComposerTestTags.CloseButton)
                    .size(ProtonDimens.IconSize.ExtraLarge),
                onClick = onCloseComposerClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_cross_big),
                    tint = ProtonTheme.colors.iconNorm,
                    contentDescription = stringResource(R.string.close_composer_content_description)
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = ProtonDimens.Spacing.Medium)
            ) {
                IconButton(
                    modifier = Modifier
                        .size(ProtonDimens.IconSize.ExtraLarge)
                        .thenIf(!isSendMessageEnabled) { semantics { disabled() } },
                    enabled = isSendMessageEnabled,
                    onClick = onScheduleSendClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = ProtonTheme.colors.textAccent,
                        disabledContentColor = ProtonTheme.colors.brandMinus20
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_proton_clock_paper_plane),
                        contentDescription = stringResource(R.string.schedule_send_content_description)
                    )
                }
                Button(
                    onClick = onSendMessageComposerClick,
                    enabled = isSendMessageEnabled,
                    modifier = Modifier
                        .testTag(ComposerTestTags.SendButton)
                        .thenIf(!isSendMessageEnabled) { semantics { disabled() } },
                    shape = ProtonTheme.shapes.huge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProtonTheme.colors.interactionBrandDefaultNorm,
                        disabledContainerColor = ProtonTheme.colors.interactionBrandWeakDisabled,
                        contentColor = ProtonTheme.colors.textAccent,
                        disabledContentColor = ProtonTheme.colors.brandMinus20
                    ),
                    contentPadding = PaddingValues(
                        horizontal = ProtonDimens.Spacing.Large,
                        vertical = ProtonDimens.Spacing.Standard
                    )
                ) {
                    Text(
                        text = stringResource(R.string.send_button_title),
                        style = ProtonTheme.typography.labelLargeInverted
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun ComposerTopBarPreviewSendButtonDisabled() {
    ProtonTheme {
        ComposerTopBar(
            onCloseComposerClick = {},
            onSendMessageComposerClick = {},
            onScheduleSendClick = {},
            isSendMessageEnabled = false
        )
    }
}


@Preview
@Composable
private fun ComposerTopBarPreviewSendButtonEnabled() {
    ProtonTheme {
        ComposerTopBar(
            onCloseComposerClick = {},
            onSendMessageComposerClick = {},
            onScheduleSendClick = {},
            isSendMessageEnabled = true
        )
    }
}

@Preview
@Composable
private fun PreviewScheduleSendDisabled() {
    ProtonTheme {
        ComposerTopBar(
            onCloseComposerClick = {},
            onSendMessageComposerClick = {},
            onScheduleSendClick = {},
            isSendMessageEnabled = true
        )
    }
}
