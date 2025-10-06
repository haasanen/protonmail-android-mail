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

package ch.protonmail.android.mailsettings.presentation.settings.signature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.repository.MobileSignatureRepository
import ch.protonmail.android.mailsettings.presentation.settings.signature.mapper.MobileSignatureUiModelMapper
import ch.protonmail.android.mailsettings.presentation.settings.signature.model.MobileSignatureMenuState
import ch.protonmail.android.mailupselling.presentation.usecase.ObserveUpsellingVisibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class SignatureSettingsMenuViewModel @Inject constructor(
    observePrimaryUserId: ObservePrimaryUserId,
    private val mobileSignatureRepository: MobileSignatureRepository,
    val observeUpsellingVisibility: ObserveUpsellingVisibility
) : ViewModel() {

    val state = observePrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                mobileSignatureRepository.observeMobileSignature(userId),
                observeUpsellingVisibility()
            ) { mobileSignaturePreference, upsellVisibility ->
                val uiModel = MobileSignatureUiModelMapper.toUiModel(mobileSignaturePreference)
                MobileSignatureMenuState.Data(settings = uiModel, upsellingVisibility = upsellVisibility)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = MobileSignatureMenuState.Loading
        )
}
