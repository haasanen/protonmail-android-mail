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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailcommon.presentation.ui.MailDivider
import ch.protonmail.android.mailtrackingprotection.presentation.R
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinkValue
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinksUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.OriginalLinkValue

@Composable
internal fun CleanedLinksDetailsList(
    modifier: Modifier = Modifier,
    links: CleanedLinksUiModel,
    onLinkClick: (String, String) -> Unit
) {
    val contentSpacing = ProtonDimens.Spacing.ModeratelyLarger

    Column(modifier = modifier) {
        for (link in links.items) {
            MailDivider()

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))
            OriginalLinkRow(
                link = link.original,
                onLinkClick = onLinkClick,
                modifier = Modifier.padding(horizontal = contentSpacing)
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))
            MailDivider(modifier = Modifier.padding(start = contentSpacing))
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))
            CleanedLinkRow(
                link = link.cleaned,
                onLinkClick = onLinkClick,
                modifier = Modifier.padding(horizontal = contentSpacing)
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))
        }
    }
}

@Composable
private fun OriginalLinkRow(
    link: OriginalLinkValue,
    onLinkClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val label = stringResource(R.string.original)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = ProtonTheme.typography.bodyMediumWeak
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))
            LinkText(
                url = link.value,
                onClick = { onLinkClick(label, link.value) }
            )
        }

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))

        Icon(
            modifier = Modifier
                .size(ProtonDimens.IconSize.Default)
                .clickable { uriHandler.openUri(link.value) },
            painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
            contentDescription = stringResource(R.string.open_link),
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

@Composable
private fun CleanedLinkRow(
    link: CleanedLinkValue,
    onLinkClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val label = stringResource(R.string.cleaned)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
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
                    text = label,
                    style = ProtonTheme.typography.bodyMediumWeak
                )
            }

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

            LinkText(
                url = link.value,
                onClick = { onLinkClick(label, link.value) }
            )
        }

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))

        Icon(
            modifier = Modifier
                .size(ProtonDimens.IconSize.Default)
                .clickable { uriHandler.openUri(link.value) },
            painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
            contentDescription = stringResource(R.string.open_link),
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

@Composable
private fun LinkText(url: String, onClick: () -> Unit) {
    Text(
        modifier = Modifier.clickable(
            onClick = onClick,
            interactionSource = null,
            indication = null
        ),
        text = url,
        style = ProtonTheme.typography.bodyLargeNorm,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
