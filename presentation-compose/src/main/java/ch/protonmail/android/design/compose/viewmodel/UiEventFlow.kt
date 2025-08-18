/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.design.compose.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

class UiEventFlow<T> : AbstractFlow<T>() {

    private val events = Channel<T>(capacity = BUFFER_CAPACITY, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        events.receiveAsFlow().collect {
            collector.emit(it)
        }
    }

    suspend fun emit(event: T) {
        withContext(Dispatchers.Main.immediate) {
            events.send(event)
        }
    }

    private companion object {

        private const val BUFFER_CAPACITY = 16
    }
}
