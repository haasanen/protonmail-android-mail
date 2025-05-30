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

package me.proton.android.core.auth.presentation.secondfactor

import java.util.Optional
import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCaller
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputAction
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.ScreenContentProtector
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@AndroidEntryPoint
class SecondFactorActivity : ProtonActivity() {

    private val mutableAction = MutableStateFlow<Fido2InputAction?>(null)

    private val screenProtector = ScreenContentProtector(ProtectScreenConfiguration())

    @Inject
    lateinit var performTwoFaWithSecurityKey: Optional<PerformTwoFaWithSecurityKey<ActivityResultCaller, Activity>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performTwoFaWithSecurityKey.getOrNull()?.register(this, ::onTwoFaWithSecurityKeyResult)
        addOnBackPressedCallback { onClose() }

        lifecycleScope.launch {
            mutableAction.collect { action ->
                when (action) {
                    is Fido2InputAction.ReadSecurityKey -> {
                        val launchResult =
                            performTwoFaWithSecurityKey.getOrNull()?.invoke(
                                this@SecondFactorActivity,
                                action.options.publicKey
                            )

                        // add observability

                        when (launchResult) {
                            is PerformTwoFaWithSecurityKey.LaunchResult.Failure ->
                                onError(
                                    launchResult.exception.localizedMessage
                                        ?: getString(R.string.auth_login_general_error)
                                )

                            is PerformTwoFaWithSecurityKey.LaunchResult.Success -> Unit
                            null -> {
                                onError(getString(R.string.auth_login_general_error))
                                mutableAction.tryEmit(
                                    Fido2InputAction.SecurityKeyResult(
                                        result = PerformTwoFaWithSecurityKey.Result.EmptyResult,
                                        proof = null
                                    )
                                )
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }

        setContent {
            ProtonTheme {
                SecondFactorInputScreen(
                    onClose = this::onClose,
                    onError = this::onError,
                    onSuccess = this::onSuccess,
                    externalAction = mutableAction,
                    onEmitAction = { action ->
                        if (action == null) {
                            mutableAction.value = null
                        } else {
                            mutableAction.tryEmit(action)
                        }
                    }
                )
            }
        }
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onError(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun onSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onTwoFaWithSecurityKeyResult(
        result: PerformTwoFaWithSecurityKey.Result,
        options: Fido2PublicKeyCredentialRequestOptions
    ) {
        // add observability
        when (result) {
            is PerformTwoFaWithSecurityKey.Result.Success -> onResultSuccess(result = result, options = options)
            else -> mutableAction.tryEmit(Fido2InputAction.SecurityKeyResult(result = result, proof = null))
        }
    }

    private fun onResultSuccess(
        result: PerformTwoFaWithSecurityKey.Result.Success,
        options: Fido2PublicKeyCredentialRequestOptions
    ) {
        val proof = SecondFactorProof.Fido2(
            publicKeyOptions = options,
            clientData = result.response.clientDataJSON,
            authenticatorData = result.response.authenticatorData,
            signature = result.response.signature,
            credentialID = result.rawId
        )
        mutableAction.tryEmit(Fido2InputAction.SecurityKeyResult(result = result, proof = proof))
    }
}
