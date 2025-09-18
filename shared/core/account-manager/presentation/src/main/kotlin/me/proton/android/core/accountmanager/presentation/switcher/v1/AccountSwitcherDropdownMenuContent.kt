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

package me.proton.android.core.accountmanager.presentation.switcher.v1

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.LocalTypography
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountmanager.presentation.R

@Composable
fun AccountSwitcherDropdownMenuContent(
    modifier: Modifier = Modifier,
    primary: AccountListItem?,
    other: List<AccountListItem>,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    Column(
        modifier
            .background(LocalColors.current.backgroundNorm)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        if (primary != null) {
            AccountSwitcherRowWithSignIn(
                accountListItem = primary,
                onEvent = onEvent
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = LocalColors.current.separatorNorm
            )
        }

        if (other.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .padding(
                        start = ProtonDimens.Spacing.Large,
                        end = ProtonDimens.Spacing.Large,
                        top = ProtonDimens.Spacing.ExtraLarge,
                        bottom = ProtonDimens.Spacing.Standard
                    ),
                style = LocalTypography.current.bodyMedium,
                color = LocalColors.current.textWeak,
                text = stringResource(R.string.account_switcher_switch_to)
            )
        }

        other.forEach { accountListItem ->
            AccountSwitcherRowWithSignIn(
                accountListItem = accountListItem,
                onEvent = onEvent
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = LocalColors.current.separatorNorm
            )
        }

        ManageAccountsButton(onClick = { onEvent(AccountSwitchEvent.OnManageAccounts) })
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ContentPreview() {
    ProtonTheme {
        Surface {
            AccountSwitcherDropdownMenuContent(
                primary = AccountListItem.Ready.Primary(
                    accountItem = AccountItem(
                        userId = CoreUserId("1"),
                        name = "John Doe",
                        email = "john.doe@proton.me",
                        initials = "J"
                    )
                ),
                other = listOf(
                    AccountListItem.Ready(
                        accountItem = AccountItem(
                            userId = CoreUserId("2"),
                            name = "Jane Doe",
                            email = "jane.doe@proton.me",
                            initials = "J"
                        )
                    ),
                    AccountListItem.Disabled(
                        accountItem = AccountItem(
                            userId = CoreUserId("3"),
                            name = "Charles Doe",
                            email = "charles.doe@proton.me",
                            initials = "C"
                        )
                    )
                ),
                onEvent = {}
            )
        }
    }
}
