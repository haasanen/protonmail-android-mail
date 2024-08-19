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
import javax.inject.Inject

class AuthOrchestrator @Inject constructor() {

    private var addAccountWorkflowLauncher: ActivityResultLauncher<Unit>? = null
    private var loginWorkflowLauncher: ActivityResultLauncher<Unit>? = null
    private var loginHelpWorkflowLauncher: ActivityResultLauncher<Unit>? = null

    private var onAddAccountResultListener: ((result: Boolean) -> Unit)? = {}
    private var onLoginResultListener: ((result: Boolean) -> Unit)? = {}
    private var onLoginHelpResultListener: ((result: Boolean) -> Unit)? = {}

    private fun registerAddAccountResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<Unit> = caller.registerForActivityResult(StartAddAccount) {
        onAddAccountResultListener?.invoke(it)
    }

    private fun registerLoginResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<Unit> = caller.registerForActivityResult(StartLogin) {
        onLoginResultListener?.invoke(it)
    }

    private fun registerLoginHelpResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<Unit> = caller.registerForActivityResult(StartLoginHelp) {
        onLoginHelpResultListener?.invoke(it)
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call register(context) before starting workflow!" }

    fun setOnAddAccountResult(block: (result: Boolean) -> Unit) {
        onAddAccountResultListener = block
    }

    fun setOnLoginResult(block: (result: Boolean) -> Unit) {
        onLoginResultListener = block
    }

    fun setOnLoginHelpResult(block: (result: Boolean) -> Unit) {
        onLoginHelpResultListener = block
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
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        addAccountWorkflowLauncher?.unregister()
        loginWorkflowLauncher?.unregister()
        loginHelpWorkflowLauncher?.unregister()

        addAccountWorkflowLauncher = null
        loginWorkflowLauncher = null
        loginHelpWorkflowLauncher = null

        onAddAccountResultListener = null
        onLoginResultListener = null
        onLoginHelpResultListener = null
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
    fun startLoginWorkflow() {
        checkRegistered(loginWorkflowLauncher).launch(Unit)
    }

    /**
     * Starts the Login Help workflow.
     */
    fun startLoginHelpWorkflow() {
        checkRegistered(loginHelpWorkflowLauncher).launch(Unit)
    }
}

fun AuthOrchestrator.onAddAccountResult(
    block: (result: Boolean) -> Unit
): AuthOrchestrator {
    setOnAddAccountResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginResult(
    block: (result: Boolean) -> Unit
): AuthOrchestrator {
    setOnLoginResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginHelpResult(
    block: (result: Boolean) -> Unit
): AuthOrchestrator {
    setOnLoginHelpResult { block(it) }
    return this
}
