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

package me.proton.android.core.auth.presentation.addaccount

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.auth.presentation.AuthOrchestrator
import me.proton.android.core.auth.presentation.onLoginResult
import me.proton.android.core.auth.presentation.onSignUpResult
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountActivity : ProtonActivity() {

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    override fun onDestroy() {
        authOrchestrator.unregister()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { onClose() }

        authOrchestrator.register(this)
        authOrchestrator.onLoginResult { result -> if (result != null) onSuccess() }
        authOrchestrator.onSignUpResult { result -> if (result != null) onSuccess() }

        setContent {
            ProtonTheme {
                AddAccountScreenMail(
                    onSignInClicked = { authOrchestrator.startLoginWorkflow() },
                    onSignUpClicked = { authOrchestrator.startSignUpWorkflow() }
                )
            }
        }
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
