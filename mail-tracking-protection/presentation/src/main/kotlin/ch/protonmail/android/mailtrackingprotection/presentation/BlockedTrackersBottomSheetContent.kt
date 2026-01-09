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

package ch.protonmail.android.mailtrackingprotection.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.bodySmallWeak
import ch.protonmail.android.design.compose.theme.headlineSmallNorm
import ch.protonmail.android.design.compose.theme.labelLargeNorm
import ch.protonmail.android.mailtrackingprotection.domain.model.CleanedLink
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedElementsUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedTrackersSheetState
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinksUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.TrackersUiModel
import ch.protonmail.android.uicomponents.BottomNavigationBarSpacer

@Composable
fun BlockedElementsBottomSheetContent(
    state: BlockedTrackersSheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    when (state) {
        is BlockedTrackersSheetState.Requested -> when {
            state.elements != null -> BlockedTrackersBottomSheetContent(state.elements, onDismiss, modifier)
            else -> NoBlockedTrackersBottomSheetContent(modifier, onDismiss)
        }
    }
}

@Composable
private fun NoBlockedTrackersBottomSheetContent(modifier: Modifier = Modifier, onDismiss: () -> Unit) {

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
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors().copy(containerColor = ProtonTheme.colors.interactionBrandDefaultNorm),
        shape = ProtonTheme.shapes.huge,
        contentPadding = PaddingValues(ProtonDimens.Spacing.Large)
    ) {
        Text(
            text = stringResource(R.string.got_it),
            style = ProtonTheme.typography.labelLargeNorm.copy(color = ProtonTheme.colors.iconInverted)
        )
    }
}

@Composable
private fun BlockedTrackersBottomSheetContent(
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
private fun TrackerUrlDialog(
    domain: String,
    url: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = { },
        dismissButton = {
            ProtonAlertDialogButton(R.string.action_close_dialog_button) {
                onDismiss()
            }
        },
        title = domain,
        text = { SelectionContainer { ProtonAlertDialogText(url) } }
    )
}

@Composable
private fun CleanedLinksDetails(links: CleanedLinksUiModel, modifier: Modifier = Modifier) {

    val isExpanded = remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.large)
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.large
            )
            .padding(ProtonDimens.Spacing.ModeratelyLarger)
    ) {

        CleanedLinksDetailsHeader(
            links,
            isExpanded.value,
            onClick = { isExpanded.value = !isExpanded.value }
        )

        AnimatedContent(isExpanded.value) {
            if (it) {
                CleanedLinksDetailsList(links)
            }
        }

    }
}

@Composable
private fun CleanedLinksDetailsList(links: CleanedLinksUiModel) {

    Column {

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        for (link in links.items) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Spacer(modifier = Modifier.weight(1f))

            }

            OriginalLinkRow(link)
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))
            CleanedLinkRow(link)
        }

    }
}

@Composable
private fun CleanedLinkRow(link: CleanedLink) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_proton_arrow_down_and_right),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconWeak
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

                Text(
                    text = stringResource(R.string.cleaned),
                    style = ProtonTheme.typography.bodyMediumWeak
                )
            }

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

            ClickableLink(url = link.cleaned)
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )
    }
}


@Composable
private fun OriginalLinkRow(link: CleanedLink) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.original),
                style = ProtonTheme.typography.bodyMediumWeak
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))
            ClickableLink(url = link.original)

        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            modifier = Modifier.size(ProtonDimens.IconSize.Default),
            painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

@Composable
private fun ClickableLink(url: String) {
    val annotatedString = remember(url) {
        buildAnnotatedString {
            withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(
                        style = SpanStyle()
                    )
                )
            ) {
                append(url)
            }
        }
    }

    Text(
        text = annotatedString,
        style = ProtonTheme.typography.bodyLargeNorm,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun CleanedLinksDetailsHeader(
    links: CleanedLinksUiModel,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(
                enabled = links.isExpandable,
                onClick = onClick,
                interactionSource = null,
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.number_of_cleaned_links,
                links.items.size,
                links.items.size
            ),
            style = ProtonTheme.typography.bodyLargeNorm
        )

        Spacer(modifier = Modifier.weight(1f))

        if (links.isExpandable) {
            val chevronIcon = when {
                isExpanded -> R.drawable.ic_proton_chevron_down
                else -> R.drawable.ic_proton_chevron_up
            }
            Icon(
                painter = painterResource(id = chevronIcon),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
        }
    }
}

@Composable
private fun BlockedTrackersDetails(
    trackers: TrackersUiModel,
    onUrlClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    val isExpanded = remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.large)
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.large
            )
            .padding(ProtonDimens.Spacing.ModeratelyLarger)
    ) {

        BlockedTrackersDetailsHeader(
            trackers,
            isExpanded.value,
            onClick = { isExpanded.value = !isExpanded.value }
        )

        AnimatedContent(isExpanded.value) {
            if (it) {
                BlockedTrackersDetailsList(trackers, onUrlClick)
            }
        }
    }
}

@Composable
private fun BlockedTrackersDetailsList(trackers: TrackersUiModel, onUrlClick: (String, String) -> Unit) {

    Column {

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        for (tracker in trackers.items) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tracker.domain,
                    style = ProtonTheme.typography.bodyLargeNorm
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${tracker.urls.size}",
                    style = ProtonTheme.typography.bodyLargeNorm
                )
            }

            for (url in tracker.urls) {
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))

                Text(
                    modifier = Modifier
                        .clickable(
                            onClick = { onUrlClick(tracker.domain, url) },
                            interactionSource = null,
                            indication = null
                        ),
                    text = url,
                    style = ProtonTheme.typography.bodySmallWeak,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
        }
    }
}

@Composable
private fun BlockedTrackersDetailsHeader(
    trackers: TrackersUiModel,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(
                enabled = trackers.isExpandable,
                onClick = onClick,
                interactionSource = null,
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.number_of_blocked_trackers,
                trackers.items.size,
                trackers.items.size
            ),
            style = ProtonTheme.typography.bodyLargeNorm
        )

        Spacer(modifier = Modifier.weight(1f))

        if (trackers.isExpandable) {
            val chevronIcon = when {
                isExpanded -> R.drawable.ic_proton_chevron_down
                else -> R.drawable.ic_proton_chevron_up
            }
            Icon(
                painter = painterResource(id = chevronIcon),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
        }
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
        BlockedTrackersSheetState.Requested(TrackersUiModelSample.trackersAndLinks),
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoBlockedTrackersBottomSheet() {
    NoBlockedTrackersBottomSheetContent(onDismiss = {})
}
