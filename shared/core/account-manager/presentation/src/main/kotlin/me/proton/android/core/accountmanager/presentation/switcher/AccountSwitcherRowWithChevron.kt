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

package me.proton.android.core.accountmanager.presentation.switcher

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.core.presentation.R

/**
 * Displays a row with the given [accountListItem].
 */
@Composable
fun AccountSwitcherRowWithChevron(accountListItem: AccountListItem, modifier: Modifier = Modifier) {
    BaseAccountSwitcherRow(
        modifier = modifier.background(LocalColors.current.interactionWeakNorm),
        accountListItem = accountListItem,
        trailingContent = {
            Icon(
                contentDescription = null,
                painter = painterResource(R.drawable.ic_proton_chevron_down),
                tint = LocalColors.current.textWeak,
                modifier = Modifier.padding(end = ProtonDimens.SmallSpacing)
            )
        }
    )
}

@Preview
@Preview(name = "Night", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AccountSwitcherRowWithChevronPreview() {
    ProtonTheme(colors = ProtonTheme.colors.sidebarColors!!) {
        AccountSwitcherRowWithChevron(
            accountListItem = AccountListItem.Ready.Primary(
                accountItem = AccountItem(
                    userId = "1",
                    name = "Username",
                    email = "email@proton.me",
                    initials = "UN"
                )
            )
        )
    }
}
