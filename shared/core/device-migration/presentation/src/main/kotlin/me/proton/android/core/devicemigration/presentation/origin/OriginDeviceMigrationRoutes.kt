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

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity.RESULT_OK
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.proton.android.core.devicemigration.presentation.origin.codeinput.ManualCodeInputScreen
import me.proton.android.core.devicemigration.presentation.origin.intro.OriginQrSignInScreen
import me.proton.android.core.devicemigration.presentation.origin.success.OriginSuccessScreen
import me.proton.core.compose.util.LaunchOnScreenView

internal object OriginDeviceMigrationRoutes {
    object Route {
        object SignInIntro {

            const val DEEPLINK: String = "device_migration/origin/intro"
            fun get(): String = DEEPLINK
        }

        object ManualCodeInput {

            const val DEEPLINK: String = "device_migration/origin/code_input"
            fun get(): String = DEEPLINK
        }

        object OriginSuccess {

            const val DEEPLINK: String = "device_migration/origin/success"
            fun get(): String = DEEPLINK
        }
    }

    fun NavGraphBuilder.addSignInIntroScreen(
        navigateToAppSettings: () -> Unit = {},
        onManualCodeInput: () -> Unit = {},
        onNavigateBack: () -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        composable(route = Route.SignInIntro.DEEPLINK) {
            LaunchOnScreenView {
                // Observability: EdmScreenViewTotal.ScreenId.origin_intro
            }
            OriginQrSignInScreen(
                navigateToAppSettings = navigateToAppSettings,
                onManualCodeInput = onManualCodeInput,
                onNavigateBack = onNavigateBack,
                onSuccess = onSuccess
            )
        }
    }

    fun NavGraphBuilder.addManualCodeInputScreen(onNavigateBack: () -> Unit = {}, onSuccess: () -> Unit = {}) {
        composable(
            route = Route.ManualCodeInput.DEEPLINK,
            enterTransition = { EnterTransition.None }
        ) {
            LaunchOnScreenView {
                // Observability: EdmScreenViewTotal.ScreenId.origin_manual_code_input
            }
            ManualCodeInputScreen(
                onNavigateBack = onNavigateBack,
                onSuccess = onSuccess
            )
        }
    }

    fun NavGraphBuilder.addOriginSuccessScreen(onClose: () -> Unit) {
        composable(route = Route.OriginSuccess.DEEPLINK) {
            val activity = LocalActivity.current

            LaunchedEffect(activity) {
                activity?.setResult(RESULT_OK)
            }

            LaunchOnScreenView {
                // Observability: EdmScreenViewTotal.ScreenId.origin_success
            }

            OriginSuccessScreen(
                onClose = onClose
            )
        }
    }
}
