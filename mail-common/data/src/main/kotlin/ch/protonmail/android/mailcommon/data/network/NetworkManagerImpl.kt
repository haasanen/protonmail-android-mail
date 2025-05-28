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

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
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
        get() = with(connectivityManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
            } else {
                // Fallback for API < 23
                @Suppress("DEPRECATION")
                when {
                    activeNetworkInfo?.isConnected != true -> NetworkStatus.Disconnected
                    isActiveNetworkMetered -> NetworkStatus.Metered
                    else -> NetworkStatus.Unmetered
                }
            }
        }

    override fun register() {
        if (!registered) {
            registered = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            } else {
                // Fallback for API < 24
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(request, networkCallback)
            }
        }
    }

    override fun unregister() {
        if (registered) {
            registered = false
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
