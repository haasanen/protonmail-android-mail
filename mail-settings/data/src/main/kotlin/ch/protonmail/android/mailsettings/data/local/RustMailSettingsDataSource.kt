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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalMailSettings
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.data.usecase.CreateRustUserMailSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.SettingsWatcher
import javax.inject.Inject

class RustMailSettingsDataSource @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustMailSettings: CreateRustUserMailSettings
) : MailSettingsDataSource {

    override fun observeMailSettings(userId: UserId): Flow<LocalMailSettings> = callbackFlow {
        Timber.v("rust-settings: initializing mail settings live query")

        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-settings: trying to load settings with a null session")
            close()
            return@callbackFlow
        }

        var watcher: SettingsWatcher? = null
        val settingsCallback = object : LiveQueryCallback {
            override fun onUpdate() {
                watcher?.settings?.let {
                    trySend(it)
                    Timber.v("rust-settings: mail settings updated: $it")
                }
            }
        }

        val watcherResult = createRustMailSettings(session, settingsCallback)
            .onLeft {
                close()
                Timber.e("rust-settings: failed creating settings watcher $it")
            }
            .onRight { settingsWatcher ->
                watcher = settingsWatcher
                Timber.v("rust-settings: Setting initial value for mail settings ${settingsWatcher.settings}")
                send(settingsWatcher.settings)
                Timber.d("rust-settings: mail settings watcher created")
            }

        awaitClose {
            Timber.v("rust-settings: mail settings watcher disconnected: ${watcherResult.getOrNull()}")
            watcherResult.getOrNull()?.watchHandle?.disconnect()
            watcherResult.getOrNull()?.destroy()
        }
    }
}
