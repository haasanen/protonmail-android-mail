/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.accountrecovery.presentation.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountrecovery.presentation.LogTag
import me.proton.android.core.accountrecovery.presentation.R
import me.proton.android.core.accountrecovery.presentation.entity.UserRecovery
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryViewState
import me.proton.android.core.accountrecovery.presentation.ui.Arg
import me.proton.android.core.accountrecovery.presentation.ui.CancellationState
import me.proton.android.core.accountrecovery.presentation.usecase.CancelRecovery
import me.proton.android.core.accountrecovery.presentation.usecase.ObserveUserRecovery
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.presentation.utils.StringBox
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import uniffi.proton_mail_uniffi.AccountRecoveryScreenId
import uniffi.proton_mail_uniffi.recordAccountRecoveryScreenView
import javax.inject.Inject

@HiltViewModel
class AccountRecoveryDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeUserRecovery: ObserveUserRecovery,
    private val cancelRecovery: CancelRecovery
) : BaseViewModel<Boolean, AccountRecoveryViewState>(false, AccountRecoveryViewState.Loading) {

    private val userId = CoreUserId(requireNotNull(savedStateHandle.get<String>(Arg.UserId)))

    private val cancellationFlow = MutableStateFlow(CancellationState())
    private val shouldShowCancellationForm = MutableStateFlow(false)
    private val shouldShowRecoveryReset = MutableStateFlow(false)

    val screenId: StateFlow<AccountRecoveryScreenId?> =
        state.map(AccountRecoveryViewState::toScreenId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = null
        )

    override suspend fun FlowCollector<AccountRecoveryViewState>.onError(throwable: Throwable) {
        CoreLogger.e(LogTag.ERROR_OBSERVING_STATE, throwable)
        emit(AccountRecoveryViewState.Error(throwable.message))
    }

    override fun onAction(action: Boolean): Flow<AccountRecoveryViewState> {
        return shouldShowRecoveryReset.flatMapLatest { showRecovery ->
            when {
                action -> flowOf(AccountRecoveryViewState.Closed())
                showRecovery -> flowOf(AccountRecoveryViewState.StartPasswordManager(userId))
                else -> observeState()
            }
        }
    }

    private fun observeState() = combine(
        observeUserRecovery(userId),
        cancellationFlow,
        shouldShowCancellationForm
    ) { userRecovery, cancellationState, showCancellationForm ->
        when (userRecovery?.state?.enum) {
            null, UserRecovery.State.None -> AccountRecoveryViewState.Closed()
            UserRecovery.State.Grace -> onGracePeriod(cancellationState, showCancellationForm, userRecovery)
            UserRecovery.State.Cancelled -> AccountRecoveryViewState.Opened.CancellationHappened
            UserRecovery.State.Insecure -> onInsecurePeriod(
                cancellationState,
                showCancellationForm,
                userRecovery
            )

            UserRecovery.State.Expired -> AccountRecoveryViewState.Opened.RecoveryEnded(email = userRecovery.email)
        }
    }

    private fun onGracePeriod(
        cancellationState: CancellationState,
        showCancellationForm: Boolean,
        userRecovery: UserRecovery
    ): AccountRecoveryViewState = when (showCancellationForm) {
        true -> cancellationState.toViewModelState(
            onCancelPasswordRequest = { startAccountRecoveryCancel(it) },
            onBack = { hideCancellationForm() }
        )

        false -> AccountRecoveryViewState.Opened.GracePeriodStarted(
            email = userRecovery.email,
            remainingHours = userRecovery.remainingHours,
            onShowCancellationForm = { showCancellationForm() }
        )
    }

    private fun onInsecurePeriod(
        cancellationState: CancellationState,
        showCancellationForm: Boolean,
        userRecovery: UserRecovery
    ): AccountRecoveryViewState = when (showCancellationForm) {
        true -> cancellationState.toViewModelState(
            onCancelPasswordRequest = { startAccountRecoveryCancel(it) },
            onBack = { hideCancellationForm() }
        )

        false -> {
            if (userRecovery.selfInitiated && userRecovery.isAccountRecoveryResetEnabled) {
                AccountRecoveryViewState.Opened.PasswordChangePeriodStarted.SelfInitiated(
                    endDate = userRecovery.endDateFormatted,
                    onShowPasswordChangeForm = { startAccountRecoveryReset() },
                    onShowCancellationForm = { showCancellationForm() }
                )
            } else {
                AccountRecoveryViewState.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(
                    endDate = userRecovery.endDateFormatted,
                    onShowCancellationForm = { showCancellationForm() }
                )
            }
        }
    }

    @VisibleForTesting
    internal fun showCancellationForm() {
        shouldShowCancellationForm.update { true }
    }

    private fun hideCancellationForm() {
        shouldShowCancellationForm.update { false }
    }

    private fun startAccountRecoveryReset() {
        shouldShowRecoveryReset.update { true }
    }

    @VisibleForTesting
    internal fun startAccountRecoveryCancel(password: String) = viewModelScope.launchWithResultContext {
        cancellationFlow.update { CancellationState(processing = true) }
        cancellationFlow.value = when {
            password.isEmpty() -> CancellationState(passwordError = StringBox(R.string.presentation_field_required))
            else -> runCatching {
                cancelRecovery(password, userId)
            }.fold(
                onSuccess = { CancellationState(success = true) },
                onFailure = { error -> CancellationState(success = false, error = error) }
            )
        }
    }

    fun onScreenView(screenId: AccountRecoveryScreenId) = viewModelScope.launch {
        recordAccountRecoveryScreenView(screenId)
    }
}

internal fun AccountRecoveryViewState.toScreenId(): AccountRecoveryScreenId? = when (this) {
    is AccountRecoveryViewState.Closed -> null
    is AccountRecoveryViewState.Error -> null
    is AccountRecoveryViewState.Loading -> null
    is AccountRecoveryViewState.StartPasswordManager -> null
    is AccountRecoveryViewState.Opened.CancellationHappened -> AccountRecoveryScreenId.RECOVERY_CANCELLED_INFO
    is AccountRecoveryViewState.Opened.GracePeriodStarted -> AccountRecoveryScreenId.GRACE_PERIOD_INFO
    is AccountRecoveryViewState.Opened.CancelPasswordReset -> AccountRecoveryScreenId.CANCEL_RESET_PASSWORD
    is AccountRecoveryViewState.Opened.PasswordChangePeriodStarted -> AccountRecoveryScreenId.PASSWORD_CHANGE_INFO
    is AccountRecoveryViewState.Opened.RecoveryEnded -> AccountRecoveryScreenId.RECOVERY_EXPIRED_INFO
}
