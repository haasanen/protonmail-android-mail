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

package me.proton.android.core.auth.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.content.IntentCompat
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.auth.presentation.AuthOrchestrator
import me.proton.android.core.auth.presentation.ProtonSecureActivity
import me.proton.android.core.auth.presentation.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : ProtonSecureActivity() {

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    private val input: LoginInput
        get() = requireNotNull(IntentCompat.getParcelableExtra(intent, ARG_INPUT, LoginInput::class.java))

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
                LoginScreen(
                    initialUsername = input.username,
                    onCloseClicked = { finish() },
                    onHelpClicked = { authOrchestrator.startLoginHelpWorkflow() },
                    onErrorMessage = { showError(it) },
                    onSuccess = { onSuccess(it) }
                )
            }
        }
    }

    private fun showError(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun onSuccess(userId: String) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ARG_OUTPUT, LoginOutput(userId = userId)) }
        )
        finish()
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    companion object {

        const val ARG_INPUT = "arg.loginInput"
        const val ARG_OUTPUT = "arg.loginOutput"
    }
}
