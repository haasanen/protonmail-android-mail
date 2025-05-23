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
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsettings.data.local.RustAppSettingsDataSource
import ch.protonmail.android.mailsettings.data.mapper.toAppSettings
import ch.protonmail.android.mailsettings.data.mapper.toLocalAppDiff
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.domain.repository.AppLanguageRepository
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class AppSettingsRepository @Inject constructor(
    private val mailSessionRepository: MailSessionRepository,
    private val rustAppSettingsDataSource: RustAppSettingsDataSource,
    private val appLanguageRepository: AppLanguageRepository
) : AppSettingsRepository {

    private val restartTrigger = MutableSharedFlow<Unit>(replay = 1)

    override fun observeAppSettings(): Flow<AppSettings> = restartTrigger.flatMapLatest {
        combine(
            getLatestRustAppSettings(),
            appLanguageRepository.observe()
        ) { appSettings, customLanguage ->
            appSettings.fold(
                {
                    AppSettings.default().apply {
                        Timber.e("Unable to get app settings $it, returning default settings: $this")
                    }
                },
                {
                    it.toAppSettings(customLanguage)
                }
            )
        }
    }.apply { restartTrigger.tryEmit(Unit) }

    override fun observeTheme(): Flow<Theme> = observeAppSettings().map { it.theme }


    private fun getLatestRustAppSettings() = flow {
        emit(rustAppSettingsDataSource.getAppSettings(mailSessionRepository.getMailSession().getRustMailSession()))
    }

    override suspend fun updateTheme(theme: Theme): Either<DataError, Unit> =
        rustAppSettingsDataSource.updateAppSettings(
            mailSessionRepository.getMailSession().getRustMailSession(),
            theme.toLocalAppDiff()
        ).onLeft { error ->
            Timber.e("Was not able to update theme $error")
        }.onRight {
            restartTrigger.emit(Unit)
        }
}
