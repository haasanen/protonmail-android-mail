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
import ch.protonmail.android.mailsession.data.mapper.toAccount
import ch.protonmail.android.mailsession.data.mapper.toLocalUserId
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.model.SessionError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import me.proton.android.core.account.domain.usecase.ObserveStoredAccounts
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionRepositoryImpl @Inject constructor(
    mailSessionRepository: MailSessionRepository,
    private val observeStoredAccounts: ObserveStoredAccounts
) : UserSessionRepository {

    private val mailSession by lazy { mailSessionRepository.getMailSession() }

    // Cache to store MailUserSession per UserId
    private val userSessionCache = mutableMapOf<UserId, MailUserSessionWrapper>()

    private suspend fun getStoredAccount(userId: UserId) = mailSession.getAccount(userId.toLocalUserId())

    override fun observeAccounts(): Flow<List<Account>> = observeStoredAccounts()
        .mapLatest { accounts -> accounts.map { it.toAccount() } }

    override fun observePrimaryUserId(): Flow<UserId?> = observeAccounts()
        .map { list ->
            val primaryUserId = mailSession.getPrimaryAccount().getOrNull()?.userId()
            list.firstOrNull { it.state == AccountState.Ready && primaryUserId == it.userId.toLocalUserId() }
        }
        .map { account -> account?.userId }
        .distinctUntilChanged()

    override fun observePrimaryAccount(): Flow<Account?> = observeAccounts()
        .map { list ->
            val primaryUserId = mailSession.getPrimaryAccount().getOrNull()?.userId()
            list.firstOrNull { it.state == AccountState.Ready && primaryUserId == it.userId.toLocalUserId() }
        }
        .distinctUntilChanged()

    override suspend fun getAccount(userId: UserId): Account? = getStoredAccount(userId).getOrNull()?.toAccount()

    override suspend fun getUserId(sessionId: SessionId): UserId? {
        val session = mailSession.getSessions().getOrNull()?.firstOrNull {
            it.sessionId() == sessionId.id
        }

        return session?.let { UserId(it.userId()) }
    }

    override suspend fun deleteAccount(userId: UserId) {
        mailSession.deleteAccount(userId.toLocalUserId())
        userSessionCache.remove(userId)?.close()
    }

    override suspend fun disableAccount(userId: UserId) {
        mailSession.logoutAccount(userId.toLocalUserId())
        userSessionCache.remove(userId)?.close()
    }

    override suspend fun getUserSession(userId: UserId): MailUserSessionWrapper? {
        // Return cached session if it exists
        userSessionCache[userId]?.let { return it }

        // Create and store session if not in cache
        val session = getStoredAccount(userId).getOrNull()?.let { account ->
            mailSession.getAccountSessions(account).getOrNull()?.firstOrNull()
        }
        val userContext = session?.let { mailSession.userContextFromSession(it) }?.getOrNull()
        if (userContext != null) {
            userSessionCache[userId] = userContext
        }
        return userContext
    }


    override suspend fun forkSession(userId: UserId): Either<SessionError, ForkedSessionId> {
        val userSession = getUserSession(userId) ?: return SessionError.Local.Unknown.left()

        return userSession.fork()
            .map { sessionId -> ForkedSessionId(sessionId) }
            .mapLeft {
                Timber.e("rust-session: Forking session failed $it")
                SessionError.Local.Unknown
            }
    }

    override suspend fun setPrimaryAccount(userId: UserId) {
        mailSession.setPrimaryAccount(userId.toLocalUserId())
    }
}

