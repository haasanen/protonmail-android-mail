/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailconversation.data.local

import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationScrollerRegistry @Inject constructor() {

    private val mutex = Mutex()
    private val activePaginators: MutableSet<ConversationPaginatorWrapper> = mutableSetOf()

    suspend fun register(paginator: ConversationPaginatorWrapper) {
        mutex.withLock {
            activePaginators.add(paginator)
            Timber.d(
                "conversation-scroller-registry: Scroller registered, activeCount=%d",
                activePaginators.size
            )
        }
    }

    suspend fun disconnectAll() {
        mutex.withLock {
            if (activePaginators.isNotEmpty()) {
                activePaginators.forEach { wrapper ->
                    wrapper.disconnect()
                }
                val clearedCount = activePaginators.size
                activePaginators.clear()
                Timber.d("conversation-scroller-registry: Disconnected %d scroller(s)", clearedCount)
            }
        }
    }

    suspend fun unregister(scroller: ConversationPaginatorWrapper) {
        mutex.withLock {
            val removed = activePaginators.remove(scroller)
            if (removed) Timber.d("conversation-scroller-registry: unregistered scroller")
        }
    }
}
