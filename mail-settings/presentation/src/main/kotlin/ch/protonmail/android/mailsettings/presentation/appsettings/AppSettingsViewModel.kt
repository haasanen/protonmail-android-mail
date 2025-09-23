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

package ch.protonmail.android.mailsettings.presentation.appsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import ch.protonmail.android.mailsettings.presentation.appsettings.usecase.GetAppIconDescription
import ch.protonmail.android.mailsettings.presentation.appsettings.usecase.GetNotificationsEnabled
import ch.protonmail.android.mailupselling.presentation.usecase.ObserveUpsellingVisibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AppSettingsViewModel @Inject constructor(
    val appSettingsRepository: AppSettingsRepository,
    val getNotificationsEnabled: GetNotificationsEnabled,
    val observeUpsellingVisibility: ObserveUpsellingVisibility,
    val getAppIconDescription: GetAppIconDescription
) : ViewModel() {

    val state = combine(
        appSettingsRepository.observeAppSettings(),
        observeUpsellingVisibility()
    ) { appSettings, upsellVisibility ->
        val notificationsEnabled = getNotificationsEnabled()
        val appIconDescription = getAppIconDescription()
        val uiModel = AppSettingsUiModelMapper.toUiModel(appSettings, notificationsEnabled, appIconDescription)
        AppSettingsState.Data(settings = uiModel, upsellingVisibility = upsellVisibility)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMs),
            initialValue = AppSettingsState.Loading
        )

    internal fun submit(intent: AppSettingsAction) {
        viewModelScope.launch {
            when (intent) {
                is ToggleAlternativeRouting -> updateAlternativeRouting(intent.value)
                is ToggleUseCombinedContacts -> updateUseCombinedContacts(intent.value)
            }
        }
    }

    private suspend fun updateAlternativeRouting(value: Boolean) = appSettingsRepository.updateAlternativeRouting(value)
    private suspend fun updateUseCombinedContacts(value: Boolean) =
        appSettingsRepository.updateUseCombineContacts(value)

    companion object {

        // needs a stop timeout MS much shorter than our standard because we can change the notification settings
        // and return to the app in less than 2 seconds and our toggle value needs to update
        const val stopTimeoutMs = 1500L
    }
}
