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

import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowManager
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowCache.SecondFactorFlow
import uniffi.proton_account_uniffi.Fido2ResponseFfi
import uniffi.proton_account_uniffi.PasswordFlowGetFidoDetailsResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFidoOptions @Inject constructor(
    private val secondFactorFlowManager: SecondFactorFlowManager
) {

    suspend operator fun invoke(userId: String): Fido2ResponseFfi? {
        val fidoFlowResult = secondFactorFlowManager.getSecondFactorFlow(userId) ?: return null

        return when (fidoFlowResult) {
            is SecondFactorFlow.LoggingIn -> fidoFlowResult.flow.getFidoDetails()
            is SecondFactorFlow.ChangingPassword -> {
                when (val fidoDetailsResult = fidoFlowResult.flow.getFidoDetails()) {
                    is PasswordFlowGetFidoDetailsResult.Error -> null
                    is PasswordFlowGetFidoDetailsResult.Ok -> fidoDetailsResult.v1
                }
            }
        }
    }
}
