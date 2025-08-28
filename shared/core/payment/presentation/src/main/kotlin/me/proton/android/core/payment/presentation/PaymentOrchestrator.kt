/*
 * Copyright (C) 2025 Proton AG
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

package me.proton.android.core.payment.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.android.core.payment.presentation.model.SubscriptionFlowLaunchOptions
import javax.inject.Inject

class PaymentOrchestrator @Inject constructor() {

    private var subscriptionWorkflowLauncher: ActivityResultLauncher<SubscriptionFlowLaunchOptions>? = null

    private var onUpgradeResultListener: ((result: Boolean) -> Unit)? = {}

    private fun registerUpgradeResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<SubscriptionFlowLaunchOptions> = caller.registerForActivityResult(StartSubscription) {
        onUpgradeResultListener?.invoke(it)
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call register(context) before starting workflow!" }

    fun setOnUpgradeResult(block: (result: Boolean) -> Unit) {
        onUpgradeResultListener = block
    }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        subscriptionWorkflowLauncher = registerUpgradeResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        subscriptionWorkflowLauncher?.unregister()
        subscriptionWorkflowLauncher = null
        onUpgradeResultListener = null
    }

    /**
     * Start the Subscription Workflow.
     */
    fun startSubscriptionWorkflow(isAllowedToUpgrade: Boolean) {
        checkRegistered(subscriptionWorkflowLauncher).launch(
            SubscriptionFlowLaunchOptions(isAllowedToUpgrade)
        )
    }
}

fun PaymentOrchestrator.onUpgradeResult(block: (result: Boolean) -> Unit): PaymentOrchestrator {
    setOnUpgradeResult { block(it) }
    return this
}
