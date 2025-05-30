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
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_account_uniffi.LoginFlowSubmitFidoResult
import uniffi.proton_mail_uniffi.MailSessionResumeLoginFlowResult
import uniffi.proton_mail_uniffi.MailSessionToUserSessionResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmitFido @Inject constructor(
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) {

    suspend fun execute(userId: String, proof: SecondFactorProof.Fido2): SubmitFidoResult {
        val loginFlowResult = sessionManager.getOrResumeLoginFlow(userId)
            ?: return SubmitFidoResult.SessionClosed

        return when (loginFlowResult) {
            is MailSessionResumeLoginFlowResult.Error ->
                SubmitFidoResult.Error(loginFlowResult.v1)

            is MailSessionResumeLoginFlowResult.Ok ->
                handleLoginFlowSubmission(loginFlowResult.v1, proof)
        }
    }

    suspend fun convertToUserContext(loginFlow: LoginFlow): MailSessionToUserSessionResult =
        sessionManager.convertToUserContext(loginFlow)

    private suspend fun handleLoginFlowSubmission(
        loginFlow: LoginFlow,
        proof: SecondFactorProof.Fido2
    ): SubmitFidoResult {
        val fidoData = proof.toFido2Data()
        return when (val submit = loginFlow.submitFido(fidoData)) {
            is LoginFlowSubmitFidoResult.Error ->
                SubmitFidoResult.GeneralError(submit.v1.getErrorMessage(context))

            is LoginFlowSubmitFidoResult.Ok ->
                SubmitFidoResult.Success(loginFlow)
        }
    }
}
