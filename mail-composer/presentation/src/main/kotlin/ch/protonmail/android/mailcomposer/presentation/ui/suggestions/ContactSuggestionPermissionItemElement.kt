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

package ch.protonmail.android.mailcomposer.presentation.ui.suggestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcomposer.presentation.R

@Composable
internal fun DeviceContactsEntry(onClick: () -> Unit, onDenyClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(color = ProtonTheme.colors.backgroundInvertedSecondary)
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.Large)
            .padding(vertical = ProtonDimens.Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactSuggestionPermissionAvatar()

        Spacer(Modifier.width(ProtonDimens.Spacing.Large))

        Text(
            text = stringResource(R.string.composer_recipient_suggestion_device),
            maxLines = 1,
            style = ProtonTheme.typography.bodyLargeNorm,
            color = ProtonTheme.colors.textNorm,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            modifier = Modifier.clickable(onClick = onDenyClick),
            painter = painterResource(id = R.drawable.ic_proton_close_filled),
            contentDescription = null,
            tint = ProtonTheme.colors.iconHint
        )
    }
}

@Composable
private fun ContactSuggestionPermissionAvatar() {
    Box(
        modifier = Modifier
            .sizeIn(
                minWidth = MailDimens.AvatarSize,
                minHeight = MailDimens.AvatarSize
            )
            .background(
                color = ProtonTheme.colors.shade45,
                shape = ProtonTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_contacts),
            tint = Color.White,
            contentDescription = null,
            modifier = Modifier.size(MailDimens.AvatarIconSize)
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewPermissionItem() {
    ProtonTheme {
        DeviceContactsEntry(
            onClick = {},
            onDenyClick = {}
        )
    }
}
