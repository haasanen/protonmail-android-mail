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

import ch.protonmail.android.mailsession.data.RepositoryFlowCoroutineScope
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.SessionCallback
import uniffi.proton_mail_uniffi.SessionError
import javax.inject.Inject

class UserSessionRepositoryImpl @Inject constructor(
    private val mailSessionRepository: MailSessionRepository,
    @RepositoryFlowCoroutineScope private val coroutineScope: CoroutineScope
) : UserSessionRepository {

    private val mutableUserSessionFlow = MutableStateFlow<MailUserSession?>(null)

    override fun observeCurrentUserSession(): Flow<MailUserSession?> = mutableUserSessionFlow
        .asStateFlow()
        .onStart { initUserSessionFlow() }

    override fun observeCurrentUserId(): Flow<UserId?> = flow {
        val mailSession = mailSessionRepository.getMailSession()
        val storedUserSession = mailSession.storedSessions().firstOrNull()

        val userId = storedUserSession?.userId()?.let { UserId(it) }
        emit(userId)
    }

    private suspend fun initUserSessionFlow() {
        val mailSession = mailSessionRepository.getMailSession()
        val storedUserSession = mailSession.storedSessions().firstOrNull()

        if (storedUserSession == null) {
            Timber.e("rust-session: no stored user session found from userSessionRepository")
            mutableUserSessionFlow.emit(null)
            return
        }

        val userSession = mailSession.userContextFromSession(
            storedUserSession,
            object : SessionCallback {
                override fun onError(err: SessionError) {
                    Timber.e("rust-session: error: ${err.name}")
                    coroutineScope.launch { mutableUserSessionFlow.emit(null) }
                }

                override fun onRefreshFailed(e: SessionError) {
                    Timber.w("rust-session: refresh failed: ${e.name}")
                    coroutineScope.launch { mutableUserSessionFlow.emit(null) }
                }

                override fun onSessionDeleted() {
                    coroutineScope.launch { mutableUserSessionFlow.emit(null) }
                }

                override fun onSessionRefresh() {

                }

            }
        )
        mutableUserSessionFlow.emit(userSession)
    }

}
