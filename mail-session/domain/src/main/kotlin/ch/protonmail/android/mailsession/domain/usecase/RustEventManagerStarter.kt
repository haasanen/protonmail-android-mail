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

import ch.protonmail.android.mailsession.domain.coroutines.EventLoopScope
import ch.protonmail.android.mailsession.domain.repository.EventLoopRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import timber.log.Timber
import javax.inject.Inject

class RustEventManagerStarter @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val eventLoopRepository: EventLoopRepository,
    @EventLoopScope private val coroutineScope: CoroutineScope
) {

    private var eventLoopJob: Job? = null

    fun start() {
        Timber.d("rust-event Starting event loop...")

        eventLoopJob = userSessionRepository.observeCurrentUserId().filterNotNull().onEach { userId ->

            while (coroutineScope.isActive) {

                eventLoopRepository.trigger(userId)
                delay(EVENT_LOOP_DELAY)
            }

        }.launchIn(coroutineScope)
    }

    companion object {
        private const val EVENT_LOOP_DELAY = 30_000L
    }
}
