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

package ch.protonmail.android.mailpinlock.presentation.autolock.ui

import java.util.concurrent.Executor
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ch.protonmail.android.mailpinlock.presentation.autolock.BiometricPromptCallback

@SuppressLint("MissingPermission")
class BiometricAuthenticator(
    private val activity: FragmentActivity,
    private val executor: Executor,
    private val promptInfo: BiometricPrompt.PromptInfo
) {
    fun authenticate(callback: BiometricPromptCallback) {
        val authCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                callback.onAuthenticationSucceeded(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                callback.onAuthenticationFailed()
            }
        }

        val prompt = BiometricPrompt(activity, executor, authCallback)
        prompt.authenticate(promptInfo)
    }

    fun authenticate(
        onAuthenticationError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onAuthenticationSucceeded: (result: BiometricPrompt.AuthenticationResult) -> Unit = {},
        onAuthenticationFailed: () -> Unit = {}
    ) {
        authenticate(
            BiometricPromptCallback(
                onAuthenticationError,
                onAuthenticationSucceeded,
                onAuthenticationFailed
            )
        )
    }
}

@Composable
@SuppressLint("MissingPermission")
fun rememberBiometricAuthenticator(
    title: String,
    subtitle: String,
    negativeButtonText: String
): BiometricAuthenticator {
    val activity = LocalActivity.current as FragmentActivity
    val executor = remember { ContextCompat.getMainExecutor(activity) }

    val promptInfo = remember(title, subtitle, negativeButtonText) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                } else {
                    setNegativeButtonText(negativeButtonText)
                }
            }
            .build()
    }

    return BiometricAuthenticator(activity, executor, promptInfo)
}
