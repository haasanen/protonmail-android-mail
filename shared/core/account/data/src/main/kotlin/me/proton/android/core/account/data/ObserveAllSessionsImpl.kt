/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.android.core.account.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.core.account.data.qualifier.QueryWatcherCoroutineScope
import me.proton.android.core.account.domain.ObserveAllSessions
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailSessionInterface
import uniffi.proton_mail_uniffi.StoredSession
import javax.inject.Inject

class ObserveAllSessionsImpl @Inject constructor(
    @QueryWatcherCoroutineScope private val coroutineScope: CoroutineScope,
    private val mailSession: MailSessionInterface
) : ObserveAllSessions {

    private val storedSessionsFlow = callbackFlow {
        val watchedSessions = mailSession.watchSessions(
            object : LiveQueryCallback {
                override fun onUpdate() {
                    launch { send(mailSession.getSessions()) }
                }
            }
        )

        send(watchedSessions.sessions)

        awaitClose {
            watchedSessions.handle.disconnect()
            watchedSessions.destroy()
        }
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(), null)

    override fun invoke(): Flow<List<StoredSession>> = storedSessionsFlow.filterNotNull()
}
