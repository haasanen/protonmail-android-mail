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

package ch.protonmail.android.mailsettings.data.local

import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.data.usecase.CreateRustUserMailSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import uniffi.proton_api_mail.MailSettings
import uniffi.proton_mail_uniffi.MailSettingsUpdated
import uniffi.proton_mail_uniffi.MailUserSettings
import javax.inject.Inject

class RustMailSettingsDataSource @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustMailSettings: CreateRustUserMailSettings,
    @AppScope private val coroutineScope: CoroutineScope
) : MailSettingsDataSource {

    private val mutableMailSettingsFlow = MutableStateFlow<MailSettings?>(null)
    private val mailSettingsFlow: Flow<MailSettings> = mutableMailSettingsFlow
        .asStateFlow()
        .filterNotNull()

    private var mailSettingsLiveQuery: MailUserSettings? = null

    override fun observeMailSettings(): Flow<MailSettings> {
        if (isMailSettingsLiveQueryNotInitialised()) {
            initMailSettingsLiveQuery()
        }

        return mailSettingsFlow
    }

    private fun initMailSettingsLiveQuery() {
        coroutineScope.launch {
            Timber.v("rust-label: initializing mail settings live query")

            val session = userSessionRepository.observeCurrentUserSession().firstOrNull()
            if (session == null) {
                Timber.e("rust-settings: trying to load settings with a null session")
                return@launch
            }

            val settingsCallback = object : MailSettingsUpdated {
                override fun onUpdated() {
                    mutableMailSettingsFlow.value = mailSettingsLiveQuery?.value()
                    Timber.v("rust-settings: mail settings updated: ${mutableMailSettingsFlow.value}")
                }
            }

            mailSettingsLiveQuery?.let { destroyMailSettingsLiveQuery() }
            mailSettingsLiveQuery = createRustMailSettings(session, settingsCallback)

            Timber.d("rust-settings: created mail settings live query")
        }
    }

    private fun isMailSettingsLiveQueryNotInitialised() = mailSettingsLiveQuery == null

    private fun destroyMailSettingsLiveQuery() {
        Timber.v("rust-settings: destroyMailSettingsLiveQuery")
        mailSettingsLiveQuery?.destroy()
        mailSettingsLiveQuery = null
    }
}
