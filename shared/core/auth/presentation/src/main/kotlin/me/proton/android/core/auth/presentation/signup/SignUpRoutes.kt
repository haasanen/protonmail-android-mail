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

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import me.proton.android.core.auth.presentation.signup.ui.CountryPickerScreen
import me.proton.android.core.auth.presentation.signup.ui.CreatePasswordScreen
import me.proton.android.core.auth.presentation.signup.ui.CreateRecoveryScreen
import me.proton.android.core.auth.presentation.signup.ui.CreateRecoverySkipDialog
import me.proton.android.core.auth.presentation.signup.ui.CreateUsernameScreen
import me.proton.android.core.auth.presentation.signup.ui.SignUpCongratsScreen
import me.proton.android.core.auth.presentation.signup.ui.SignUpLoadingScreen
import me.proton.android.core.auth.presentation.signup.viewmodel.SignUpViewModel

object SignUpRoutes {
    sealed class Route(val route: String) {
        data object CreateUsername : Route("auth/signup/create/username") {

            operator fun invoke() = route
        }

        data object CreatePassword : Route("auth/signup/create/password") {

            operator fun invoke() = route
        }

        data object CreateRecovery : Route("auth/signup/create/recovery") {

            operator fun invoke() = route
        }

        data object CountryPicker : Route("auth/signup/create/recovery/countrypicker") {

            operator fun invoke() = route
        }

        data object SkipRecovery : Route("auth/signup/create/recovery/skip") {

            operator fun invoke() = route
        }

        data object SignUpCreateUser : Route("auth/signup/user/create") {

            operator fun invoke() = route
        }

        data object SignUpCongrats : Route("auth/signup/congrats") {

            operator fun invoke() = route
        }
    }

    fun NavGraphBuilder.addCreateUsernameScreen(
        navController: NavHostController,
        onClose: () -> Unit,
        onErrorMessage: (String?) -> Unit,
        navGraphViewModel: SignUpViewModel
    ) {
        composable(
            route = Route.CreateUsername.route
        ) {
            CreateUsernameScreen(
                onBackClicked = { onClose() },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                viewModel = navGraphViewModel
            )
        }
    }

    fun NavGraphBuilder.addCreatePasswordScreen(
        navController: NavHostController,
        onErrorMessage: (String?) -> Unit,
        navGraphViewModel: SignUpViewModel
    ) {
        composable(
            route = Route.CreatePassword.route
        ) {
            CreatePasswordScreen(
                onBackClicked = {
                    navController.popBackStack()
                },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                viewModel = navGraphViewModel
            )
        }
    }

    fun NavGraphBuilder.addCreateRecoveryScreen(
        navController: NavHostController,
        onErrorMessage: (String?) -> Unit,
        navGraphViewModel: SignUpViewModel
    ) {
        composable(
            route = Route.CreateRecovery.route
        ) {
            CreateRecoveryScreen(
                onBackClicked = { navController.popBackStack() },
                onWantSkip = {
                    navController.navigate(Route.SkipRecovery.route) {
                        launchSingleTop = true
                    }
                },
                onCountryPickerClicked = {
                    navController.navigate(Route.CountryPicker.route) {
                        launchSingleTop = true
                    }
                },
                onErrorMessage = { onErrorMessage(it) },
                onSuccess = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                viewModel = navGraphViewModel
            )
        }
    }

    fun NavGraphBuilder.addCountryPickerDialog(navController: NavHostController, navGraphViewModel: SignUpViewModel) {
        dialog(
            route = Route.CountryPicker.route,
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            CountryPickerScreen(
                viewModel = navGraphViewModel,
                onCloseClick = { navController.popBackStack() }
            )
        }
    }

    fun NavGraphBuilder.addSkipRecoveryDialog(navController: NavHostController, navGraphViewModel: SignUpViewModel) {
        dialog(route = Route.SkipRecovery.route) {
            CreateRecoverySkipDialog(
                onCloseClicked = { navController.popBackStack() },
                onSkip = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = navGraphViewModel
            )
        }
    }

    fun NavGraphBuilder.addSignUpCreateUserScreen(
        navController: NavHostController,
        onErrorMessage: (String?) -> Unit,
        navGraphViewModel: SignUpViewModel
    ) {
        composable(
            route = Route.SignUpCreateUser.route
        ) {
            SignUpLoadingScreen(
                onErrorMessage = {
                    onErrorMessage(it)
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.navigate(Route.SignUpCongrats.route) {
                        launchSingleTop = true
                    }
                },
                viewModel = navGraphViewModel
            )
        }
    }

    fun NavGraphBuilder.addSignUpCongratsScreen(
        onSuccess: (String) -> Unit,
        onError: (String?) -> Unit,
        navGraphViewModel: SignUpViewModel
    ) {
        composable(
            route = Route.SignUpCongrats.route
        ) {
            SignUpCongratsScreen(
                onStartUsingApp = { onSuccess(it) },
                onErrorMessage = {
                    onError(it)
                },
                viewModel = navGraphViewModel
            )
        }
    }
}
