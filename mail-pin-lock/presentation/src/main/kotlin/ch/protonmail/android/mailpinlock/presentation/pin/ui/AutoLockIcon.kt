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

package ch.protonmail.android.mailpinlock.presentation.pin.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.standalone.LocalLockScreenEntryPointIsStandalone

@Composable
internal fun AutoLockIcon() {
    if (LocalLockScreenEntryPointIsStandalone.current) {
        AutoLockMailIcon()
    } else {
        AutoLockLockIcon()
    }
}

@Composable
private fun AutoLockLockIcon() {
    val isDarkMode = isSystemInDarkTheme()

    val imageVectorId = if (isDarkMode) {
        R.drawable.pin_lock_header_dark
    } else {
        R.drawable.pin_lock_header
    }

    Icon(
        imageVector = ImageVector.vectorResource(id = imageVectorId),
        contentDescription = null,
        tint = Color.Unspecified
    )
}

@Composable
private fun AutoLockMailIcon() {
    Box(
        modifier = Modifier
            .padding(ProtonDimens.Spacing.Small),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(width = MailDimens.AutolockIconWidth, height = MailDimens.AutolockIconHeight),
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo_mail_no_bg),
            tint = Color.Unspecified,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun AutoLockIconMailPreview() {
    ProtonTheme {
        AutoLockMailIcon()
    }
}

@Preview
@Composable
private fun AutoLockLockIconPreview() {
    ProtonTheme {
        AutoLockLockIcon()
    }
}
