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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.android.core.account.domain.ObserveStoredAccounts
import me.proton.android.core.accountmanager.presentation.switcher.AccountItem
import me.proton.android.core.accountmanager.presentation.switcher.AccountListItem
import me.proton.core.util.kotlin.takeIfNotBlank
import uniffi.proton_mail_uniffi.MailSessionInterface
import uniffi.proton_mail_uniffi.StoredAccountState
import javax.inject.Inject

class ObserveAccountListItems @Inject constructor(
    private val mailSessionInterface: MailSessionInterface,
    private val observeStoredAccounts: ObserveStoredAccounts
) {

    suspend operator fun invoke(): Flow<List<AccountListItem>> = observeStoredAccounts().map { storedAccounts ->
        val primaryStoredAccount = mailSessionInterface.getPrimaryAccount()
        storedAccounts.map { storedAccount ->
            val accountName = storedAccount.displayName()?.takeIfNotBlank() ?: storedAccount.nameOrAddress()
            val initials = storedAccount.avatarInformation()?.text
            val color = storedAccount.avatarInformation()?.color
            val accountItem = AccountItem(
                userId = storedAccount.userId(),
                name = accountName,
                email = storedAccount.primaryAddr(),
                initials = initials,
                color = color
            )
            when (storedAccount.state()) {
                is StoredAccountState.LoggedIn -> if (storedAccount.userId() == primaryStoredAccount?.userId()) {
                    AccountListItem.Ready.Primary(accountItem)
                } else {
                    AccountListItem.Ready(accountItem)
                }

                else -> AccountListItem.Disabled(accountItem)
            }
        }
    }
}
