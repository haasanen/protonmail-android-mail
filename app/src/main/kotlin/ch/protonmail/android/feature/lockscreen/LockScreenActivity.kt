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

package ch.protonmail.android.feature.lockscreen

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailpinlock.presentation.autolock.standalone.LocalLockScreenEntryPointIsStandalone
import ch.protonmail.android.navigation.model.Destination
import ch.protonmail.android.navigation.route.addAutoLockOverlay
import ch.protonmail.android.navigation.route.addAutoLockPinScreen
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.compose.withSentryObservableEffect

@AndroidEntryPoint
internal class LockScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Blur is only added on API 31+ devices.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setupNativeBlur()
        }

        setContent {
            ProtonTheme {
                val navController = rememberNavController().withSentryObservableEffect()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            // If < API 31, show a background to hide the underlying content as there is no blur.
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                Modifier.background(ProtonTheme.colors.backgroundNorm)
                            } else {
                                Modifier
                            }
                        )
                ) {

                    CompositionLocalProvider(LocalLockScreenEntryPointIsStandalone provides true) {
                        NavHost(
                            navController = navController,
                            startDestination = Destination.Screen.AutoLockOverlay.route
                        ) {
                            addAutoLockOverlay(
                                onClose = { this@LockScreenActivity.finish() },
                                navController
                            )

                            addAutoLockPinScreen(
                                onClose = { this@LockScreenActivity.finish() },
                                onShowSuccessSnackbar = {}
                            )
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupNativeBlur() {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            )

            @Suppress("MagicNumber")
            setDimAmount(0.1f)
        }
    }
}
