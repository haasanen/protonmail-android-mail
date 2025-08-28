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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.proton.android.core.payment.presentation.model.SubscriptionFlowLaunchOptions

object StartSubscription : ActivityResultContract<SubscriptionFlowLaunchOptions, Boolean>() {

    const val UPSELLING_ENABLED_EXTRA_KEY = "is_upselling_enabled"

    override fun createIntent(context: Context, input: SubscriptionFlowLaunchOptions) =
        Intent(context, SubscriptionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(UPSELLING_ENABLED_EXTRA_KEY, input.isAllowedToUpgrade)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean = when (resultCode) {
        Activity.RESULT_OK -> true
        else -> false
    }
}
