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
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.domain.model.autolock.SetAutoLockPinError
import ch.protonmail.android.mailpinlock.data.mapper.toAutolock
import ch.protonmail.android.mailpinlock.domain.AutolockRepository
import ch.protonmail.android.mailpinlock.domain.BiometricsSystemStateRepository
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.model.Autolock
import ch.protonmail.android.mailsession.data.mapper.toLocalAutoLockPin
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsettings.domain.model.AppSettingsDiff
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AutolockRepository @Inject constructor(
    private val biometricsSystemStateRepository: BiometricsSystemStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val mailSessionRepository: MailSessionRepository,
    private val appLockDataSource: AppLockDataSource
) : AutolockRepository {

    override fun observeAppLock(): Flow<Autolock> = combine(
        biometricsSystemStateRepository.observe(),
        appSettingsRepository.observeAppSettings()
    ) { biometrics, appSettings ->
        appSettings.toAutolock(biometrics)
    }

    override suspend fun updateAutolockInterval(interval: AutoLockInterval): Either<DataError, Unit> =
        appSettingsRepository.updateAppSettings(AppSettingsDiff(interval = interval))

    // note this exists in UserSessionRepositoryImpl for dealing with legacy pin codes, maybe it should all be moved
    // to once place
    override suspend fun setAutoLockPinCode(autoLockPin: AutoLockPin): Either<SetAutoLockPinError, Unit> {
        return autoLockPin.toLocalAutoLockPin().mapLeft { _ ->
            SetAutoLockPinError.Other(DataError.Local.TypeConversionError)
        }.flatMap { localPin ->
            mailSessionRepository.getMailSession().setAutoLockPinCode(localPin)
        }
    }

    override suspend fun shouldAutolock(): Either<DataError, Boolean> =
        appLockDataSource.shouldAutoLock(mailSessionRepository.getMailSession().getRustMailSession())
}
