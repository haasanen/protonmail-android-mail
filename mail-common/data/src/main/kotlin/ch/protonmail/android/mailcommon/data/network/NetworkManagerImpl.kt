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

package ch.protonmail.android.mailcommon.data.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import ch.protonmail.android.mailcommon.domain.network.NetworkManager
import ch.protonmail.android.mailcommon.domain.network.NetworkStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkManager() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var registered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            notifyObservers(networkStatus)
        }

        override fun onLost(network: Network) {
            notifyObservers(networkStatus)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            notifyObservers(networkStatus)
        }
    }


    override val networkStatus: NetworkStatus
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        get() = with(connectivityManager) {
            val activeNetwork = activeNetwork
            val networkCapabilities = getNetworkCapabilities(activeNetwork)

            when {
                networkCapabilities == null -> NetworkStatus.Disconnected
                !networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) -> NetworkStatus.Disconnected

                !networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                ) -> NetworkStatus.Disconnected

                !isActiveNetworkMetered -> NetworkStatus.Unmetered
                else -> NetworkStatus.Metered
            }
        }

    override val activeNetwork: Network?
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        get() = connectivityManager.activeNetwork

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun register() {
        if (!registered) {
            registered = true
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
    }

    override fun unregister() {
        if (registered) {
            registered = false
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
