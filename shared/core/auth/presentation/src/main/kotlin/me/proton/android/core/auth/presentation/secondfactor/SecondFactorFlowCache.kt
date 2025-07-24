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

import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_account_uniffi.PasswordFlow
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.StoredSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecondFactorFlowCache @Inject constructor(
    private val sessionInterface: MailSession
) {

    sealed class SecondFactorFlow {
        data class LoggingIn(val flow: LoginFlow) : SecondFactorFlow()
        data class ChangingPassword(val flow: PasswordFlow) : SecondFactorFlow()
    }

    private var cachedFlow: SecondFactorFlow? = null
    private var cachedSession: StoredSession? = null
    private var cachedUserId: String? = null
    private var shouldClearOnNext: Boolean = true

    fun getActiveFlow(): SecondFactorFlow? = cachedFlow

    fun setActiveFlow(flow: SecondFactorFlow, shouldClearOnNext: Boolean) {
        this.cachedFlow = flow
        this.shouldClearOnNext = shouldClearOnNext
    }

    fun setCachedSession(session: StoredSession, userId: String) {
        cachedSession = session
        cachedUserId = userId
    }

    fun getCachedSession(): StoredSession? = cachedSession

    suspend fun clearIfUserChanged(userId: String) {
        if (cachedUserId != null && cachedUserId != userId) {
            clear(force = true)
        }
    }

    suspend fun clear(force: Boolean = false): Boolean {
        if (!shouldClearOnNext && !force) {
            return false
        }

        if (cachedFlow is SecondFactorFlow.LoggingIn) {
            cachedUserId?.let { sessionInterface.deleteAccount(it) }
        }

        cachedFlow = null
        cachedSession = null
        cachedUserId = null
        shouldClearOnNext = true

        return true
    }
}
