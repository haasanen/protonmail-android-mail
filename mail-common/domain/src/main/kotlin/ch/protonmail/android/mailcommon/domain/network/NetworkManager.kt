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

package ch.protonmail.android.mailcommon.domain.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Ported from core.network.data.
 *
 * Allows checking connectivity state (disconnected/metered/unmetered).
 */
abstract class NetworkManager {

    private var observers = mutableSetOf<NetworkCallback>()

    /** @return current [NetworkStatus] **/
    abstract val networkStatus: NetworkStatus

    /** @return `true` if network connectivity is available */
    fun isConnectedToNetwork() = networkStatus != NetworkStatus.Disconnected

    /**
     * @return [Flow] of [NetworkStatus] - current state and changes
     */
    fun observe() = callbackFlow {
        val observer: NetworkCallback = { trySend(it) }
        trySend(networkStatus)
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }

    /** Registers system's network state observer */
    abstract fun register()

    /** Unregisters system's network state observer */
    abstract fun unregister()

    protected fun notifyObservers(status: NetworkStatus) = observers.forEach { it(status) }

    private fun addObserver(callback: NetworkCallback) {
        if (observers.isEmpty())
            register()
        observers.add(callback)
    }

    private fun removeObserver(callback: NetworkCallback) {
        observers.remove(callback)
        if (observers.isEmpty())
            unregister()
    }
}

enum class NetworkStatus { Unmetered, Metered, Disconnected }

internal typealias NetworkCallback = (NetworkStatus) -> Unit
