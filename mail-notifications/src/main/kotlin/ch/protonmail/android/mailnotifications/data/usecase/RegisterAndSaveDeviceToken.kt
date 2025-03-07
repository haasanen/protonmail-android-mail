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

package ch.protonmail.android.mailnotifications.data.usecase

import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import ch.protonmail.android.mailnotifications.data.remote.fcm.model.KEY_PM_REGISTRATION_WORKER_ERROR
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import uniffi.proton_mail_uniffi.DeviceEnvironment
import uniffi.proton_mail_uniffi.RegisteredDevice
import javax.inject.Inject

class RegisterAndSaveDeviceToken @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val registerAndSaveDevice: RegisterAndSaveDevice
) {

    suspend operator fun invoke(userId: UserId, deviceToken: String): Result {
        val userSession = userSessionRepository
            .getUserSession(userId)
            ?.getRustUserSession()
            ?: return Result.failure(workDataOf(KEY_PM_REGISTRATION_WORKER_ERROR to "Unable to find user session"))

        val device = RegisteredDevice(
            deviceToken = deviceToken,
            environment = DeviceEnvironment.GOOGLE,
            pingNotificationStatus = null,
            pushNotificationStatus = null
        )

        return registerAndSaveDevice(userSession, device).fold(
            ifRight = { Result.success() },
            ifLeft = { Result.failure(workDataOf(KEY_PM_REGISTRATION_WORKER_ERROR to "Unable to register device")) }
        )
    }
}
