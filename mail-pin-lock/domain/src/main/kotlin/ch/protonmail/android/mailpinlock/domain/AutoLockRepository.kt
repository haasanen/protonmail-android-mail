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

package ch.protonmail.android.mailpinlock.domain

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.domain.model.autolock.SetAutoLockPinError
import ch.protonmail.android.mailcommon.domain.model.autolock.VerifyAutoLockPinError
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.model.Autolock
import kotlinx.coroutines.flow.Flow

interface AutoLockRepository {

    fun observeAppLock(): Flow<Autolock>
    suspend fun updateAutolockInterval(interval: AutoLockInterval): Either<DataError, Unit>
    suspend fun setAutoLockPinCode(autoLockPin: AutoLockPin): Either<SetAutoLockPinError, Unit>
    suspend fun verifyAutoLockPinCode(autoLockPin: AutoLockPin): Either<VerifyAutoLockPinError, Unit>
    suspend fun deleteAutoLockPinCode(autoLockPin: AutoLockPin): Either<VerifyAutoLockPinError, Unit>

    suspend fun setBiometricProtection(enabled: Boolean): Either<DataError, Unit>
    suspend fun shouldAutolock(): Either<DataError, Boolean>

    suspend fun getRemainingAttempts(): Either<DataError, Int>
}
