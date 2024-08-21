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
import ch.protonmail.android.mailcommon.domain.mapper.LocalMailSettings
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.data.usecase.CreateRustUserMailSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.SettingsWatcher
import javax.inject.Inject

class RustMailSettingsDataSource @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustMailSettings: CreateRustUserMailSettings,
    @AppScope private val coroutineScope: CoroutineScope
) : MailSettingsDataSource {

    private val mutableMailSettingsFlow = MutableStateFlow<LocalMailSettings?>(null)
    private val mailSettingsFlow: Flow<LocalMailSettings> = mutableMailSettingsFlow
        .asStateFlow()
        .filterNotNull()

    private var mailSettingsWatcherByUserId: MailUserSettingsWatcherByUserId? = null

    override fun observeMailSettings(userId: UserId): Flow<LocalMailSettings> {
        if (isMailSettingsLiveQueryNotInitialised()) {
            initMailSettingsLiveQuery(userId)
        }

        return mailSettingsFlow
    }

    private fun initMailSettingsLiveQuery(userId: UserId) {
        coroutineScope.launch {
            Timber.v("rust-settings: initializing mail settings live query")

            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-settings: trying to load settings with a null session")
                return@launch
            }

            val settingsCallback = object : LiveQueryCallback {
                override fun onUpdate() {
                    mutableMailSettingsFlow.value = mailSettingsWatcherByUserId?.watcher?.settings
                    Timber.v("rust-settings: mail settings updated: ${mutableMailSettingsFlow.value}")
                }
            }

            val settingsWatcher = createRustMailSettings(session, settingsCallback)
            mailSettingsWatcherByUserId?.let { destroyMailSettingsLiveQuery() }
            mailSettingsWatcherByUserId = MailUserSettingsWatcherByUserId(userId, settingsWatcher)


            Timber.v("rust-settings: Setting initial value for mail settings ${settingsWatcher.settings}")
            mutableMailSettingsFlow.value = settingsWatcher.settings
            Timber.d("rust-settings: created mail settings live query")
        }
    }

    private fun isMailSettingsLiveQueryNotInitialised() = mailSettingsWatcherByUserId == null

    private fun destroyMailSettingsLiveQuery() {
        Timber.v("rust-settings: destroyMailSettingsLiveQuery")
        mailSettingsWatcherByUserId?.watcher?.destroy()
        mailSettingsWatcherByUserId = null
    }

    private data class MailUserSettingsWatcherByUserId(
        val userId: UserId,
        val watcher: SettingsWatcher
    )
}
