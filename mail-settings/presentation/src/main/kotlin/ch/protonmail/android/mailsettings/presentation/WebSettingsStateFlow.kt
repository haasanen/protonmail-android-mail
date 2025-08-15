/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.presentation

import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.usecase.ForkSession
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.domain.model.WebSettingsConfig
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import ch.protonmail.android.mailsettings.domain.usecase.ObserveWebSettingsConfig
import ch.protonmail.android.mailsettings.presentation.websettings.WebSettingsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

class ObserveWebSettingsStateFlow @Inject constructor(
    val observePrimaryUserId: ObservePrimaryUserId,
    val forkSession: ForkSession,
    val appSettingsRepository: AppSettingsRepository,
    val observeWebSettingsConfig: ObserveWebSettingsConfig
) {

    operator fun invoke(
        coroutineScope: CoroutineScope,
        getSettingsUrl: (ForkedSessionId, Theme, WebSettingsConfig) -> String
    ): Flow<WebSettingsState> = combine(
        observePrimaryUserId().filterNotNull(),
        appSettingsRepository.observeTheme(),
        observeWebSettingsConfig()
    ) { userId, theme, webSettingsConfig ->

        forkSession(userId).fold(
            ifRight = { forkedSessionId ->
                Timber.e("web-email-settings: Forking session success $forkedSessionId")
                WebSettingsState.Data(getSettingsUrl(forkedSessionId, theme, webSettingsConfig), theme)
            },
            ifLeft = { sessionError ->
                Timber.e("web-email-settings: Forking session failed")
                WebSettingsState.Error("Forking session failed due to $sessionError")
            }
        )
    }
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            WebSettingsState.Loading
        )
}
