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

package me.proton.android.core.auth.presentation.secondfactor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.login.getErrorMessage
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorArg.getUserId
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputAction.Close
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputAction.Load
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputAction.SelectTab
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Closed
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Error
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Idle
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Loading
import me.proton.android.core.auth.presentation.secondfactor.fido.GetFidoOptions
import me.proton.core.compose.viewmodel.BaseViewModel
import uniffi.proton_mail_uniffi.LoginScreenId
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionResumeLoginFlowResult
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.recordLoginScreenView
import javax.inject.Inject

@HiltViewModel
class SecondFactorInputViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionInterface: MailSession,
    private val getFidoOptions: GetFidoOptions,
    private val secondFactorFlowManager: SecondFactorFlowManager
) : BaseViewModel<SecondFactorInputAction, SecondFactorInputState>(
    initialState = Idle,
    initialAction = Load,
    sharingStarted = SharingStarted.Lazily
) {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val allAvailableTabs = listOf(SecondFactorTab.SecurityKey, SecondFactorTab.Otp)
    private var userAvailableTabs: List<SecondFactorTab> = emptyList()

    override suspend fun FlowCollector<SecondFactorInputState>.onError(throwable: Throwable) {
        emit(Error.SecondFactor)
    }

    override fun onAction(action: SecondFactorInputAction): Flow<SecondFactorInputState> {
        return when (action) {
            is Close -> onClose()
            is Load -> onLoad()
            is SelectTab -> onSelectTab(action.index)
        }
    }

    fun onScreenView() = viewModelScope.launch {
        recordLoginScreenView(LoginScreenId.SECOND_FACTOR)
    }

    private fun onLoad(): Flow<SecondFactorInputState> = flow {
        val account = sessionInterface.getAccountById(userId)
        if (account == null) {
            emitAll(onClose())
            return@flow
        }

        val session = sessionInterface.getSessionsForAccount(account)?.firstOrNull()
        if (session == null) {
            emitAll(onClose())
            return@flow
        }

        userAvailableTabs = determineAvailableTabs()
        val defaultTab = getDefaultTab()

        emit(Loading(selectedTab = defaultTab, tabs = userAvailableTabs))
    }

    private suspend fun determineAvailableTabs(): List<SecondFactorTab> {
        val fido2Options = getFidoOptions.invoke(userId)
        val securityKeys = fido2Options?.registeredKeys ?: emptyList()
        val hasSecurityKeys = securityKeys.isNotEmpty()

        return if (hasSecurityKeys) {
            allAvailableTabs
        } else {
            listOf(SecondFactorTab.Otp)
        }
    }

    private fun getDefaultTab(): SecondFactorTab = if (userAvailableTabs.contains(SecondFactorTab.SecurityKey)) {
        SecondFactorTab.SecurityKey
    } else {
        SecondFactorTab.Otp
    }

    private fun onSelectTab(index: Int): Flow<SecondFactorInputState> = flow {
        if (index < 0 || index >= userAvailableTabs.size) {
            return@flow
        }

        val selectedTab = userAvailableTabs[index]
        emit(Loading(selectedTab = selectedTab, tabs = userAvailableTabs))
    }

    private fun onClose(): Flow<SecondFactorInputState> = flow {
        secondFactorFlowManager.clearCache()
        emit(Closed)
    }
}
