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

package me.proton.android.core.devicemigration.presentation.origin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.devicemigration.presentation.origin.OriginDeviceMigrationRoutes.Route
import me.proton.android.core.devicemigration.presentation.origin.OriginDeviceMigrationRoutes.addManualCodeInputScreen
import me.proton.android.core.devicemigration.presentation.origin.OriginDeviceMigrationRoutes.addOriginSuccessScreen
import me.proton.android.core.devicemigration.presentation.origin.OriginDeviceMigrationRoutes.addSignInIntroScreen
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.enableProtonEdgeToEdge
import me.proton.core.presentation.utils.openAppSettings

@AndroidEntryPoint
public class OriginDeviceMigrationActivity : ProtonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }

    @Composable
    private fun Content() = ProtonTheme {
        val navController = rememberNavController()
        NavHost(
            navController,
            startDestination = Route.SignInIntro.DEEPLINK,
            modifier = Modifier
                .background(LocalColors.current.backgroundNorm)
                .safeDrawingPadding()
        ) {
            addSignInIntroScreen(
                navigateToAppSettings = this@OriginDeviceMigrationActivity::openAppSettings,
                onManualCodeInput = {
                    navController.navigate(Route.ManualCodeInput.get()) {
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.backOrFinish() },
                onSuccess = {
                    navController.navigate(Route.OriginSuccess.get()) {
                        popUpTo(Route.SignInIntro.DEEPLINK) { inclusive = true }
                    }
                }
            )
            addManualCodeInputScreen(
                onNavigateBack = { navController.backOrFinish() },
                onSuccess = {
                    navController.navigate(Route.OriginSuccess.get()) {
                        popUpTo(Route.SignInIntro.DEEPLINK) { inclusive = true }
                    }
                }
            )
            addOriginSuccessScreen(
                onClose = { finish() }
            )
        }
    }

    private fun NavController.backOrFinish() {
        if (!popBackStack()) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
