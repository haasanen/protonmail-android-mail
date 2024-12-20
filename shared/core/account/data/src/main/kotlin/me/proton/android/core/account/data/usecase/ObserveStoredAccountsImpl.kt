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

package me.proton.android.core.account.data.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import me.proton.android.core.account.data.qualifier.QueryWatcherCoroutineScope
import me.proton.android.core.account.domain.usecase.ObserveCoreSessions
import me.proton.android.core.account.domain.usecase.ObserveStoredAccounts
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetAccountsResult
import uniffi.proton_mail_uniffi.MailSessionWatchAccountsResult
import uniffi.proton_mail_uniffi.StoredAccount
import javax.inject.Inject

class ObserveStoredAccountsImpl @Inject constructor(
    @QueryWatcherCoroutineScope private val coroutineScope: CoroutineScope,
    private val mailSession: MailSession,
    private val observeCoreSessions: ObserveCoreSessions
) : ObserveStoredAccounts {

    /**
     * Note:
     * Instead of just invoking [MailSession.watchAccounts],
     * we also call [observeCoreSessions],
     * so that the [StoredAccount.state] is always up to date.
     * This is needed because currently, a session state is not part of the "Accounts" SQL table.
     */
    private val storedAccountsWithSessionStateFlow = observeCoreSessions()
        .flatMapLatest { accountsFlow() }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)

    override fun invoke(): Flow<List<StoredAccount>> = storedAccountsWithSessionStateFlow

    private fun accountsFlow(): Flow<List<StoredAccount>> = callbackFlow {
        val watchedStoredAccounts = mailSession.watchAccounts(
            object : LiveQueryCallback {
                override fun onUpdate() {
                    launch {
                        when (val accountsResult = mailSession.getAccounts()) {
                            is MailSessionGetAccountsResult.Error -> send(emptyList())
                            is MailSessionGetAccountsResult.Ok -> send(accountsResult.v1)
                        }

                    }
                }
            }
        )

        when (watchedStoredAccounts) {
            is MailSessionWatchAccountsResult.Error -> {
                send(emptyList())
                close()
            }
            is MailSessionWatchAccountsResult.Ok -> {
                send(watchedStoredAccounts.v1.accounts)

                awaitClose {
                    watchedStoredAccounts.v1.handle.disconnect()
                    watchedStoredAccounts.destroy()
                }
            }
        }
    }
}
