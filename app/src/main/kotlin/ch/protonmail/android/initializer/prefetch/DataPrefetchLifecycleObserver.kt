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

package ch.protonmail.android.initializer.prefetch

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailmailbox.domain.usecase.PrefetchDataForUser
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal class DataPrefetchLifecycleObserver @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val prefetchDataForUser: PrefetchDataForUser,
    @AppScope private val coroutineScope: CoroutineScope
) : DefaultLifecycleObserver {

    private var job: Job? = null

    override fun onResume(owner: LifecycleOwner) {
        job?.cancel()
        job = coroutineScope.launch {
            userSessionRepository.observePrimaryAccount().filterNotNull().collectLatest {
                if (it.state == AccountState.Ready) {
                    val userSession = userSessionRepository.getUserSession(it.userId) ?: return@collectLatest
                    prefetchDataForUser(userSession)
                } else {
                    Timber
                        .tag("DataPrefetchLifecycleObserver")
                        .w("Unable to trigger prefetch for account with state ${it.state}")
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        job?.cancel()
        job = null
    }
}
