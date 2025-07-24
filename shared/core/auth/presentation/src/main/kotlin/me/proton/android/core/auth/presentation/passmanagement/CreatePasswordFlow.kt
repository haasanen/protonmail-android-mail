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

package me.proton.android.core.auth.presentation.passmanagement

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.proton.android.core.account.domain.usecase.ObservePrimaryCoreAccount
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowManager
import uniffi.proton_account_uniffi.PasswordFlow
import javax.inject.Inject
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowCache.SecondFactorFlow

class CreatePasswordFlow @Inject constructor(
    private val secondFactorFlowManager: SecondFactorFlowManager,
    private val observePrimaryCoreAccount: ObservePrimaryCoreAccount
) {

    suspend operator fun invoke(): PasswordFlow {
        val account = observePrimaryCoreAccount().filterNotNull().first()
        return when (val passwordFlow = secondFactorFlowManager.tryCreatePasswordFlow(account.userId.id)) {
            is SecondFactorFlow.ChangingPassword -> passwordFlow.flow
            is SecondFactorFlow.LoggingIn,
            null -> throw PasswordFlowException("Could not create PasswordFlow.")
        }
    }

    suspend fun clear() {
        secondFactorFlowManager.clearCache(force = true)
    }

    class PasswordFlowException(message: String) : Exception(message)
}
