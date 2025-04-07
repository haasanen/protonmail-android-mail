/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.auth.presentation.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.auth.presentation.AuthOrchestrator
import me.proton.android.core.auth.presentation.ProtonSecureActivity
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.signup.SignUpRoutes.addCountryPickerDialog
import me.proton.android.core.auth.presentation.signup.SignUpRoutes.addCreatePasswordScreen
import me.proton.android.core.auth.presentation.signup.SignUpRoutes.addCreateRecoveryScreen
import me.proton.android.core.auth.presentation.signup.SignUpRoutes.addCreateUsernameScreen
import me.proton.android.core.auth.presentation.signup.SignUpRoutes.addSignUpCongratsScreen
import me.proton.android.core.auth.presentation.signup.SignUpRoutes.addSignUpCreateUserScreen
import me.proton.android.core.auth.presentation.signup.SignUpRoutes.addSkipRecoveryDialog
import me.proton.android.core.auth.presentation.signup.viewmodel.SignUpViewModel
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : ProtonSecureActivity() {

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onDestroy() {
        authOrchestrator.unregister()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { onClose() }

        authOrchestrator.register(this)

        setContent {
            ProtonTheme {
                val navController = rememberNavController()

                NavHost(
                    route = "sign_up",
                    navController = navController,
                    startDestination = SignUpRoutes.Route.CreateUsername()
                ) {
                    addCreateUsernameScreen(
                        navController = navController,
                        onClose = { onClose() },
                        onErrorMessage = { onErrorMessage(it) },
                        navGraphViewModel = signUpViewModel
                    )
                    addCreatePasswordScreen(
                        navController = navController,
                        onErrorMessage = { onErrorMessage(it) },
                        navGraphViewModel = signUpViewModel
                    )
                    addCreateRecoveryScreen(
                        navController = navController,
                        onErrorMessage = { onErrorMessage(it) },
                        navGraphViewModel = signUpViewModel
                    )
                    addSkipRecoveryDialog(
                        navController = navController,
                        navGraphViewModel = signUpViewModel
                    )
                    addCountryPickerDialog(
                        navController = navController,
                        navGraphViewModel = signUpViewModel
                    )
                    addSignUpCreateUserScreen(
                        onErrorMessage = { onErrorMessage(it) },
                        navController = navController,
                        navGraphViewModel = signUpViewModel
                    )
                    addSignUpCongratsScreen(
                        onSuccess = { userId -> onSuccess(userId) },
                        onError = {
                            onErrorMessage(it)
                            onClose()
                        },
                        navGraphViewModel = signUpViewModel
                    )
                }
            }
        }
    }

    private fun onErrorMessage(message: String?) {
        val errorMessage = if (message.isNullOrEmpty()) {
            getString(R.string.presentation_error_general)
        } else message
        errorToast(errorMessage)
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onSuccess(userId: String) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ARG_OUTPUT, SignupOutput(userId = userId)) }
        )
        finish()
    }

    companion object {

        const val ARG_OUTPUT = "arg.signupOutput"
    }
}
