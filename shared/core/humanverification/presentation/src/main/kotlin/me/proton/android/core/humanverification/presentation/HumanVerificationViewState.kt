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

import uniffi.proton_mail_uniffi.ChallengeLoader

sealed interface HumanVerificationViewState {
    data object Idle : HumanVerificationViewState
    data object Cancel : HumanVerificationViewState
    data class Load(
        val extraHeaders: List<Pair<String, String>>?,
        val fullUrl: String,
        val isWebViewDebuggingEnabled: Boolean,
        val loader: ChallengeLoader
    ) : HumanVerificationViewState

    sealed interface Error : HumanVerificationViewState {
        data class General(val message: String?) : Error
        data object Loader : Error
    }
    data class Success(val token: String, val type: String) : HumanVerificationViewState
    data class Notify(val messageType: HV3ResponseMessage.MessageType, val message: String) : HumanVerificationViewState

    val isLoading: Boolean
        get() = when (this) {
            is Load -> true
            else -> false
        }
}
