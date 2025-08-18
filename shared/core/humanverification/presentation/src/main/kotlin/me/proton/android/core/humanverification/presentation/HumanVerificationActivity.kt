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

package me.proton.android.core.humanverification.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback

@AndroidEntryPoint
class HumanVerificationActivity : ProtonActivity() {

    private val input: HumanVerificationInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { onClose() }

        setContent {
            ProtonTheme {
                HumanVerificationScreen(
                    onCancel = { onClose() },
                    onHelpClicked = { onHumanVerificationHelp() },
                    onSuccess = { onSuccess() },
                    url = input.buildUrl(),
                    originalHost = input.originalHost,
                    alternativeHost = input.alternativeHost,
                    defaultCountry = input.defaultCountry,
                    recoveryPhone = input.recoveryPhone,
                    locale = resources.configuration.locales.get(0).language,
                    headers = input.extraHeaders
                )
            }
        }
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onHumanVerificationHelp() {
        val intent = Intent(this, HumanVerificationHelpActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun onSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object {

        const val ARG_INPUT = "arg.hvInput"
        const val ARG_RESULT = "arg.hvResult"
    }
}
