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

package ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailtrackingprotection.domain.model.CleanedLink
import ch.protonmail.android.mailtrackingprotection.presentation.R
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinksUiModel

@Composable
internal fun CleanedLinksDetails(links: CleanedLinksUiModel, modifier: Modifier = Modifier) {

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
