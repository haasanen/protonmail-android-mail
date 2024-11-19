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
    sealed class Route(val route: String) {
        data object SignUp : Route("auth/signup") {

            operator fun invoke() = route
        }

        data object CreateUsername : Route("auth/signup/create/username") {

            operator fun invoke() = route
        }

        data object CreatePassword : Route("auth/signup/create/password") {

            operator fun invoke() = route
        }

        data object CreateRecovery : Route("auth/signup/create/recovery") {

            operator fun invoke() = route
        }
    }

    @Suppress("ForbiddenComment")
    fun NavGraphBuilder.addMainScreen(
        onErrorMessage: (String?) -> Unit,
        onSuccess: () -> Unit // todo: add user id
    ) {
        composable(
            route = Route.SignUp.route
        ) {
            SignUpScreen(
                onSuccess = { onSuccess() },
                onErrorMessage = { onErrorMessage(it) }
            )
        }
    }

    fun NavGraphBuilder.addCreateUsernameScreen(
        navController: NavHostController,
        onClose: () -> Unit,
        onErrorMessage: (String?) -> Unit
    ) {
        composable(
            route = Route.CreateUsername.route
        ) {
            CreateUsernameScreen(
                onCloseClicked = { onClose() },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = {
                    val password = it
                    navController.navigate(Route.CreatePassword())
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
            route = Route.CreatePassword.route
        ) {
            CreatePasswordScreen(
                onBackClicked = { navController.popBackStack() },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = {
                    val password = it
                    navController.navigate(Route.CreateRecovery())
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
            route = Route.CreateRecovery.route
        ) {
            CreateRecoveryScreen(
                onBackClicked = { onClose() },
                onSkipClicked = { onClose() },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = { method, value ->
                    val recovery = it // add to viewmodel
                    navController.navigate(Route.SignUp())
                }
            )
        }
    }
}
