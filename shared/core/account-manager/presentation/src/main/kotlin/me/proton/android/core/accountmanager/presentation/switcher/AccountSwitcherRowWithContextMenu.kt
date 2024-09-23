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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.LocalTypography
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.accountmanager.presentation.R
import me.proton.android.core.accountmanager.presentation.switcher.MenuOption.ManageAccount
import me.proton.android.core.accountmanager.presentation.switcher.MenuOption.Remove
import me.proton.android.core.accountmanager.presentation.switcher.MenuOption.SignIn
import me.proton.android.core.accountmanager.presentation.switcher.MenuOption.SignOut

/**
 * Displays a row with the given [accountListItem].
 * At the end of the row, displays a button which will show
 * a drop-down menu with actions such as "Sign out".
 */
@Composable
fun AccountSwitcherRowWithContextMenu(
    accountListItem: AccountListItem,
    modifier: Modifier = Modifier,
    onEvent: (AccountSwitchEvent) -> Unit = {}
) {
    BaseAccountSwitcherRow(
        modifier = modifier
            .background(LocalColors.current.backgroundNorm)
            .padding(ProtonDimens.SmallSpacing),
        accountListItem = accountListItem,
        trailingContent = {
            AccountSwitcherRowContextMenu(
                accountListItem = accountListItem,
                modifier = modifier,
                onEvent = onEvent
            )
        }
    )
}

@Composable
private fun AccountSwitcherRowContextMenu(
    accountListItem: AccountListItem,
    modifier: Modifier = Modifier,
    onEvent: (AccountSwitchEvent) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                contentDescription = null,
                painter = painterResource(R.drawable.ic_proton_three_dots_vertical),
                tint = LocalColors.current.textWeak
            )
        }
        DropdownMenu(
            modifier = Modifier.background(LocalColors.current.backgroundDeep),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val list = when (accountListItem) {
                is AccountListItem.Disabled -> listOf(SignIn, Remove)
                is AccountListItem.Ready.Primary -> listOf(ManageAccount, SignOut, Remove)
                is AccountListItem.Ready -> listOf(SignOut, Remove)
            }
            list.forEach {
                when (it) {
                    ManageAccount ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    style = LocalTypography.current.body1Regular,
                                    text = stringResource(R.string.account_switcher_manage_account)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    contentDescription = null,
                                    painter = painterResource(R.drawable.ic_proton_cog_wheel)
                                )
                            },
                            onClick = {
                                expanded = false
                                onEvent(AccountSwitchEvent.OnManageAccount(accountListItem.accountItem.userId))
                            }
                        )

                    SignOut ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    style = LocalTypography.current.body1Regular,
                                    text = stringResource(R.string.account_switcher_sign_out)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    contentDescription = null,
                                    painter = painterResource(R.drawable.ic_proton_arrow_out_from_rectangle)
                                )
                            },
                            onClick = {
                                expanded = false
                                onEvent(AccountSwitchEvent.OnSignOut(accountListItem.accountItem.userId))
                            }
                        )

                    SignIn ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    style = LocalTypography.current.body1Regular,
                                    text = stringResource(R.string.account_switcher_sign_in)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    contentDescription = null,
                                    painter = painterResource(R.drawable.ic_proton_arrow_in_to_rectangle)
                                )
                            },
                            onClick = {
                                expanded = false
                                onEvent(AccountSwitchEvent.OnSignIn(accountListItem.accountItem.userId))
                            }
                        )

                    Remove ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    style = LocalTypography.current.body1Regular,
                                    text = stringResource(R.string.account_switcher_remove)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    contentDescription = null,
                                    painter = painterResource(R.drawable.ic_remove_account)
                                )
                            },
                            onClick = {
                                expanded = false
                                onEvent(AccountSwitchEvent.OnRemoveAccount(accountListItem.accountItem.userId))
                            }
                        )
                }
            }
        }
    }
}

private enum class MenuOption {
    ManageAccount,
    SignOut,
    SignIn,
    Remove
}

@Preview
@Preview(name = "Night", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AccountSwitcherRowWithContextMenuPreview() {
    ProtonTheme(colors = ProtonTheme.colors.sidebarColors!!) {
        AccountSwitcherRowWithContextMenu(
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
