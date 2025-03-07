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

package ch.protonmail.android.mailnotifications.data.remote

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.protonmail.android.mailnotifications.data.remote.fcm.model.KEY_PM_REGISTRATION_WORKER_ERROR
import ch.protonmail.android.mailnotifications.data.usecase.RegisterAndSaveDeviceToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.takeIfNotBlank

@HiltWorker
class RegisterDeviceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val registerAndSaveDeviceToken: RegisterAndSaveDeviceToken
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(RawUserIdKey)?.takeIfNotBlank()
            ?.let { UserId(it) }
            ?: return Result.failure(workDataOf(KEY_PM_REGISTRATION_WORKER_ERROR to "User id not provided"))

        val token = inputData.getString(TokenKey)?.takeIfNotBlank()
            ?: return Result.failure(workDataOf(KEY_PM_REGISTRATION_WORKER_ERROR to "Notifications token not found"))

        return registerAndSaveDeviceToken(userId, token)
    }

    companion object {

        const val RawUserIdKey = "pmRegistrationWorkerUserId"
        const val TokenKey = "pmRegistrationWorkerToken"

        fun params(userId: UserId, token: String) = mapOf(
            TokenKey to token,
            RawUserIdKey to userId.id
        )

        fun id(userId: UserId) = "RegisterDeviceWorker-${userId.id}"
    }
}
