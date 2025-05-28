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

@file:Suppress("DEPRECATION")

package ch.protonmail.android.mailcommon.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import ch.protonmail.android.mailcommon.domain.network.NetworkStatus
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NetworkManagerImplTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var connectivityManager: ConnectivityManager

    @RelaxedMockK
    private lateinit var network: Network

    @RelaxedMockK
    private lateinit var networkCapabilities: NetworkCapabilities

    @RelaxedMockK
    private lateinit var networkInfo: NetworkInfo

    private lateinit var networkManager: NetworkManagerImpl

    private val networkCallback = slot<ConnectivityManager.NetworkCallback>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        networkManager = NetworkManagerImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `networkStatus returns Disconnected when activeNetwork is null`() {
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.getNetworkCapabilities(null) } returns null

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Disconnected, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `networkStatus returns Disconnected when networkCapabilities is null`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Disconnected, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `networkStatus returns Disconnected when no internet capability`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Disconnected, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `networkStatus returns Disconnected when not validated`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Disconnected, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `networkStatus returns Unmetered when connected and not metered`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { connectivityManager.isActiveNetworkMetered } returns false

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Unmetered, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `networkStatus returns Metered when connected and metered`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { connectivityManager.isActiveNetworkMetered } returns true

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Metered, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `networkStatus fallback returns Disconnected when not connected`() {
        every { connectivityManager.activeNetworkInfo } returns networkInfo
        every { networkInfo.isConnected } returns false

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Disconnected, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `networkStatus fallback returns Disconnected when activeNetworkInfo is null`() {
        every { connectivityManager.activeNetworkInfo } returns null

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Disconnected, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `networkStatus fallback returns Metered when connected and metered`() {
        every { connectivityManager.activeNetworkInfo } returns networkInfo
        every { networkInfo.isConnected } returns true
        every { connectivityManager.isActiveNetworkMetered } returns true

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Metered, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `networkStatus fallback returns Unmetered when connected and not metered`() {
        every { connectivityManager.activeNetworkInfo } returns networkInfo
        every { networkInfo.isConnected } returns true
        every { connectivityManager.isActiveNetworkMetered } returns false

        val result = networkManager.networkStatus

        assertEquals(NetworkStatus.Unmetered, result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `register calls registerDefaultNetworkCallback on API 24+`() {
        every { connectivityManager.registerDefaultNetworkCallback(capture(networkCallback)) } just Runs

        networkManager.register()

        verify { connectivityManager.registerDefaultNetworkCallback(any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `register only registers once when called multiple times`() {
        every { connectivityManager.registerDefaultNetworkCallback(any()) } just Runs

        networkManager.register()
        networkManager.register()

        verify(exactly = 1) { connectivityManager.registerDefaultNetworkCallback(any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `register calls registerNetworkCallback with request on API 23`() {
        val requestSlot = slot<NetworkRequest>()
        every { connectivityManager.registerNetworkCallback(capture(requestSlot), capture(networkCallback)) } just Runs

        networkManager.register()

        verify {
            connectivityManager.registerNetworkCallback(
                any<NetworkRequest>(),
                any<ConnectivityManager.NetworkCallback>()
            )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `unregister calls unregisterNetworkCallback when registered`() {
        every { connectivityManager.registerDefaultNetworkCallback(capture(networkCallback)) } just Runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just Runs

        networkManager.register()
        networkManager.unregister()

        verify { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `unregister does nothing when not registered`() {
        networkManager.unregister()

        verify(exactly = 0) {
            connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `unregister only unregisters once when called multiple times`() {
        every { connectivityManager.registerDefaultNetworkCallback(capture(networkCallback)) } just Runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just Runs

        networkManager.register()
        networkManager.unregister()
        networkManager.unregister()

        verify(exactly = 1) {
            connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
        }
    }
}
