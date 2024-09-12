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
import ch.protonmail.android.mailsession.data.mapper.toLocalUserId
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.model.SessionError
import timber.log.Timber
import uniffi.proton_mail_uniffi.MailUserSession
import javax.inject.Inject

class UserSessionRepositoryImpl @Inject constructor(
    private val mailSessionRepository: MailSessionRepository
) : UserSessionRepository {

    private val activeUserSessions = mutableMapOf<UserId, MailUserSession?>()
    private val lock = Mutex()

    override suspend fun getUserSession(userId: UserId): MailUserSession? {
        lock.withLock {
            if (sessionNotInitialised(userId)) {
                initUserSession(userId)
            }

            return activeUserSessions[userId]
        }
    }

    override suspend fun forkSession(userId: UserId): Either<SessionError, ForkedSessionId> {
        Timber.d("rust-session: Forking session for $userId")
        val userSession = getUserSession(userId) ?: return SessionError.Local.Unknown.left()

        runCatching {
            userSession.fork()
        }.fold(
            onSuccess = { sessionId ->
                Timber.d("rust-session: Forked session for $userId: $sessionId")
                return ForkedSessionId(sessionId).right()
            },
            onFailure = { throwable ->
                Timber.e(throwable, "rust-session: Forking session failed")
                return SessionError.Local.Unknown.left()
            }
        )
    }

    override fun observeCurrentUserId(): Flow<UserId?> = flow {
        val mailSession = mailSessionRepository.getMailSession()
        val storedUserSession = mailSession.storedSessions().firstOrNull()

        val userId = storedUserSession?.userId()?.let { UserId(it) }
        emit(userId)
    }

    private suspend fun initUserSession(userId: UserId) {
        val mailSession = mailSessionRepository.getMailSession()
        val storedSessions = mailSession.storedSessions()
        val storedUserSession = storedSessions.find { it.userId() == userId.toLocalUserId() }

        if (storedUserSession == null) {
            Timber.e("rust-session: no stored user session found for $userId in userSessionRepository")
            activeUserSessions[userId] = null
            return
        }

        val userSession = mailSession.userContextFromSession(storedUserSession)
        activeUserSessions[userId] = userSession
    }

    private fun sessionNotInitialised(userId: UserId) = activeUserSessions.containsKey(userId).not()

}
