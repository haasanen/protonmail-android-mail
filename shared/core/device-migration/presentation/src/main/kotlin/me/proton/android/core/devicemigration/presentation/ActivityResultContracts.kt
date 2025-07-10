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

package me.proton.android.core.devicemigration.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import kotlinx.parcelize.Parcelize
import me.proton.android.core.devicemigration.presentation.origin.OriginDeviceMigrationActivity
import me.proton.android.core.devicemigration.presentation.target.TargetDeviceMigrationActivity

public class StartDeviceMigrationFromOrigin : ActivityResultContract<Unit, OriginDeviceMigrationOutput?>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(context, OriginDeviceMigrationActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): OriginDeviceMigrationOutput? {
        return when (resultCode) {
            Activity.RESULT_OK -> OriginDeviceMigrationOutput.Success
            Activity.RESULT_CANCELED -> OriginDeviceMigrationOutput.Cancelled
            else -> null
        }
    }
}

public sealed interface OriginDeviceMigrationOutput {
    public data object Success : OriginDeviceMigrationOutput
    public data object Cancelled : OriginDeviceMigrationOutput
}

public class StartMigrationFromTarget : ActivityResultContract<Unit, TargetDeviceMigrationResult?>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(context, TargetDeviceMigrationActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): TargetDeviceMigrationResult? =
        intent?.getParcelableExtra(TargetDeviceMigrationActivity.ARG_RESULT)
}

@Parcelize
public sealed interface TargetDeviceMigrationResult : Parcelable {

    /** It was not possible to sign in, and the user should be navigated to the sign-in screen. */
    public data object NavigateToSignIn : TargetDeviceMigrationResult

    /** User was signed in successfully. */
    public data class SignedIn(val userId: String) : TargetDeviceMigrationResult
}
