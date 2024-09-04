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

package me.proton.android.core.auth.presentation.secondfactor

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.auth.presentation.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast

@AndroidEntryPoint
class SecondFactorActivity : ProtonActivity() {

    private val userId: String
        get() = requireNotNull(intent.getStringExtra(SecondFactorArg.ARG_USER_ID)) {
            "Missing intent argument: ${SecondFactorArg.ARG_USER_ID}"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOnBackPressedCallback { onClose() }
        setContent {
            ProtonTheme {
                SecondFactorInputScreen(
                    onBackClicked = this::onClose,
                    onError = this::onError,
                    onSuccess = this::onSuccess
                )
            }
        }
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onError(throwable: Throwable) {
        errorToast(throwable.localizedMessage ?: getString(R.string.presentation_error_general))
    }

    private fun onSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
