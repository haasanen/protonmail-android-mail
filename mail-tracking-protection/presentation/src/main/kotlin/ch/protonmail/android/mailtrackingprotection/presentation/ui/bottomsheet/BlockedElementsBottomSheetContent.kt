/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ch.protonmail.android.mailtrackingprotection.presentation.R
import ch.protonmail.android.mailtrackingprotection.presentation.TrackersUiModelSample
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedElementsSheetState
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedElementsUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet.components.BlockedTrackersDetails
import ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet.components.CleanedLinksDetails
import ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet.components.CloseButton
import ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet.components.TrackerUrlDialog
import ch.protonmail.android.uicomponents.BottomNavigationBarSpacer

@Composable
fun BlockedElementsBottomSheetContent(
    state: BlockedElementsSheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    when (state) {
        is BlockedElementsSheetState.Requested -> when {
            state.elements != null -> BlockedElementsBottomSheetContent(state.elements, onDismiss, modifier)
            else -> NoBlockedElementsBottomSheetContent(modifier, onDismiss)
        }
    }
}

@Composable
private fun NoBlockedElementsBottomSheetContent(modifier: Modifier = Modifier, onDismiss: () -> Unit) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.ExtraLarge, vertical = ProtonDimens.Spacing.Tiny)
            .padding(bottom = ProtonDimens.Spacing.ExtraLarge)

    ) {

        Header()

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        CloseButton(onClick = onDismiss)

        BottomNavigationBarSpacer()
    }
}

@Composable
private fun BlockedElementsBottomSheetContent(
    blockedElements: BlockedElementsUiModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    val urlDialogContent = remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.ExtraLarge, vertical = ProtonDimens.Spacing.Tiny)
            .padding(bottom = ProtonDimens.Spacing.ExtraLarge)
            .verticalScroll(rememberScrollState())
    ) {

        Header()

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Huge))

        BlockedTrackersDetails(
            blockedElements.trackers,
            onUrlClick = { domain, url -> urlDialogContent.value = Pair(domain, url) }
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        CleanedLinksDetails(blockedElements.links)

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Huge))

        CloseButton(onClick = onDismiss)

        BottomNavigationBarSpacer()
    }

    urlDialogContent.value?.let { domainUrlPair ->
        TrackerUrlDialog(
            domain = domainUrlPair.first,
            url = domainUrlPair.second,
            onDismiss = { urlDialogContent.value = null }
        )
    }
}

@Composable
private fun Header() {

    Column {
        HeaderIcon()

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        Text(
            text = stringResource(R.string.tracking_protection),
            style = ProtonTheme.typography.headlineSmallNorm
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        HeaderDescription()
    }
}

@Composable
private fun HeaderDescription() {
    val text = stringResource(R.string.tracking_protection_description)
    val linkText = stringResource(R.string.tracking_protection_learn_more)
    val linkUrl = "https://proton.me/support/email-tracker-protection"
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
private fun HeaderIcon() {
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
            painter = painterResource(R.drawable.ic_shield_2_check_filled),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.IconSize.Large)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBlockedTrackersBottomSheet() {
    BlockedElementsBottomSheetContent(
        BlockedElementsSheetState.Requested(TrackersUiModelSample.trackersAndLinks),
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoBlockedElementsBottomSheet() {
    NoBlockedElementsBottomSheetContent(onDismiss = {})
}
