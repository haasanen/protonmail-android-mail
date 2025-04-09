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

package me.proton.android.core.accountmanager.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountmanager.presentation.entity.PasswordManagementResult
import me.proton.android.core.accountmanager.presentation.entity.SettingsInput
import javax.inject.Inject

class AccountManagerOrchestrator @Inject constructor() {

    private var passwordManagementLauncher: ActivityResultLauncher<SettingsInput>? = null

    private var onPasswordManagementResultListener: ((result: PasswordManagementResult?) -> Unit)? = {}

    fun setPasswordManagementResult(block: (result: PasswordManagementResult?) -> Unit) {
        onPasswordManagementResultListener = block
    }

    private fun registerPasswordManagementResult(caller: ActivityResultCaller): ActivityResultLauncher<SettingsInput> =
        caller.registerForActivityResult(
            StartPasswordManagement()
        ) {
            onPasswordManagementResultListener?.invoke(it)
        }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        passwordManagementLauncher = registerPasswordManagementResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        passwordManagementLauncher?.unregister()
        passwordManagementLauncher = null
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call settingsOrchestrator.register(context) before starting workflow!" }

    /**
     * Starts the Password Management workflow (part of the User Settings).
     *
     * @see [onPasswordManagementResult]
     */
    fun startPasswordManagementWorkflow(userId: CoreUserId) {
        checkRegistered(passwordManagementLauncher).launch(
            SettingsInput(userId.id)
        )
    }
}

fun AccountManagerOrchestrator.onPasswordManagementResult(
    block: (result: PasswordManagementResult?) -> Unit
): AccountManagerOrchestrator {
    setPasswordManagementResult { block(it) }
    return this
}
