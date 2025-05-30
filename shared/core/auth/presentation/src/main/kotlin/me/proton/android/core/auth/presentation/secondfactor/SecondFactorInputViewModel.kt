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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import me.proton.android.core.auth.presentation.LogTag
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorArg.getUserId
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputAction.Close
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputAction.Load
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputAction.SelectTab
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Idle
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Loading
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Error
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputState.Closed
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.util.kotlin.CoreLogger
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetAccountResult
import uniffi.proton_mail_uniffi.MailSessionGetAccountSessionsResult
import uniffi.proton_mail_uniffi.MailSessionUserContextFromSessionResult
import uniffi.proton_mail_uniffi.MailUserSessionUserSettingsResult
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import javax.inject.Inject

@HiltViewModel
class SecondFactorInputViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionInterface: MailSession
) : BaseViewModel<SecondFactorInputAction, SecondFactorInputState>(
    initialState = Idle,
    initialAction = Load
) {

    private val userId by lazy { savedStateHandle.getUserId() }
    private val allAvailableTabs = listOf(SecondFactorTab.SecurityKey, SecondFactorTab.Otp)
    private var userAvailableTabs: List<SecondFactorTab> = emptyList()

    override suspend fun FlowCollector<SecondFactorInputState>.onError(throwable: Throwable) {
        emit(Error)
    }

    override fun onAction(action: SecondFactorInputAction): Flow<SecondFactorInputState> {
        return when (action) {
            is Close -> onClose()
            is Load -> onLoad()
            is SelectTab -> onSelectTab(action.index)
        }
    }

    private fun onLoad(): Flow<SecondFactorInputState> = flow {
        val session = getSession(getAccount(userId))?.firstOrNull()
        val userSettings = when (val result = session?.let { sessionInterface.userContextFromSession(session) }) {
            null -> null
            is MailSessionUserContextFromSessionResult.Error -> null
            is MailSessionUserContextFromSessionResult.Ok -> result.v1.userSettings()
        }

        val registeredKeys = when (userSettings) {
            null -> null
            is MailUserSessionUserSettingsResult.Error -> emptyList()
            is MailUserSessionUserSettingsResult.Ok -> userSettings.v1.twoFactorAuth.registeredKeys
        }

        val hasSecurityKeys = registeredKeys?.isNotEmpty() ?: false

        userAvailableTabs = if (hasSecurityKeys) {
            allAvailableTabs
        } else {
            listOf(SecondFactorTab.Otp)
        }

        val defaultTab = if (userAvailableTabs.contains(SecondFactorTab.SecurityKey)) {
            SecondFactorTab.SecurityKey
        } else {
            SecondFactorTab.Otp
        }

        emit(Loading(selectedTab = defaultTab, tabs = userAvailableTabs))
    }

    private fun onSelectTab(index: Int): Flow<SecondFactorInputState> = flow {
        val selectedTab = userAvailableTabs.getOrElse(index) { SecondFactorTab.Otp }
        emit(Loading(selectedTab = selectedTab, tabs = userAvailableTabs))
    }

    private fun onClose(): Flow<SecondFactorInputState> = flow {
        sessionInterface.deleteAccount(userId)
        emit(Closed)
    }

    private suspend fun getSession(account: StoredAccount?): List<StoredSession>? {
        if (account == null) {
            return null
        }

        return when (val result = sessionInterface.getAccountSessions(account)) {
            is MailSessionGetAccountSessionsResult.Error -> {
                CoreLogger.e(LogTag.LOGIN, result.v1.toString())
                null
            }

            is MailSessionGetAccountSessionsResult.Ok -> result.v1
        }
    }

    private suspend fun getAccount(userId: String) = when (val result = sessionInterface.getAccount(userId)) {
        is MailSessionGetAccountResult.Error -> {
            CoreLogger.e(LogTag.LOGIN, result.v1.toString())
            null
        }

        is MailSessionGetAccountResult.Ok -> result.v1
    }
}
