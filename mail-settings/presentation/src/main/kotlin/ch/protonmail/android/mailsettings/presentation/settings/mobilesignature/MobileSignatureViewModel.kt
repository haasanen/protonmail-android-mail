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

package ch.protonmail.android.mailsettings.presentation.settings.mobilesignature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.repository.MobileSignatureRepository
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.mapper.MobileSignatureUiModelMapper
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureEvent
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureOperation
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureState
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureViewAction
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.reducer.MobileSignatureReducer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MobileSignatureViewModel @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val mobileSignatureRepository: MobileSignatureRepository,
    private val reducer: MobileSignatureReducer
) : ViewModel() {

    private val mutableState: MutableStateFlow<MobileSignatureState> = MutableStateFlow(MobileSignatureState.Loading)
    val state: StateFlow<MobileSignatureState> = mutableState.asStateFlow()

    init {
        observePrimaryUserId()
            .filterNotNull()
            .flatMapLatest { userId ->
                mobileSignatureRepository.observeMobileSignature(userId)
            }
            .onEach {
                emitNewStateFor(
                    MobileSignatureEvent.SignatureLoaded(
                        signatureSettingsUiModel = MobileSignatureUiModelMapper.toSettingsUiModel(it)
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun submit(action: MobileSignatureViewAction) {
        viewModelScope.launch {
            when (action) {
                is MobileSignatureViewAction.ToggleSignatureEnabled ->
                    handleToggleSignatureEnabled(action.enabled)

                is MobileSignatureViewAction.EditSignatureValue ->
                    emitNewStateFor(action)

                is MobileSignatureViewAction.UpdateSignatureValue ->
                    updateSignatureValue(action.value)
            }
        }
    }

    private fun emitNewStateFor(operation: MobileSignatureOperation) {
        val currentState = state.value
        mutableState.update { reducer.newStateFrom(currentState, operation) }
    }

    private suspend fun handleToggleSignatureEnabled(enabled: Boolean) {
        observePrimaryUserId().first()?.let { userId ->
            mobileSignatureRepository.setMobileSignatureEnabled(userId, enabled)
        }
    }

    private suspend fun updateSignatureValue(value: String) {
        observePrimaryUserId().first()?.let { userId ->
            mobileSignatureRepository.setMobileSignature(userId, value)
        }
    }
}
