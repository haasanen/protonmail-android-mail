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

package ch.protonmail.android.mailpadlocks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.headlineSmallNorm
import ch.protonmail.android.design.compose.theme.labelLargeNorm
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoUiModel
import ch.protonmail.android.uicomponents.BottomNavigationBarSpacer

@Composable
fun EncryptionInfoBottomSheetContent(
    state: EncryptionInfoSheetState,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {

    when (state) {
        is EncryptionInfoSheetState.Requested -> EncryptionInfoBottomSheetContent(
            state.uiModel,
            onDismissed,
            modifier
        )
    }
}

@Composable
private fun EncryptionInfoBottomSheetContent(
    uiModel: EncryptionInfoUiModel,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ProtonDimens.Spacing.ExtraLarge)
    ) {

        Header(uiModel)

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        CloseButton(onClick = onDismissed)

        BottomNavigationBarSpacer()
    }
}

@Composable
private fun Header(uiModel: EncryptionInfoUiModel) {

    Column {
        HeaderIcon(uiModel)

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        Text(
            text = stringResource(uiModel.title),
            style = ProtonTheme.typography.headlineSmallNorm
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        HeaderDescription(uiModel)
    }
}

@Composable
private fun HeaderDescription(uiModel: EncryptionInfoUiModel) {
    val text = stringResource(uiModel.description)
    val linkText = stringResource(R.string.padlocks_learn_more)
    val linkUrl = stringResource(uiModel.link)
    val linkColor = ProtonTheme.colors.interactionBrandDefaultNorm

    val annotatedString = remember(text, linkText) {
        buildAnnotatedString {
            append(text)
            append(" ")
            withLink(
                LinkAnnotation.Url(
                    url = linkUrl,
                    styles = TextLinkStyles(
                        style = SpanStyle(color = linkColor)
                    )
                )
            ) {
                append(linkText)
            }
        }
    }
    Text(
        text = annotatedString,
        style = ProtonTheme.typography.bodyLargeNorm
    )
}

@Composable
private fun HeaderIcon(uiModel: EncryptionInfoUiModel) {
    Box(
        modifier = Modifier
            .size(ProtonDimens.IconSize.Huge)
            .clip(ProtonTheme.shapes.large)
            .background(
                color = ProtonTheme.colors.backgroundDeep,
                shape = ProtonTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(uiModel.icon),
            contentDescription = null,
            tint = colorResource(uiModel.color),
            modifier = Modifier.size(ProtonDimens.IconSize.Large)
        )
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors().copy(containerColor = ProtonTheme.colors.interactionBrandDefaultNorm),
        shape = ProtonTheme.shapes.huge,
        contentPadding = PaddingValues(ProtonDimens.Spacing.Large)
    ) {
        Text(
            text = stringResource(R.string.padlocks_close_button_text),
            style = ProtonTheme.typography.labelLargeNorm.copy(color = ProtonTheme.colors.iconInverted)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewEncryptionInfoBottomSheet() {
    EncryptionInfoBottomSheetContent(
        state = EncryptionInfoSheetState.Requested(EncryptionInfoUiModel.PgpE2eeWithFailedVerification),
        onDismissed = {}
    )
}
