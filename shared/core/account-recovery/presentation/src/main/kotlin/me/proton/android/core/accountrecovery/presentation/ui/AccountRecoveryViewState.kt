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

package me.proton.android.core.accountrecovery.presentation.ui

import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.core.presentation.utils.StringBox

sealed class AccountRecoveryViewState {

    sealed class Opened : AccountRecoveryViewState() {
        data class GracePeriodStarted(
            val email: String,
            val remainingHours: Int,
            val onShowCancellationForm: () -> Unit = {}
        ) : Opened()

        sealed class PasswordChangePeriodStarted : Opened() {
            data class OtherDeviceInitiated(
                val endDate: String, // formatted day, e.g. "16 Aug"
                val onShowCancellationForm: () -> Unit = {}
            ) : PasswordChangePeriodStarted()

            data class SelfInitiated(
                val endDate: String, // formatted day, e.g. "16 Aug"
                val onShowPasswordChangeForm: () -> Unit = {},
                val onShowCancellationForm: () -> Unit = {}
            ) : PasswordChangePeriodStarted()
        }

        data class CancelPasswordReset(
            val processing: Boolean = false,
            val passwordError: StringBox? = null,
            val onCancelPasswordRequest: (String) -> Unit = {},
            val onBack: () -> Unit = {}
        ) : Opened()

        object CancellationHappened : Opened()

        data class RecoveryEnded(val email: String) : Opened()
    }

    data class Closed(val hasCancelledSuccessfully: Boolean = false) : AccountRecoveryViewState()

    data class StartPasswordManager(val userId: CoreUserId) : AccountRecoveryViewState()

    object Loading : AccountRecoveryViewState()

    data class Error(val message: String?) : AccountRecoveryViewState()
}
