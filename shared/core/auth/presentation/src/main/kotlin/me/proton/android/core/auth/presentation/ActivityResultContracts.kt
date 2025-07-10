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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import me.proton.android.core.auth.presentation.addaccount.AddAccountActivity
import me.proton.android.core.auth.presentation.login.LoginActivity
import me.proton.android.core.auth.presentation.login.LoginHelpActivity
import me.proton.android.core.auth.presentation.login.LoginHelpOutput
import me.proton.android.core.auth.presentation.login.LoginInput
import me.proton.android.core.auth.presentation.login.LoginOutput
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorActivity
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorArg
import me.proton.android.core.auth.presentation.signup.SignUpActivity
import me.proton.android.core.auth.presentation.signup.SignupOutput
import me.proton.android.core.auth.presentation.twopass.TwoPassActivity
import me.proton.android.core.auth.presentation.twopass.TwoPassArg

object StartAddAccount : ActivityResultContract<Unit, Boolean>() {

    override fun createIntent(context: Context, input: Unit) = Intent(context, AddAccountActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean = when (resultCode) {
        Activity.RESULT_OK -> true
        else -> false
    }
}

object StartLogin : ActivityResultContract<LoginInput, LoginOutput?>() {

    override fun createIntent(context: Context, input: LoginInput) = Intent(context, LoginActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra(LoginActivity.ARG_INPUT, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): LoginOutput? = when (resultCode) {
        Activity.RESULT_OK -> intent?.let {
            IntentCompat.getParcelableExtra(it, LoginActivity.ARG_OUTPUT, LoginOutput::class.java)
        }

        else -> null
    }

}

object StartLoginHelp : ActivityResultContract<Unit, LoginHelpOutput?>() {

    override fun createIntent(context: Context, input: Unit) = Intent(context, LoginHelpActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): LoginHelpOutput? = when (resultCode) {
        Activity.RESULT_OK -> intent?.let {
            IntentCompat.getParcelableExtra(intent, LoginHelpActivity.ARG_OUTPUT, LoginHelpOutput::class.java)
        }

        else -> null
    }
}

object StartSecondFactor : ActivityResultContract<String, Boolean>() {

    override fun createIntent(context: Context, input: String): Intent =
        Intent(context, SecondFactorActivity::class.java).apply {
            putExtra(SecondFactorArg.ARG_USER_ID, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean = when (resultCode) {
        Activity.RESULT_OK -> true
        else -> false
    }
}

object StartTwoPassMode : ActivityResultContract<String, Boolean>() {

    override fun createIntent(context: Context, input: String): Intent =
        Intent(context, TwoPassActivity::class.java).apply {
            putExtra(TwoPassArg.ARG_USER_ID, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean = when (resultCode) {
        Activity.RESULT_OK -> true
        else -> false
    }
}

object StartSignUp : ActivityResultContract<Unit, SignupOutput?>() {

    override fun createIntent(context: Context, input: Unit) = Intent(context, SignUpActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): SignupOutput? = when (resultCode) {
        Activity.RESULT_OK -> intent?.let {
            IntentCompat.getParcelableExtra(it, SignUpActivity.ARG_OUTPUT, SignupOutput::class.java)
        }

        else -> null
    }
}
