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

package me.proton.android.core.accountmanager.presentation.switcher.v2

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountmanager.presentation.AccountDimens
import me.proton.android.core.accountmanager.presentation.switcher.BaseAccountSwitcherRow
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountItem
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountListItem
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountSwitchEvent

private const val COUNTER_MIN_VALUE = 0
private const val COUNTER_MAX_VALUE = 9

/**
 * Displays a row with the given [accountListItem].
 * "Counter" text will be displayed at the end of the row (in a format 0-9+).
 */
@Composable
fun RowForSignedInAccountWithCounter(
    accountListItem: AccountListItem,
    modifier: Modifier = Modifier,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    BaseAccountSwitcherRow(
        modifier = modifier
            .clickable(enabled = accountListItem !is AccountListItem.Disabled) {
                onEvent(AccountSwitchEvent.OnAccountSelected(accountListItem.accountItem.userId))
            }
            .padding(ProtonDimens.Spacing.Large),
        accountListItem = accountListItem,
        accountInitialsShape = ProtonTheme.shapes.large,
        trailingRowContent = {
            if (accountListItem is AccountListItem.Ready) {
                val counter = accountListItem.accountItem.counter
                if (counter != null) {
                    Box(
                        modifier = Modifier
                            .size(AccountDimens.AccountCounterSize)
                            .clip(CircleShape)
                            .background(color = ProtonTheme.colors.interactionWeakNorm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = counter.trim())
                    }
                }
            }
        }
    )
}

private fun Int.trim(): String {
    return when {
        this < COUNTER_MIN_VALUE -> "0"
        this > COUNTER_MAX_VALUE -> "9+"
        else -> this.toString()
    }
}

@Preview
@Preview(name = "Night", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RowForSignedInAccountPreview() {
    ProtonTheme {
        Column {
            RowForSignedInAccountWithCounter(
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

            RowForSignedInAccountWithCounter(
                accountListItem = AccountListItem.Ready.Primary(
                    accountItem = AccountItem(
                        userId = CoreUserId("2"),
                        name = "Username",
                        email = "email@proton.me",
                        initials = "UN",
                        counter = 7
                    )
                ),
                onEvent = {}
            )

            RowForSignedInAccountWithCounter(
                accountListItem = AccountListItem.Ready.Primary(
                    accountItem = AccountItem(
                        userId = CoreUserId("2"),
                        name = "Username",
                        email = "email@proton.me",
                        initials = "UN",
                        counter = -1
                    )
                ),
                onEvent = {}
            )

            RowForSignedInAccountWithCounter(
                accountListItem = AccountListItem.Ready.Primary(
                    accountItem = AccountItem(
                        userId = CoreUserId("2"),
                        name = "Username",
                        email = "email@proton.me",
                        initials = "UN",
                        counter = 15
                    )
                ),
                onEvent = {}
            )

            RowForSignedInAccountWithCounter(
                accountListItem = AccountListItem.Disabled(
                    accountItem = AccountItem(
                        userId = CoreUserId("1"),
                        name = "Username",
                        email = "email@proton.me",
                        initials = "UN",
                        counter = 7
                    )
                ),
                onEvent = {}
            )
        }
    }
}
