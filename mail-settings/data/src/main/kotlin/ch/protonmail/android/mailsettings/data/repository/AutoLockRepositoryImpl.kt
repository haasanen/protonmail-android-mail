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

package ch.protonmail.android.mailsettings.data.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockAttemptPendingStatus
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockBiometricsPreference
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockInterval
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockLastForegroundMillis
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockPreference
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockRemainingAttempts
import ch.protonmail.android.mailsettings.domain.repository.AutoLockPreferenceError
import ch.protonmail.android.mailsettings.domain.repository.AutoLockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class AutoLockRepositoryImpl @Inject constructor() : AutoLockRepository {

    override suspend fun getCurrentAutoLockBiometricsPreference():
        Either<AutoLockPreferenceError, AutoLockBiometricsPreference> =
        AutoLockPreferenceError.DataStoreError.left()

    override fun observeAutoLockBiometricsPreference(): AutoLockPreferenceEitherFlow<AutoLockBiometricsPreference> =
        flowOf(AutoLockPreferenceError.DataStoreError.left())

    override fun observeAutoLockEnabledValue(): AutoLockPreferenceEitherFlow<AutoLockPreference> =
        flowOf(AutoLockPreferenceError.DataStoreError.left())

    override fun observeAutoLockInterval(): AutoLockPreferenceEitherFlow<AutoLockInterval> =
        flowOf(AutoLockPreferenceError.DataStoreError.left())

    override fun observeAutoLockLastForegroundMillis(): AutoLockPreferenceEitherFlow<AutoLockLastForegroundMillis> =
        flowOf(AutoLockPreferenceError.DataStoreError.left())

    override fun observeAutoLockPin(): AutoLockPreferenceEitherFlow<AutoLockPin> =
        flowOf(AutoLockPreferenceError.DataStoreError.left())

    override fun observeAutoLockRemainingAttempts(): AutoLockPreferenceEitherFlow<AutoLockRemainingAttempts> =
        flowOf(AutoLockPreferenceError.DataStoreError.left())

    override fun observeAutoLockAttemptPendingStatus(): AutoLockPreferenceEitherFlow<AutoLockAttemptPendingStatus> =
        flowOf(AutoLockPreferenceError.DataStoreError.left())

    override suspend fun updateAutoLockBiometricsPreference(
        value: AutoLockBiometricsPreference
    ): Either<AutoLockPreferenceError, Unit> = Unit.right()

    override suspend fun updateAutoLockEnabledValue(value: AutoLockPreference): Either<AutoLockPreferenceError, Unit> =
        Unit.right()

    override suspend fun updateAutoLockInterval(interval: AutoLockInterval): Either<AutoLockPreferenceError, Unit> =
        Unit.right()

    override suspend fun updateAutoLockPin(pin: AutoLockPin): Either<AutoLockPreferenceError, Unit> = Unit.right()

    override suspend fun updateLastForegroundMillis(
        timestamp: AutoLockLastForegroundMillis
    ): Either<AutoLockPreferenceError, Unit> = Unit.right()

    override suspend fun updateAutoLockRemainingAttempts(
        attempts: AutoLockRemainingAttempts
    ): Either<AutoLockPreferenceError, Unit> = Unit.right()

    override suspend fun updateAutoLockAttemptPendingStatus(
        pendingAttempt: AutoLockAttemptPendingStatus
    ): Either<AutoLockPreferenceError, Unit> = Unit.right()
}

private typealias AutoLockPreferenceEitherFlow<T> = Flow<AutoLockPreferenceEither<T>>
private typealias AutoLockPreferenceEither<T> = Either<AutoLockPreferenceError, T>
