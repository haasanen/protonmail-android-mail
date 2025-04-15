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

package ch.protonmail.android.mailnotifications.domain

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailnotifications.data.FirebaseNotificationsTokenChannel
import ch.protonmail.android.mailnotifications.data.remote.FirebaseMessagingProxy
import ch.protonmail.android.mailnotifications.data.repository.DeviceRegistrationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FirebaseMessagingTokenLifecycleObserver @Inject constructor(
    private val firebaseMessagingProxy: FirebaseMessagingProxy,
    private val firebaseNotificationsTokenChannel: FirebaseNotificationsTokenChannel,
    private val deviceRegistrationRepository: DeviceRegistrationRepository,
    @AppScope private val coroutineScope: CoroutineScope
) : DefaultLifecycleObserver {

    private var tokenFlowJob: Job? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        tokenFlowJob = coroutineScope.launch {
            firebaseNotificationsTokenChannel.tokenFlow.distinctUntilChanged().collect {
                Timber.tag("Register device token").d("Received new token, registering...")
                deviceRegistrationRepository.registerDeviceToken(it)
            }
        }

        coroutineScope.launch {
            val token = firebaseMessagingProxy.fetchToken().getOrElse {
                Timber.tag("Register device token").d("Unable to fetch token")
                return@launch
            }

            firebaseNotificationsTokenChannel.sendToken(token)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        tokenFlowJob?.cancel()
    }
}
