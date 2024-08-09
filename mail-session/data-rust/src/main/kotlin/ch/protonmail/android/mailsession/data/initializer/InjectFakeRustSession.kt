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
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailUserSessionInitializationCallback
import uniffi.proton_mail_uniffi.MailUserSessionInitializationStage
import uniffi.proton_mail_uniffi.SessionCallback
import uniffi.proton_mail_uniffi.SessionError
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

        val newLoginFlow = buildLoginFlow(mailSession)
        newLoginFlow.login(username, password)
        Timber.v("rust-session: Fake login with $username performed")

        Timber.v("rust-session: Initializing user context for $username...")

        var initCompleted = false
        newLoginFlow.toUserContext().initialize(
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

    private fun buildLoginFlow(mailSession: MailSession) = mailSession.newLoginFlow(
        object : SessionCallback {
            override fun onError(err: SessionError) {
                Timber.w("rust-session: fake login flow failed.")
            }

            override fun onRefreshFailed(e: SessionError) {
                Timber.w("rust-session: fake login flow failed.")
            }

            override fun onSessionDeleted() {
                Timber.d("rust-session: fake login session deleted.")
            }

            override fun onSessionRefresh() {
                Timber.d("rust-session: fake login session refreshed.")
            }

        }
    )

}
