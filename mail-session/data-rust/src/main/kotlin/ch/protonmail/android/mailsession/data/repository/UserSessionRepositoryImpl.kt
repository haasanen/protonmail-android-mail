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

package ch.protonmail.android.mailsession.data.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailsession.data.RepositoryFlowCoroutineScope
import ch.protonmail.android.mailsession.data.mapper.toAccount
import ch.protonmail.android.mailsession.data.mapper.toLocalUserId
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.model.SessionError
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.WatchedAccounts
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionRepositoryImpl @Inject constructor(
    mailSessionRepository: MailSessionRepository,
    @RepositoryFlowCoroutineScope coroutineScope: CoroutineScope
) : UserSessionRepository {

    private val mailSession by lazy { mailSessionRepository.getMailSession() }

    private val storedAccountsStateFlow: Flow<List<StoredAccount>?> = callbackFlow {
        var watchedStoredAccounts: WatchedAccounts? = null
        watchedStoredAccounts = mailSession.watchAccounts(
            object : LiveQueryCallback {
                override fun onUpdate() {
                    launch { send(mailSession.getAccounts()) }
                }
            }
        )

        send(watchedStoredAccounts.accounts)

        awaitClose {
            watchedStoredAccounts.handle.disconnect()
            watchedStoredAccounts.handle.destroy()
            watchedStoredAccounts.destroy()
        }
    }.stateIn(coroutineScope, SharingStarted.Lazily, initialValue = null)

    private suspend fun getStoredAccount(userId: UserId) = mailSession.getAccount(userId.toLocalUserId())

    override fun observeAccounts(): Flow<List<Account>> = storedAccountsStateFlow
        // Skip null initialValue.
        .filterNotNull()
        .mapLatest { accounts -> accounts.map { it.toAccount() } }

    override fun observePrimaryUserId(): Flow<UserId?> = observeAccounts()
        .map { list ->
            val primaryUserId = mailSession.getPrimaryAccount()?.userId()
            list.firstOrNull { it.state == AccountState.Ready && primaryUserId == it.userId.toLocalUserId() }
        }
        .map { account -> account?.userId }
        .distinctUntilChanged()

    override suspend fun getAccount(userId: UserId): Account? = getStoredAccount(userId)?.toAccount()

    override suspend fun deleteAccount(userId: UserId) {
        mailSession.deleteAccount(userId.toLocalUserId())
    }

    override suspend fun disableAccount(userId: UserId) {
        mailSession.logoutAccount(userId.toLocalUserId())
    }

    override suspend fun getUserSession(userId: UserId): MailUserSession? {
        val session = getStoredAccount(userId)?.let { mailSession.getAccountSessions(it).firstOrNull() }
        return session?.let { mailSession.userContextFromSession(it) }
    }

    override suspend fun forkSession(userId: UserId): Either<SessionError, ForkedSessionId> {
        val userSession = getUserSession(userId) ?: return SessionError.Local.Unknown.left()

        runCatching {
            userSession.fork()
        }.fold(
            onSuccess = { sessionId ->
                return ForkedSessionId(sessionId).right()
            },
            onFailure = { throwable ->
                Timber.e(throwable, "rust-session: Forking session failed")
                return SessionError.Local.Unknown.left()
            }
        )
    }
}
