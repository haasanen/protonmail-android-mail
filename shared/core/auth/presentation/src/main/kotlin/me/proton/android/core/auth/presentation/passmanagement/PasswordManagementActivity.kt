/*
 * Copyright (c) 2025 Proton Technologies AG
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

package me.proton.android.core.auth.presentation.passmanagement

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.showToast

@AndroidEntryPoint
class PasswordManagementActivity : ProtonActivity() {

    private val passwordManagementViewModel: PasswordManagementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        passwordManagementViewModel.register(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                passwordManagementViewModel.uiEvent.collect { uiEvent ->
                    when (uiEvent) {
                        PasswordManagementEvent.LoginPasswordSaved -> onLoginPasswordSaved()
                        PasswordManagementEvent.MailboxPasswordSaved -> onMailboxPasswordSaved()
                    }
                }
            }
        }

        setContent {
            ProtonTheme {
                PasswordManagementScreen(
                    onClose = this::onClose,
                    onError = this::onError,
                    viewModel = passwordManagementViewModel
                )
            }
        }
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onError(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun onLoginPasswordSaved() {
        showToast(getString(R.string.settings_password_management_login_success))
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onMailboxPasswordSaved() {
        showToast(getString(R.string.settings_password_management_second_success))
        setResult(Activity.RESULT_OK)
        finish()
    }
}
