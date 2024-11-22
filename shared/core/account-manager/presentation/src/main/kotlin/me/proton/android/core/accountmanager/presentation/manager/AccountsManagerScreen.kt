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

package me.proton.android.core.accountmanager.presentation.manager

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.LocalTypography
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.accountmanager.presentation.R
import me.proton.android.core.accountmanager.presentation.switcher.AccountItem
import me.proton.android.core.accountmanager.presentation.switcher.AccountListItem
import me.proton.android.core.accountmanager.presentation.switcher.AccountSwitchEvent
import me.proton.android.core.accountmanager.presentation.switcher.AccountSwitcherRowWithContextMenu
import me.proton.core.domain.entity.UserId

@Composable
fun AccountsManagerScreen(
    onCloseClicked: () -> Unit,
    onEvent: (AccountSwitchEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountsManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountsManagerScreen(
        state = state,
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onEvent = onEvent
    )
}

@Composable
fun AccountsManagerScreen(
    state: AccountsManagerState,
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onEvent: (AccountSwitchEvent) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = { Text(text = stringResource(R.string.manage_accounts_title)) },
                navigationIcon = {
                    IconButton(onClick = onCloseClicked) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.presentation_back),
                            tint = ProtonTheme.colors.iconNorm
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm,
                actions = {
                    IconButton(
                        onClick = { onEvent(AccountSwitchEvent.OnAddAccount) }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_proton_plus),
                            contentDescription = stringResource(R.string.manage_accounts_add_account)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (state) {
                is AccountsManagerState.Loading -> ProtonCenteredProgress()
                is AccountsManagerState.Idle -> AccountsList(state.signedInAccounts, state.disabledAccounts, onEvent)
            }
        }
    }
}

@Composable
private fun AccountsList(
    signedInAccounts: List<AccountListItem>,
    signedOutAccounts: List<AccountListItem>,
    onEvent: (AccountSwitchEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        if (signedInAccounts.isNotEmpty()) {
            item(key = "signed-in-header") {
                Text(
                    color = LocalColors.current.textWeak,
                    style = LocalTypography.current.bodyLarge,
                    text = stringResource(R.string.manage_accounts_signed_in),
                    modifier = Modifier.padding(
                        top = ProtonDimens.Spacing.ExtraLarge,
                        bottom = ProtonDimens.Spacing.Standard,
                        start = ProtonDimens.Spacing.Large,
                        end = ProtonDimens.Spacing.Large
                    )
                )
            }

            items(signedInAccounts, { "signed-in-${it.accountItem.userId}" }) { account ->
                AccountSwitcherRowWithContextMenu(
                    accountListItem = account,
                    onEvent = onEvent,
                    modifier = Modifier.clickable {
                        onEvent(AccountSwitchEvent.OnAccountSelected(account.accountItem.userId))
                    }
                )
            }
        }

        if (signedOutAccounts.isNotEmpty()) {
            item(key = "signed-out-header") {
                Text(
                    color = LocalColors.current.textWeak,
                    style = LocalTypography.current.bodyLarge,
                    text = stringResource(R.string.manage_accounts_signed_out),
                    modifier = Modifier.padding(
                        top = ProtonDimens.Spacing.ExtraLarge,
                        bottom = ProtonDimens.Spacing.Standard,
                        start = ProtonDimens.Spacing.Large,
                        end = ProtonDimens.Spacing.Large
                    )
                )
            }

            items(signedOutAccounts, { "signed-out-${it.accountItem.userId}" }) { account ->
                AccountSwitcherRowWithContextMenu(
                    accountListItem = account,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Preview
@Composable
fun ManageAccountsScreenLoadingPreview() {
    ProtonTheme {
        AccountsManagerScreen(
            state = AccountsManagerState.Loading
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ManageAccountsScreenPreview() {
    ProtonTheme {
        AccountsManagerScreen(
            state = AccountsManagerState.Idle(
                signedInAccounts = listOf(
                    AccountListItem.Ready.Primary(
                        AccountItem(UserId("user-1"), "User One", "user.one@example.test", "U1")
                    ),
                    AccountListItem.Ready(
                        AccountItem(UserId("user-2"), "User Two", "user.two@example.test", "U2")
                    )
                ),
                disabledAccounts = listOf(
                    AccountListItem.Disabled(
                        AccountItem(UserId("user-3"), "User Three", "user.three@example.test", "U3")
                    )
                )
            )
        )
    }
}
