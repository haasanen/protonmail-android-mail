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

package me.proton.android.core.auth.presentation.signup

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

object SignUpRoutes {

    object Route {
        object SignUp {

            const val Deeplink: String = "auth/signup"
            fun get(): String = "auth/signup"
        }

        object CreateUsername {

            const val Deeplink: String = "auth/signup/create/username"
            fun get(): String = "auth/signup/create/username"
        }

        object CreatePassword {

            const val Deeplink: String = "auth/signup/create/password"
            fun get(): String = "auth/signup/create/password"
        }

        object CreateRecovery {

            const val Deeplink: String = "auth/signup/create/recovery"
            fun get(): String = "auth/signup/create/recovery"
        }
    }

    fun NavGraphBuilder.addMainScreen(
        onErrorMessage: (String?) -> Unit,
        onSuccess: () -> Unit // todo: add user id
    ) {
        composable(
            route = Route.SignUp.Deeplink,
        ) {
            SignUpScreen(
                onSuccess = { onSuccess() },
                onErrorMessage = { onErrorMessage(it) },
            )
        }
    }

    fun NavGraphBuilder.addCreateUsernameScreen(
        navController: NavHostController,
        onClose: () -> Unit,
        onErrorMessage: (String?) -> Unit
    ) {
        composable(
            route = Route.CreateUsername.Deeplink,
        ) {
            CreateUsernameScreen(
                onCloseClicked = { onClose() },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = {
                    val password = it
                    navController.navigate(Route.CreatePassword.get())
                }
            )
        }
    }

    fun NavGraphBuilder.addCreatePasswordScreen(
        navController: NavHostController,
        onClose: () -> Unit,
        onErrorMessage: (String?) -> Unit
    ) {
        composable(
            route = Route.CreatePassword.Deeplink,
        ) {
            CreatePasswordScreen(
                onBackClicked = { navController.popBackStack() },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = {
                    val password = it
                    navController.navigate(Route.CreateRecovery.get())
                }
            )
        }
    }

    fun NavGraphBuilder.addCreateRecoveryScreen(
        navController: NavHostController,
        onClose: () -> Unit,
        onErrorMessage: (String?) -> Unit
    ) {
        composable(
            route = Route.CreateRecovery.Deeplink,
        ) {
            CreateRecoveryScreen(
                onBackClicked = { onClose() },
                onSkipClicked = { onClose() },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = { method, value ->
                    val recovery = it // add to viewmodel
                    navController.navigate(Route.SignUp.get())
                }
            )
        }
    }
}