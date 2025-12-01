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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonSettingsTopBar
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.LocalTypography
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountmanager.presentation.AccountDimens
import me.proton.android.core.accountmanager.presentation.R
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountItem
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountListItem
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountSwitchEvent
import me.proton.android.core.accountmanager.presentation.switcher.v2.RowForSignedInAccount
import me.proton.android.core.accountmanager.presentation.switcher.v2.RowForSignedOutAccountWithRemove

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsManagerScreen(
    state: AccountsManagerState,
    modifier: Modifier = Modifier,
    cardModifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onEvent: (AccountSwitchEvent) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(
            left = ProtonDimens.Spacing.Large,
            right = ProtonDimens.Spacing.Large
        ),
        topBar = {
            ProtonSettingsTopBar(
                title = stringResource(id = R.string.manage_accounts_title),
                onBackClick = dropUnlessResumed { onCloseClicked() },
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
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                )
        ) {
            when (state) {
                is AccountsManagerState.Loading -> ProtonCenteredProgress()
                is AccountsManagerState.Idle -> AccountsList(
                    modifier = modifier,
                    cardModifier = cardModifier,
                    signedInAccounts = state.signedInAccounts,
                    signedOutAccounts = state.disabledAccounts,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun AccountsList(
    signedInAccounts: List<AccountListItem>,
    signedOutAccounts: List<AccountListItem>,
    onEvent: (AccountSwitchEvent) -> Unit,
    modifier: Modifier = Modifier,
    cardModifier: Modifier = Modifier
) {

    val contentDesSignedIn = stringResource(R.string.manage_accounts_switch_to_signed_in_content_desc)
    Column(modifier = modifier) {
        if (signedInAccounts.isNotEmpty()) {
            Text(
                color = LocalColors.current.textWeak,
                style = LocalTypography.current.bodyLarge,
                text = stringResource(R.string.manage_accounts_switch_to),
                modifier = Modifier
                    .semantics {
                        contentDescription = contentDesSignedIn
                        heading()
                    }
                    .padding(
                        bottom = ProtonDimens.Spacing.Medium,
                        top = ProtonDimens.Spacing.Standard
                    )
            )
            signedInAccounts.forEachIndexed { index, account ->
                Card(
                    shape = RoundedCornerShape(AccountDimens.AccountCardRadius),
                    modifier = cardModifier
                        .padding(bottom = ProtonDimens.Spacing.Medium)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = ProtonTheme.colors.backgroundInvertedSecondary
                    )
                ) {
                    RowForSignedInAccount(
                        accountListItem = account,
                        onEvent = onEvent,
                        modifier = Modifier.clickable {
                            onEvent(AccountSwitchEvent.OnAccountSelected(account.accountItem.userId))
                        }
                    )
                }
            }
        }

        if (signedOutAccounts.isNotEmpty()) {
            val contentDesSignedOut = stringResource(R.string.manage_accounts_switch_to_signed_out_content_desc)
            Text(
                color = LocalColors.current.textWeak,
                style = LocalTypography.current.bodyLarge,
                text = stringResource(R.string.manage_accounts_switch_to),
                modifier = Modifier
                    .semantics {
                        contentDescription = contentDesSignedOut
                        heading()
                    }
                    .padding(
                        bottom = ProtonDimens.Spacing.Medium,
                        top = ProtonDimens.Spacing.Standard
                    )
            )
            signedOutAccounts.forEachIndexed { index, account ->
                Card(
                    shape = RoundedCornerShape(AccountDimens.AccountCardRadius),
                    modifier = cardModifier
                        .padding(bottom = ProtonDimens.Spacing.Medium)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = ProtonTheme.colors.backgroundInvertedSecondary
                    )
                ) {
                    RowForSignedOutAccountWithRemove(
                        accountListItem = account,
                        onEvent = onEvent
                    )
                }
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
                        AccountItem(CoreUserId("user-1"), "User One", "user.one@example.test", "U1")
                    ),
                    AccountListItem.Ready(
                        AccountItem(CoreUserId("user-2"), "User Two", "user.two@example.test", "U2")
                    )
                ),
                disabledAccounts = listOf(
                    AccountListItem.Disabled(
                        AccountItem(CoreUserId("user-3"), "User Three", "user.three@example.test", "U3")
                    )
                )
            )
        )
    }
}
