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
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ch.protonmail.android.mailsettings.domain.model.autolock.biometric.BiometricPromptCallback

@SuppressLint("MissingPermission")
data class BiometricAuthenticator(
    val prompt: BiometricPrompt,
    val executor: Executor,
    val callback: BiometricPrompt.AuthenticationCallback
) {

    fun authenticate() {
        prompt.authenticate(CancellationSignal(), executor, callback)
    }

    fun authenticate(callback: BiometricPromptCallback) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback.onAuthenticationError()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                callback.onAuthenticationSucceeded()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                callback.onAuthenticationFailed()
            }
        }

        prompt.authenticate(CancellationSignal(), executor, callback)
    }
}

@Composable
@SuppressLint("MissingPermission")
@Suppress("UseComposableActions")
fun rememberBiometricAuthenticator(
    title: String,
    subtitle: String,
    negativeButtonText: String,
    onAuthenticationError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
    onAuthenticationSucceeded: (result: BiometricPrompt.AuthenticationResult) -> Unit = { },
    onAuthenticationFailed: () -> Unit = { }
): BiometricAuthenticator {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val executor = ContextCompat.getMainExecutor(context)

    val callback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthenticationSucceeded(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthenticationFailed()
            }
        }
    }

    val biometricPrompt = remember {
        BiometricPrompt.Builder(activity)
            .setTitle(title)
            .setSubtitle(subtitle)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                } else {
                    setNegativeButton(negativeButtonText, executor) { _, _ -> }
                }
            }
            .build()
    }

    return remember {
        BiometricAuthenticator(biometricPrompt, executor, callback)
    }
}
