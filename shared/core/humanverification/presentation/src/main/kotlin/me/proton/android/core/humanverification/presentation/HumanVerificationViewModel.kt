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

package me.proton.android.core.humanverification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailsession.domain.model.RustApiConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.core.humanverification.domain.ChallengeNotifierCallback
import me.proton.android.core.humanverification.presentation.HV3ResponseMessage.MessageType
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.util.kotlin.CoreLogger
import uniffi.proton_mail_uniffi.ApiConfig
import uniffi.proton_mail_uniffi.AppDetails
import uniffi.proton_mail_uniffi.ChallengeLoader
import uniffi.proton_mail_uniffi.HumanVerificationScreenId
import uniffi.proton_mail_uniffi.HumanVerificationStatus
import uniffi.proton_mail_uniffi.NewChallengeLoaderResult
import uniffi.proton_mail_uniffi.newChallengeLoader
import uniffi.proton_mail_uniffi.recordHumanVerificationResult
import uniffi.proton_mail_uniffi.recordHumanVerificationScreenView
import javax.inject.Inject

@HiltViewModel
class HumanVerificationViewModel @Inject constructor(
    private val isWebViewDebuggingEnabled: IsWebViewDebuggingEnabled,
    private val updateHumanVerificationURL: UpdateHumanVerificationURL,
    private val challengeNotifierCallback: ChallengeNotifierCallback,
    private val rustApiConfig: RustApiConfig
) : ViewModel() {

    private val mutableAction = MutableStateFlow<HumanVerificationAction?>(null)

    val state: StateFlow<HumanVerificationViewState> = mutableAction.flatMapLatest { action ->
        when (action) {
            null -> flowOf(HumanVerificationViewState.Idle)
            is HumanVerificationAction.Load -> with(action) {
                onLoad(url, defaultCountry, recoveryPhone, locale, headers)
            }

            is HumanVerificationAction.Verify -> onVerify(action.result)
            is HumanVerificationAction.Cancel -> onCancel()
            is HumanVerificationAction.Failure.ResourceLoadingError -> onFailure(action.message)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), HumanVerificationViewState.Idle)

    fun submit(action: HumanVerificationAction) = viewModelScope.launch {
        mutableAction.emit(action)
    }

    fun onScreenView() {
        viewModelScope.launch {
            recordHumanVerificationScreenView(screenId = HumanVerificationScreenId.V3)
        }
    }

    private fun onLoad(
        url: String,
        defaultCountry: String?,
        recoveryPhone: String?,
        locale: String?,
        headers: List<Pair<String, String>>?
    ): Flow<HumanVerificationViewState> = flow {
        val loader = when (
            val result = newChallengeLoader(
                ApiConfig(rustApiConfig.userAgent, rustApiConfig.envId, rustApiConfig.proxy),
                AppDetails(rustApiConfig.platform, rustApiConfig.product, rustApiConfig.appVersion)
            )
        ) {
            is NewChallengeLoaderResult.Error -> {
                CoreLogger.e(LogTag.DEFAULT, result.v1.toString())
                null
            }

            is NewChallengeLoaderResult.Ok -> result.v1
        }
        if (loader == null) {
            emit(HumanVerificationViewState.Error.Loader)
        } else {
            emitAll(updateUrl(loader, url, defaultCountry, recoveryPhone, locale, headers))
        }
    }

    private fun onCancel(): Flow<HumanVerificationViewState> = flow {
        challengeNotifierCallback.onHumanVerificationCancel()
        recordHumanVerificationResult(HumanVerificationStatus.CANCELLED)
        emit(HumanVerificationViewState.Cancel)
    }

    private fun onFailure(message: String?): Flow<HumanVerificationViewState> = flow {
        challengeNotifierCallback.onHumanVerificationFailed()
        recordHumanVerificationResult(HumanVerificationStatus.FAILED)
        emit(HumanVerificationViewState.Error.General(message))
    }

    private fun onVerify(result: HV3ResponseMessage): Flow<HumanVerificationViewState> = flow {
        when (result.type) {
            HV3ResponseMessage.Type.Success -> {
                val token = requireNotNull(result.payload?.token)
                val tokenType = requireNotNull(result.payload?.type)
                challengeNotifierCallback.onHumanVerificationSuccess(tokenType, token)
                recordHumanVerificationResult(HumanVerificationStatus.SUCCEEDED)
                emit(HumanVerificationViewState.Success(token, tokenType))
            }

            HV3ResponseMessage.Type.Notification -> {
                val message = requireNotNull(result.payload?.text)
                val messageType = requireNotNull(result.payload?.type?.let { MessageType.map[it] })
                emit(HumanVerificationViewState.Notify(messageType, message))
            }

            HV3ResponseMessage.Type.Close -> {
                onCancel()
            }

            HV3ResponseMessage.Type.Error -> {
                onFailure(result.payload?.text)
            }

            HV3ResponseMessage.Type.Loaded -> {
                // add observability once ready, no other action needed.
            }

            HV3ResponseMessage.Type.Resize -> {
                // No action needed
            }
        }
    }

    @Suppress("LongParameterList")
    private fun updateUrl(
        loader: ChallengeLoader,
        url: String,
        defaultCountry: String?,
        recoveryPhone: String?,
        locale: String?,
        headers: List<Pair<String, String>>?
    ) = flow {
        emit(
            HumanVerificationViewState.Load(
                extraHeaders = headers,
                fullUrl = updateHumanVerificationURL(url, defaultCountry, recoveryPhone, locale),
                isWebViewDebuggingEnabled = isWebViewDebuggingEnabled(),
                loader = loader
            )
        )
    }
}
