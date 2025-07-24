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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.android.core.auth.presentation.login.getErrorMessage
import me.proton.android.core.auth.presentation.passmanagement.getErrorMessage
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowCache.SecondFactorFlow
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowManager
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_account_uniffi.LoginFlowSubmitFidoResult
import uniffi.proton_account_uniffi.PasswordFlow
import uniffi.proton_account_uniffi.PasswordFlowSubmitFidoResult
import uniffi.proton_mail_uniffi.MailSessionToUserSessionResult
import javax.inject.Inject

class SubmitFido @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secondFactorFlowManager: SecondFactorFlowManager
) {

    suspend fun execute(userId: String, proof: SecondFactorProof.Fido2): SubmitFidoResult {
        val submitFidoFlow = secondFactorFlowManager.getSecondFactorFlow(userId)
            ?: return SubmitFidoResult.SessionClosed

        return when (submitFidoFlow) {
            is SecondFactorFlow.LoggingIn -> handleLoginFlowSubmission(submitFidoFlow.flow, proof)
            is SecondFactorFlow.ChangingPassword -> handlePasswordFlowSubmission(submitFidoFlow.flow, proof)
        }
    }

    private suspend fun handleLoginFlowSubmission(
        loginFlow: LoginFlow,
        proof: SecondFactorProof.Fido2
    ): SubmitFidoResult {
        val fidoData = proof.toFido2Data()
        return when (val submit = loginFlow.submitFido(fidoData)) {
            is LoginFlowSubmitFidoResult.Error ->
                SubmitFidoResult.OtherError(submit.v1.getErrorMessage(context))

            is LoginFlowSubmitFidoResult.Ok ->
                SubmitFidoResult.Success(loginFlow)
        }
    }

    private suspend fun handlePasswordFlowSubmission(
        passwordFlow: PasswordFlow,
        proof: SecondFactorProof.Fido2
    ): SubmitFidoResult {
        val fidoData = proof.toFido2Data()
        return when (val submit = passwordFlow.submitFido(fidoData)) {
            is PasswordFlowSubmitFidoResult.Error -> SubmitFidoResult.OtherError(submit.v1.getErrorMessage(context))
            is PasswordFlowSubmitFidoResult.Ok -> SubmitFidoResult.Success()
        }
    }

    suspend fun convertToUserContext(loginFlow: LoginFlow): MailSessionToUserSessionResult =
        secondFactorFlowManager.convertToUserContext(loginFlow)
}
