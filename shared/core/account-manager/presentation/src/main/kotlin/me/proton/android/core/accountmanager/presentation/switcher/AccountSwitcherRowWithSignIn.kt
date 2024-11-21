/*
 * Copyright (c) 2024 Proton AG
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.accountmanager.presentation.switcher

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonSecondaryButton
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.LocalTypography
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.defaultSmallNorm
import me.proton.android.core.accountmanager.presentation.R
import me.proton.core.domain.entity.UserId

/**
 * Displays a row with the given [accountListItem].
 * If the account is disabled, a "Sign in" button will be displayed at the end of the row.
 */
@Composable
fun AccountSwitcherRowWithSignIn(
    accountListItem: AccountListItem,
    modifier: Modifier = Modifier,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    BaseAccountSwitcherRow(
        modifier = modifier
            .background(LocalColors.current.backgroundNorm)
            .clickable(enabled = accountListItem !is AccountListItem.Disabled) {
                onEvent(AccountSwitchEvent.OnAccountSelected(accountListItem.accountItem.userId))
            }
            .padding(ProtonDimens.SmallSpacing),
        accountListItem = accountListItem,
        trailingContent = {
            if (accountListItem is AccountListItem.Disabled) {
                ProtonSecondaryButton(
                    onClick = {
                        onEvent(AccountSwitchEvent.OnSignIn(accountListItem.accountItem.userId))
                    }
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = ProtonDimens.SmallSpacing),
                        style = LocalTypography.current.defaultSmallNorm,
                        text = stringResource(R.string.account_switcher_sign_in)
                    )
                }
            }
        }
    )
}

@Preview
@Preview(name = "Night", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AccountSwitcherRowWithSignInPreview() {
    ProtonTheme {
        Column {
            AccountSwitcherRowWithSignIn(
                accountListItem = AccountListItem.Ready.Primary(
                    accountItem = AccountItem(
                        userId = UserId("1"),
                        name = "Username",
                        email = "email@proton.me",
                        initials = "UN"
                    )
                ),
                onEvent = {}
            )

            AccountSwitcherRowWithSignIn(
                accountListItem = AccountListItem.Disabled(
                    accountItem = AccountItem(
                        userId = UserId("1"),
                        name = "Username",
                        email = "email@proton.me",
                        initials = "UN"
                    )
                ),
                onEvent = {}
            )
        }
    }
}
