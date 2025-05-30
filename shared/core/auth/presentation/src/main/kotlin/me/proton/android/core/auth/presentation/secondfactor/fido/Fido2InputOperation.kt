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
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey

sealed interface Fido2InputOperation

sealed interface Fido2InputAction : Fido2InputOperation {
    data class Load(val unused: Long = System.currentTimeMillis()) : Fido2InputAction
    data object Reset : Fido2InputAction
    data class Authenticate(val unused: Long = System.currentTimeMillis()) : Fido2InputAction
    data class ReadSecurityKey(val options: Fido2AuthenticationOptions) : Fido2InputAction

    data class SecurityKeyResult(
        val result: PerformTwoFaWithSecurityKey.Result,
        val proof: SecondFactorProof.Fido2?
    ) : Fido2InputAction
}
