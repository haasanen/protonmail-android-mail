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

package ch.protonmail.android.mailpinlock.data

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.domain.model.autolock.SetAutoLockPinError
import ch.protonmail.android.mailcommon.domain.model.autolock.VerifyAutoLockPinError
import ch.protonmail.android.mailpinlock.data.mapper.toAutoLock
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.domain.BiometricsSystemStateRepository
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailsession.data.mapper.toLocalAutoLockPin
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AutoLockRepositoryImpl @Inject constructor(
    private val biometricsSystemStateRepository: BiometricsSystemStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val mailSessionRepository: MailSessionRepository,
    private val appLockDataSource: AppLockDataSource
) : AutoLockRepository {

    override fun observeAppLock(): Flow<AutoLock> = combine(
        biometricsSystemStateRepository.observe(),
        appSettingsRepository.observeAppSettings()
    ) { biometrics, appSettings ->
        appSettings.toAutoLock(biometrics)
    }

    override suspend fun updateAutoLockInterval(interval: AutoLockInterval): Either<DataError, Unit> =
        appSettingsRepository.updateInterval(interval = interval)

    // note this exists in UserSessionRepositoryImpl for dealing with legacy pin codes, maybe it should all be moved
    // to once place
    override suspend fun setAutoLockPinCode(autoLockPin: AutoLockPin): Either<SetAutoLockPinError, Unit> {
        return autoLockPin.toLocalAutoLockPin().mapLeft { _ ->
            SetAutoLockPinError.Other(DataError.Local.TypeConversionError)
        }.flatMap { localPin ->
            mailSessionRepository.getMailSession().setAutoLockPinCode(localPin).onRight {
                appSettingsRepository.refreshSettings()
            }
        }
    }

    override suspend fun verifyAutoLockPinCode(autoLockPin: AutoLockPin): Either<VerifyAutoLockPinError, Unit> {
        return autoLockPin.toLocalAutoLockPin().mapLeft { _ ->
            VerifyAutoLockPinError.Other(DataError.Local.TypeConversionError)
        }.flatMap { localPin ->
            mailSessionRepository.getMailSession().verifyPinCode(localPin).onRight {
                appSettingsRepository.refreshSettings()
            }
        }
    }

    override suspend fun deleteAutoLockPinCode(autoLockPin: AutoLockPin): Either<VerifyAutoLockPinError, Unit> {
        return autoLockPin.toLocalAutoLockPin().mapLeft { _ ->
            VerifyAutoLockPinError.Other(DataError.Local.TypeConversionError)
        }.flatMap { localPin ->
            mailSessionRepository.getMailSession().deleteAutoLockPinCode(localPin).onRight {
                appSettingsRepository.refreshSettings()
            }
        }
    }

    override suspend fun setBiometricProtection(enabled: Boolean): Either<DataError, Unit> {
        return if (enabled) {
            mailSessionRepository.getMailSession().setBiometricAppProtection()
        } else {
            mailSessionRepository.getMailSession().unsetBiometricAppProtection()
        }.onRight {
            appSettingsRepository.refreshSettings()
        }
    }

    override fun signalBiometricsCheckPassed() = mailSessionRepository.getMailSession().signalBiometricsCheckPassed()

    override suspend fun shouldAutoLock(): Either<DataError, Boolean> =
        appLockDataSource.shouldAutoLock(mailSessionRepository.getMailSession().getRustMailSession())

    override suspend fun getRemainingAttempts(): Either<DataError, Int> {
        return mailSessionRepository.getMailSession().getRemainingAttempts().flatMap {
            it?.toInt()?.right() ?: DataError.Local.Unknown.left()
        }
    }
}
