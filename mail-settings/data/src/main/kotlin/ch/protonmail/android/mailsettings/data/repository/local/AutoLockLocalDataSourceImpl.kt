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

package ch.protonmail.android.mailsettings.data.repository.local

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.PreferencesError
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockEnabledEncryptedValue
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockEncryptedAttemptPendingStatus
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockEncryptedInterval
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockEncryptedLastForegroundMillis
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockEncryptedPin
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockEncryptedRemainingAttempts
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockBiometricsEncryptedValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class AutoLockLocalDataSourceImpl @Inject constructor() : AutoLockLocalDataSource {

    @Suppress("MaxLineLength")
    override suspend fun getAutoLockBiometricEncryptedValue(): Either<PreferencesError, AutoLockBiometricsEncryptedValue> =
        PreferencesError.left()

    override fun observeAutoLockBiometricEncryptedValue():
        Flow<Either<PreferencesError, AutoLockBiometricsEncryptedValue>> =
        flowOf(PreferencesError.left())

    override fun observeAutoLockEnabledEncryptedValue(): Flow<Either<PreferencesError, AutoLockEnabledEncryptedValue>> =
        flowOf(PreferencesError.left())

    override fun observeAutoLockEncryptedInterval(): Flow<Either<PreferencesError, AutoLockEncryptedInterval>> =
        flowOf(PreferencesError.left())

    override fun observeLastEncryptedForegroundMillis():
        Flow<Either<PreferencesError, AutoLockEncryptedLastForegroundMillis>> =
        flowOf(PreferencesError.left())

    override fun observeAutoLockEncryptedPin(): Flow<Either<PreferencesError, AutoLockEncryptedPin>> =
        flowOf(PreferencesError.left())

    override fun observeAutoLockEncryptedAttemptsLeft():
        Flow<Either<PreferencesError, AutoLockEncryptedRemainingAttempts>> =
        flowOf(PreferencesError.left())

    override fun observeAutoLockEncryptedPendingAttempt():
        Flow<Either<PreferencesError, AutoLockEncryptedAttemptPendingStatus>> =
        flowOf(PreferencesError.left())

    override suspend fun updateAutoLockEnabledEncryptedValue(
        value: AutoLockEnabledEncryptedValue
    ): Either<PreferencesError, Unit> = Unit.right()

    override suspend fun updateAutoLockBiometricEncryptedValue(
        value: AutoLockBiometricsEncryptedValue
    ): Either<PreferencesError, Unit> = Unit.right()

    override suspend fun updateAutoLockEncryptedInterval(
        interval: AutoLockEncryptedInterval
    ): Either<PreferencesError, Unit> = Unit.right()

    override suspend fun updateLastEncryptedForegroundMillis(
        timestamp: AutoLockEncryptedLastForegroundMillis
    ): Either<PreferencesError, Unit> = Unit.right()

    override suspend fun updateAutoLockEncryptedPin(pin: AutoLockEncryptedPin): Either<PreferencesError, Unit> =
        Unit.right()

    override suspend fun updateAutoLockAttemptsLeft(
        attempts: AutoLockEncryptedRemainingAttempts
    ): Either<PreferencesError, Unit> = Unit.right()

    override suspend fun updateAutoLockPendingAttempt(
        pendingAttempt: AutoLockEncryptedAttemptPendingStatus
    ): Either<PreferencesError, Unit> = Unit.right()
}
