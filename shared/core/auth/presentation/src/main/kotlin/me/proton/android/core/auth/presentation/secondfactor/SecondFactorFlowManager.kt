/*
 * Copyright (c) 2025 Proton Technologies AG
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

package me.proton.android.core.auth.presentation.secondfactor

import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowCache.SecondFactorFlow
import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_account_uniffi.PasswordFlow
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionResumeLoginFlowResult
import uniffi.proton_mail_uniffi.MailSessionToUserSessionResult
import uniffi.proton_mail_uniffi.MailSessionUserSessionFromStoredSessionResult
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionNewPasswordChangeFlowResult
import uniffi.proton_mail_uniffi.StoredSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecondFactorFlowManager @Inject constructor(
    private val sessionInterface: MailSession,
    private val flowCache: SecondFactorFlowCache
) {

    suspend fun getSecondFactorFlow(userId: String): SecondFactorFlow? {
        flowCache.clearIfUserChanged(userId)
        return flowCache.getActiveFlow() ?: tryResumeLoginFlow(userId)
    }

    private suspend fun tryResumeLoginFlow(userId: String): SecondFactorFlow? {
        val currentFlow = flowCache.getActiveFlow()
        val loginFlow = (currentFlow as? SecondFactorFlow.LoggingIn)?.flow ?: run {
            val session = getActiveSession(userId) ?: return null
            when (val result = sessionInterface.resumeLoginFlow(userId, session.sessionId())) {
                is MailSessionResumeLoginFlowResult.Error -> null
                is MailSessionResumeLoginFlowResult.Ok -> result.v1
            }
        } ?: return null

        return SecondFactorFlow.LoggingIn(loginFlow).also {
            flowCache.setActiveFlow(it, shouldClearOnNext = true)
        }
    }

    suspend fun tryCreatePasswordFlow(userId: String): SecondFactorFlow? {
        val currentFlow = flowCache.getActiveFlow()
        val passwordFlow = (currentFlow as? SecondFactorFlow.ChangingPassword)?.flow ?: run {
            val session = getActiveSession(userId) ?: return null
            val userSession = getUserSession(session)
            createPasswordChangeFlow(userSession)
        }

        return SecondFactorFlow.ChangingPassword(passwordFlow).also {
            flowCache.setActiveFlow(it, shouldClearOnNext = false)
        }
    }

    suspend fun convertToUserContext(loginFlow: LoginFlow): MailSessionToUserSessionResult =
        sessionInterface.toUserSession(loginFlow)

    suspend fun clearCache(force: Boolean = false): Boolean = flowCache.clear(force)

    private suspend fun getActiveSession(userId: String): StoredSession? {
        return flowCache.getCachedSession() ?: run {
            val account = sessionInterface.getAccountById(userId)
            sessionInterface.getSessionsForAccount(account)
                ?.firstOrNull()
                ?.also { session ->
                    flowCache.setCachedSession(session, userId)
                }
        }
    }

    private suspend fun getUserSession(session: StoredSession): MailUserSession {
        return when (val result = sessionInterface.userSessionFromStoredSession(session)) {
            is MailSessionUserSessionFromStoredSessionResult.Error ->
                throw SessionException("Failed to get user context")
            is MailSessionUserSessionFromStoredSessionResult.Ok -> result.v1
        }
    }

    private suspend fun createPasswordChangeFlow(userSession: MailUserSession): PasswordFlow {
        return when (val result = userSession.newPasswordChangeFlow()) {
            is MailUserSessionNewPasswordChangeFlowResult.Error ->
                throw SessionException("Failed to create password change flow")

            is MailUserSessionNewPasswordChangeFlowResult.Ok -> result.v1
        }
    }

    class SessionException(message: String) : Exception(message)
}
