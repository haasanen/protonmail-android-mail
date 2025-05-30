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

package me.proton.android.core.auth.presentation.secondfactor.fido

import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationOptions

sealed interface Fido2InputState {
    data object Idle : Fido2InputState
    data object InitiatedReadingSecurityKey : Fido2InputState
    data class ReadingSecurityKey(val options: Fido2AuthenticationOptions) : Fido2InputState
    data object Authenticating : Fido2InputState
    data object Awaiting2Pass : Fido2InputState
    data object LoggedIn : Fido2InputState
    data object Closed : Fido2InputState

    sealed interface Error : Fido2InputState {
        sealed interface ReadingSecurityKey : Error {
            data object Cancelled : ReadingSecurityKey
            data object NoCredentials : ReadingSecurityKey
            data object Unknown : ReadingSecurityKey
            data object Empty : ReadingSecurityKey
            data class Message(val error: String?) : ReadingSecurityKey
        }

        data class SubmitFido(val error: String?) : Error
        data object StoredKeysConfig : ReadingSecurityKey
        data class General(val error: String?) : Error
    }
}
