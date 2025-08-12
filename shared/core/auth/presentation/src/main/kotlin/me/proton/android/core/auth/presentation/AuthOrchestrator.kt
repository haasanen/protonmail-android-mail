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

package me.proton.android.core.auth.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.android.core.auth.presentation.login.LoginHelpOutput
import me.proton.android.core.auth.presentation.login.LoginInput
import me.proton.android.core.auth.presentation.login.LoginOutput
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementInput
import me.proton.android.core.auth.presentation.signup.SignupOutput
import javax.inject.Inject

@Suppress("TooManyFunctions")
class AuthOrchestrator @Inject constructor() {

    private var addAccountWorkflowLauncher: ActivityResultLauncher<Unit>? = null
    private var loginWorkflowLauncher: ActivityResultLauncher<LoginInput>? = null
    private var loginHelpWorkflowLauncher: ActivityResultLauncher<Unit>? = null
    private var secondFactorWorkflowLauncher: ActivityResultLauncher<String>? = null
    private var twoPassModeWorkflowLauncher: ActivityResultLauncher<String>? = null
    private var signupWorkflowLauncher: ActivityResultLauncher<Unit>? = null
    private var passManagementWorkflowLauncher: ActivityResultLauncher<PasswordManagementInput>? = null
    private var securityKeysLauncher: ActivityResultLauncher<Unit>? = null

    private var onAddAccountResultListener: ((result: Boolean) -> Unit)? = {}
    private var onLoginResultListener: ((result: LoginOutput?) -> Unit)? = {}
    private var onLoginHelpResultListener: ((result: LoginHelpOutput?) -> Unit)? = {}
    private var onSecondFactorResultListener: ((result: Boolean) -> Unit)? = {}
    private var onTwoPassModeResultListener: ((result: Boolean) -> Unit)? = {}
    private var onSignUpResultListener: ((result: SignupOutput?) -> Unit)? = {}
    private var onPassManagementResultListener: ((result: Boolean) -> Unit)? = {}
    private var onSecurityKeysResultListener: ((result: Boolean) -> Unit)? = {}

    private fun registerAddAccountResult(caller: ActivityResultCaller): ActivityResultLauncher<Unit> =
        caller.registerForActivityResult(StartAddAccount) {
            onAddAccountResultListener?.invoke(it)
        }

    private fun registerLoginResult(caller: ActivityResultCaller): ActivityResultLauncher<LoginInput> =
        caller.registerForActivityResult(StartLogin) {
            onLoginResultListener?.invoke(it)
        }

    private fun registerLoginHelpResult(caller: ActivityResultCaller): ActivityResultLauncher<Unit> =
        caller.registerForActivityResult(StartLoginHelp) {
            onLoginHelpResultListener?.invoke(it)
        }

    private fun registerSecondFactorResult(caller: ActivityResultCaller): ActivityResultLauncher<String> =
        caller.registerForActivityResult(StartSecondFactor) {
            onSecondFactorResultListener?.invoke(it)
        }

    private fun registerTwoPassModeResult(caller: ActivityResultCaller): ActivityResultLauncher<String> =
        caller.registerForActivityResult(StartTwoPassMode) {
            onTwoPassModeResultListener?.invoke(it)
        }

    private fun registerSignUpResult(caller: ActivityResultCaller): ActivityResultLauncher<Unit> =
        caller.registerForActivityResult(StartSignUp) {
            onSignUpResultListener?.invoke(it)
        }

    private fun registerPassManagementResult(caller: ActivityResultCaller): ActivityResultLauncher<PasswordManagementInput> =
        caller.registerForActivityResult(StartPasswordManagement) {
            onPassManagementResultListener?.invoke(it)
        }

    private fun registerSecurityKeysResult(caller: ActivityResultCaller): ActivityResultLauncher<Unit> =
        caller.registerForActivityResult(StartSecurityKeys) {
            onSecurityKeysResultListener?.invoke(it)
        }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call register(context) before starting workflow!" }

    fun setOnAddAccountResult(block: (result: Boolean) -> Unit) {
        onAddAccountResultListener = block
    }

    fun setOnLoginResult(block: (result: LoginOutput?) -> Unit) {
        onLoginResultListener = block
    }

    fun setOnLoginHelpResult(block: (result: LoginHelpOutput?) -> Unit) {
        onLoginHelpResultListener = block
    }

    fun setOnSecondFactorResult(block: (result: Boolean) -> Unit) {
        onSecondFactorResultListener = block
    }

    fun setOnTwoPassModeResult(block: (result: Boolean) -> Unit) {
        onTwoPassModeResultListener = block
    }

    fun setOnSignUpResult(block: (result: SignupOutput?) -> Unit) {
        onSignUpResultListener = block
    }

    fun setOnPassManagementResult(block: (result: Boolean) -> Unit) {
        onPassManagementResultListener = block
    }

    fun setOnSecurityKeysResult(block: (result: Boolean) -> Unit) {
        onSecurityKeysResultListener = block
    }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        addAccountWorkflowLauncher = registerAddAccountResult(caller)
        loginWorkflowLauncher = registerLoginResult(caller)
        loginHelpWorkflowLauncher = registerLoginHelpResult(caller)
        secondFactorWorkflowLauncher = registerSecondFactorResult(caller)
        twoPassModeWorkflowLauncher = registerTwoPassModeResult(caller)
        signupWorkflowLauncher = registerSignUpResult(caller)
        passManagementWorkflowLauncher = registerPassManagementResult(caller)
        securityKeysLauncher = registerSecurityKeysResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        addAccountWorkflowLauncher?.unregister()
        loginWorkflowLauncher?.unregister()
        loginHelpWorkflowLauncher?.unregister()
        secondFactorWorkflowLauncher?.unregister()
        twoPassModeWorkflowLauncher?.unregister()
        signupWorkflowLauncher?.unregister()
        passManagementWorkflowLauncher?.unregister()
        securityKeysLauncher?.unregister()

        addAccountWorkflowLauncher = null
        loginWorkflowLauncher = null
        loginHelpWorkflowLauncher = null
        secondFactorWorkflowLauncher = null
        twoPassModeWorkflowLauncher = null
        signupWorkflowLauncher = null
        passManagementWorkflowLauncher = null
        securityKeysLauncher = null

        onAddAccountResultListener = null
        onLoginResultListener = null
        onLoginHelpResultListener = null
        onSecondFactorResultListener = null
        onTwoPassModeResultListener = null
        onSignUpResultListener = null
        onPassManagementResultListener = null
        onSecurityKeysResultListener = null
    }

    /**
     * Starts the Add Account workflow (sign in or sign up).
     */
    fun startAddAccountWorkflow() {
        checkRegistered(addAccountWorkflowLauncher).launch(Unit)
    }

    /**
     * Starts the Login workflow.
     */
    fun startLoginWorkflow(loginInput: LoginInput = LoginInput()) {
        checkRegistered(loginWorkflowLauncher).launch(loginInput)
    }

    /**
     * Starts the Login Help workflow.
     */
    fun startLoginHelpWorkflow() {
        checkRegistered(loginHelpWorkflowLauncher).launch(Unit)
    }

    /**
     * Starts the workflow the submitting a second factor.
     */
    fun startSecondFactorWorkflow(userId: String) {
        checkRegistered(secondFactorWorkflowLauncher).launch(userId)
    }

    /**
     * Starts the workflow for submitting the second (mailbox) password.
     */
    fun startTwoPassModeWorkflow(userId: String) {
        checkRegistered(twoPassModeWorkflowLauncher).launch(userId)
    }

    /**
     * Starts the Signup workflow.
     */
    fun startSignUpWorkflow() {
        checkRegistered(signupWorkflowLauncher).launch(Unit)
    }

    /**
     * Starts the Signup workflow.
     */
    fun startPassManagement(userId: String?) {
        checkRegistered(passManagementWorkflowLauncher).launch(PasswordManagementInput(userId))
    }

    /**
     * Starts the Security keys.
     */
    fun startSecurityKeys() {
        checkRegistered(securityKeysLauncher).launch(Unit)
    }
}

fun AuthOrchestrator.onAddAccountResult(block: (result: Boolean) -> Unit): AuthOrchestrator {
    setOnAddAccountResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginResult(block: (result: LoginOutput?) -> Unit): AuthOrchestrator {
    setOnLoginResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginHelpResult(block: (result: LoginHelpOutput?) -> Unit): AuthOrchestrator {
    setOnLoginHelpResult { block(it) }
    return this
}

fun AuthOrchestrator.onSecondFactorResult(block: (result: Boolean) -> Unit): AuthOrchestrator {
    setOnSecondFactorResult { block(it) }
    return this
}

fun AuthOrchestrator.onTwoPassModeResult(block: (result: Boolean) -> Unit): AuthOrchestrator {
    setOnTwoPassModeResult { block(it) }
    return this
}

fun AuthOrchestrator.onSignUpResult(block: (result: SignupOutput?) -> Unit): AuthOrchestrator {
    setOnSignUpResult { block(it) }
    return this
}

fun AuthOrchestrator.onSecurityKeysResult(block: (result: Boolean) -> Unit): AuthOrchestrator {
    setOnSecurityKeysResult { block(it) }
    return this
}
