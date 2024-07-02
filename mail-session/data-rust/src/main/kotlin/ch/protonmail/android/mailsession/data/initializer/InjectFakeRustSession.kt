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

import ch.protonmail.android.mailsession.data.RepositoryFlowCoroutineScope
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import uniffi.proton_mail_uniffi.SessionCallback
import uniffi.proton_mail_uniffi.SessionError
import javax.inject.Inject

class InjectFakeRustSession @Inject constructor(
    private val mailSessionRepository: MailSessionRepository,
    @RepositoryFlowCoroutineScope private val coroutineScope: CoroutineScope
) {

    fun withUser(username: String, password: String) = coroutineScope.launch {
        val mailSession = mailSessionRepository.getMailSession()
        val newLoginFlow = mailSession.newLoginFlow(
            object : SessionCallback {
                override fun onError(err: SessionError) {
                    Timber.w("RustLib: fake login flow failed.")
                }

                override fun onRefreshFailed(e: SessionError) {
                    Timber.w("RustLib: fake login flow failed.")
                }

                override fun onSessionDeleted() {
                    Timber.w("RustLib: fake login session deleted.")
                }

                override fun onSessionRefresh() {
                }

            }
        )
        newLoginFlow.login(username, password)
        Timber.d("RustLib: Fake login with $username performed")
    }

}
