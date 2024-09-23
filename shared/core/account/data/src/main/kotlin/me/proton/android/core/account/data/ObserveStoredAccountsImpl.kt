/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.android.core.account.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.core.account.data.qualifier.QueryWatcherCoroutineScope
import me.proton.android.core.account.domain.ObserveAllSessions
import me.proton.android.core.account.domain.ObserveStoredAccounts
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailSessionInterface
import uniffi.proton_mail_uniffi.StoredAccount
import javax.inject.Inject

class ObserveStoredAccountsImpl @Inject constructor(
    @QueryWatcherCoroutineScope private val coroutineScope: CoroutineScope,
    private val mailSession: MailSessionInterface,
    private val observeAllSessions: ObserveAllSessions
) : ObserveStoredAccounts {

    /**
     * Note:
     * Instead of just invoking [MailSessionInterface.watchAccounts],
     * we also call [observeAllSessions],
     * so that the [StoredAccount.state] is always up to date.
     * This is needed because currently, a session state is not part of the "Accounts" SQL table.
     */
    private val storedAccountsWithSessionStateFlow = observeAllSessions()
        .flatMapLatest { storedAccountsFlow() }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), null)

    override fun invoke(): Flow<List<StoredAccount>> = storedAccountsWithSessionStateFlow.filterNotNull()

    private fun storedAccountsFlow(): Flow<List<StoredAccount>> = callbackFlow {
        val watchedStoredAccounts = mailSession.watchAccounts(
            object : LiveQueryCallback {
                override fun onUpdate() {
                    launch { send(mailSession.getAccounts()) }
                }
            }
        )

        send(watchedStoredAccounts.accounts)

        awaitClose {
            watchedStoredAccounts.handle.disconnect()
            watchedStoredAccounts.destroy()
        }
    }
}
