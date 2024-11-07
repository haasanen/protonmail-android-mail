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

package ch.protonmail.android.mailsession.domain.usecase

import androidx.annotation.VisibleForTesting
import ch.protonmail.android.mailsession.domain.coroutines.EventLoopScope
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState.Disabled
import ch.protonmail.android.mailsession.domain.model.AccountState.Ready
import ch.protonmail.android.mailsession.domain.repository.EventLoopRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.repository.onAccountState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustEventManagerStarter @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val eventLoopRepository: EventLoopRepository,
    @EventLoopScope private val coroutineScope: CoroutineScope
) {

    @VisibleForTesting
    internal var eventLoopJobs = hashMapOf<UserId, Job>()

    fun start() {
        Timber.d("rust-event Starting event loops(s)...")
        with(userSessionRepository) {
            onAccountState(Disabled).onEach { stopEventLoop(it) }.launchIn(coroutineScope)
            onAccountState(Ready).onEach { startEventLoop(it) }.launchIn(coroutineScope)
        }
    }

    private fun stopEventLoop(account: Account) {
        eventLoopJobs[account.userId]?.cancel()
    }

    private fun startEventLoop(account: Account) {
        eventLoopJobs[account.userId]?.cancel()
        eventLoopJobs[account.userId] = coroutineScope.launch {
            while (isActive) {
                delay(EVENT_LOOP_DELAY)
                eventLoopRepository.trigger(account.userId)
            }
        }
    }

    companion object {

        internal const val EVENT_LOOP_DELAY = 30_000L
    }
}
