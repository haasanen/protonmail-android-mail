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

package ch.protonmail.android.mailsession.data.initializer

import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uniffi.proton_mail_uniffi.LoginFlow
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionInitializationCallback
import uniffi.proton_mail_uniffi.MailUserSessionInitializationStage
import uniffi.proton_mail_uniffi.UserLoginFlowArcLoginFlowResult
import uniffi.proton_mail_uniffi.UserLoginFlowArcMailUserSessionResult
import uniffi.proton_mail_uniffi.UserLoginFlowVoidResult
import javax.inject.Inject

class InjectFakeRustSession @Inject constructor(
    private val mailSessionRepository: MailSessionRepository
) {

    @SuppressWarnings("MagicNumber")
    fun withUser(username: String, password: String) = runBlocking {
        Timber.v("rust-session: Begin injection of fake rust session with for $username...")
        val mailSession = mailSessionRepository.getMailSession()
        val storedSessionExists = mailSession.storedSessions().isNotEmpty()
        if (storedSessionExists) {
            Timber.d("rust-session: Existing session found in rust lib. Fake login skipped")
            return@runBlocking
        }

        val loginFlow: LoginFlow = buildLoginInterface(mailSession) ?: return@runBlocking
        val mailUserSession = loginFlow.performLogin(username, password) ?: return@runBlocking

        Timber.d("rust-session: Fake login with $username performed")
        Timber.v("rust-session: Initializing user context for $username...")

        var initCompleted = false
        mailUserSession.initialize(
            object : MailUserSessionInitializationCallback {
                override fun onStage(stage: MailUserSessionInitializationStage) {
                    Timber.v("rust-session: rust-session onStage: $stage")
                    if (stage == MailUserSessionInitializationStage.FINISHED) {
                        initCompleted = true
                    }
                }
            }
        )
        while (!initCompleted) {
            delay(1000)
        }
        Timber.d("rust-session: rust-session initialization completed successfully")
    }

    private suspend fun LoginFlow.performLogin(username: String, password: String): MailUserSession? {
        Timber.v("rust-session: Performing login for $username ...")
        when (val result = this.login(username, password)) {
            is UserLoginFlowVoidResult.Error -> {
                Timber.v("rust-session: Fake login Failure! reason: $result")
                return null
            }
            is UserLoginFlowVoidResult.Ok -> {
                Timber.v("rust-session: Fake login Success..")
                return this.extractUserContext()
            }
        }
    }

    private fun LoginFlow.extractUserContext(): MailUserSession? {
        return when (val result = this.toUserContext()) {
            is UserLoginFlowArcMailUserSessionResult.Error -> {
                Timber.v("rust-session: Login Flow: Failure creating user session! reason: $result")
                null
            }

            is UserLoginFlowArcMailUserSessionResult.Ok -> {
                Timber.v("rust-session: Login Flow: user session creation success..")
                result.v1
            }
        }
    }

    private suspend fun buildLoginInterface(mailSession: MailSession): LoginFlow? {
        Timber.v("rust-session: building login interface...")
        return when (val result = mailSession.newLoginFlow()) {
            is UserLoginFlowArcLoginFlowResult.Error -> {
                Timber.v("rust-session: Login Flow: Failure creating interface due to $result")
                null
            }

            is UserLoginFlowArcLoginFlowResult.Ok -> {
                Timber.v("rust-session: Login Flow: interface created...")
                result.v1
            }
        }
    }

}
