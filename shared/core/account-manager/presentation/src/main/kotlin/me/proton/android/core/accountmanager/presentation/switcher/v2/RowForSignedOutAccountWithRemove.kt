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

package me.proton.android.core.accountmanager.presentation.switcher.v2

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.component.ProtonSecondaryButton
import ch.protonmail.android.design.compose.component.ProtonTextButton
import ch.protonmail.android.design.compose.theme.LocalTypography
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountmanager.presentation.R
import me.proton.android.core.accountmanager.presentation.switcher.BaseAccountSwitcherRow
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountItem
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountListItem
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountSwitchEvent

/**
 * Displays a row with the given [accountListItem] for accounts that are signed out.
 * "Sign in" button will be displayed at the end of the row.
 * "Remove Account" button will be displayed at the bottom of the row.
 */
@Composable
fun RowForSignedOutAccountWithRemove(
    accountListItem: AccountListItem,
    modifier: Modifier = Modifier,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    BaseAccountSwitcherRow(
        modifier = modifier
            .clickable(enabled = accountListItem !is AccountListItem.Disabled) {
                onEvent(AccountSwitchEvent.OnAccountSelected(accountListItem.accountItem.userId))
            }
            .padding(ProtonDimens.Spacing.Standard),
        accountListItem = accountListItem,
        accountInitialsShape = ProtonTheme.shapes.large,
        trailingRowContent = {
            if (accountListItem is AccountListItem.Disabled) {
                ProtonSecondaryButton(
                    onClick = {
                        onEvent(AccountSwitchEvent.OnSignIn(accountListItem.accountItem.userId))
                    }
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Standard),
                        style = LocalTypography.current.bodyMediumNorm,
                        text = stringResource(R.string.account_switcher_sign_in)
                    )
                }
            }
        },
        trailingColumnContent = {
            HorizontalDivider(thickness = 1.dp, color = ProtonTheme.colors.separatorNorm)
            ProtonTextButton(
                onClick = {
                    onEvent(AccountSwitchEvent.OnRemoveAccount(accountListItem.accountItem.userId))
                }
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = ProtonDimens.Spacing.Standard)
                        .fillMaxWidth(),
                    style = LocalTypography.current.bodyMediumNorm,
                    textAlign = TextAlign.Center,
                    color = Color.Red,
                    text = stringResource(R.string.account_switcher_remove_from_device)
                )
            }
        }
    )
    HorizontalDivider(thickness = 1.dp, color = ProtonTheme.colors.separatorNorm)
}

@Preview
@Preview(name = "Night", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RowForSignedOutWithRemoveAccountPreview() {
    ProtonTheme {
        Column {
            RowForSignedOutAccountWithRemove(
                accountListItem = AccountListItem.Ready.Primary(
                    accountItem = AccountItem(
                        userId = CoreUserId("1"),
                        name = "Username",
                        email = "email@proton.me",
                        initials = "UN"
                    )
                ),
                onEvent = {}
            )

            RowForSignedOutAccount(
                accountListItem = AccountListItem.Disabled(
                    accountItem = AccountItem(
                        userId = CoreUserId("1"),
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
