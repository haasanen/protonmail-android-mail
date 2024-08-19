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
import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.auth.presentation.AuthOrchestrator
import me.proton.android.core.auth.presentation.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.protectScreen
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : ProtonActivity() {

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    private val configuration = ProtectScreenConfiguration(true)
    private val screenProtector by protectScreen(configuration)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { onClose() }

        authOrchestrator.register(this)

        setContent {
            ProtonTheme {
                LoginScreen(
                    onCloseClicked = { finish() },
                    onHelpClicked = { authOrchestrator.startLoginHelpWorkflow() },
                    onErrorMessage = { showError(it) },
                    onLoggedIn = { onSuccess() }
                )
            }
        }
    }

    private fun showError(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun onSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
