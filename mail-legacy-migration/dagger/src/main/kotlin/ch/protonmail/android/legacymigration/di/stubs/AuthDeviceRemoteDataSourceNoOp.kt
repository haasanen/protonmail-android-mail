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

package ch.protonmail.android.legacymigration.di.stubs

import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.repository.AuthDeviceRemoteDataSource
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.domain.entity.UserId

/**
 * This class is only provided for bindings required by transitive dependencies brought by the legacy Core library.
 * No usage is expected.
 */
internal object AuthDeviceRemoteDataSourceNoOp : AuthDeviceRemoteDataSource, LegacyDeprecated {

    override suspend fun createDevice(
        userId: UserId,
        name: String,
        activationToken: String?
    ) = throwUnsupported()

    override suspend fun associateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        deviceToken: String
    ) = throwUnsupported()

    override suspend fun activateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        encryptedSecret: Based64Encoded
    ) = throwUnsupported()

    override suspend fun deleteDevice(deviceId: AuthDeviceId, userId: UserId) = throwUnsupported()

    override suspend fun getAuthDevices(userId: UserId) = throwUnsupported()

    override suspend fun rejectAuthDevice(userId: UserId, deviceId: AuthDeviceId) = throwUnsupported()

    override suspend fun requestAdminHelp(userId: UserId, deviceId: AuthDeviceId) = throwUnsupported()

    override suspend fun getUnprivatizationInfo(userId: UserId) = throwUnsupported()
}
