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

package ch.protonmail.android.mailsettings.presentation.webaccountsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.domain.model.WebSettingsConfig
import ch.protonmail.android.mailsettings.domain.repository.ThemeRepository
import ch.protonmail.android.mailsettings.domain.usecase.ObserveWebSettingsConfig
import ch.protonmail.android.mailsettings.presentation.webaccountsettings.model.AccountSettingsAction
import ch.protonmail.android.mailsettings.presentation.webaccountsettings.model.AccountSettingsOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WebAccountSettingsViewModel @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val userSessionRepository: UserSessionRepository,
    private val themeRepository: ThemeRepository,
    private val observeWebSettingsConfig: ObserveWebSettingsConfig
) : ViewModel() {

    private val _state = MutableStateFlow<WebAccountSettingsState>(WebAccountSettingsState.Loading)
    val state: StateFlow<WebAccountSettingsState> = _state.asStateFlow()

    init {
        // Combine the two flows
        combine(
            observePrimaryUserId().filterNotNull(),
            themeRepository.observe(),
            observeWebSettingsConfig()
        ) { userId, theme, webSettingsConfig ->

            userSessionRepository.forkSession(userId).fold(
                ifRight = { forkedSessionId ->
                    WebAccountSettingsState.Data(webSettingsConfig.toAccountSettingsUrl(forkedSessionId, theme), theme)
                },
                ifLeft = { sessionError ->
                    Timber.e("web-settings: Forking session failed")
                    WebAccountSettingsState.Error("Forking session failed due to $sessionError")
                }
            )
        }
            .onEach { newState ->
                _state.value = newState

                Timber.d("web-settings: State changed: $newState")
            }
            .launchIn(viewModelScope)
    }

    internal fun submit(action: AccountSettingsOperation) {
        viewModelScope.launch {
            when (action) {
                is AccountSettingsAction.OnCloseAccountSettings -> handleCloseSettings()

            }
        }
    }

    @MissingRustApi
    private suspend fun handleCloseSettings() {
        Timber.d("web-settings: Closing account settings, event loop polling will be triggered")
    }
}

private fun WebSettingsConfig.toAccountSettingsUrl(forkedSessionId: ForkedSessionId, theme: Theme): String =
    "$baseUrl?action=$accountSettingsAction&theme=${theme.getUriParam()}#selector=${forkedSessionId.id}".also {
        Timber.d("web-settings: Account settings URL: $it")
    }

private fun Theme.getUriParam(): String = when (this) {
    Theme.SYSTEM_DEFAULT -> "0"
    Theme.LIGHT -> "0"
    Theme.DARK -> "1"
}
