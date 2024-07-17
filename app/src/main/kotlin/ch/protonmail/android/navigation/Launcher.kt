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

package ch.protonmail.android.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ch.protonmail.android.MainActivity
import ch.protonmail.android.navigation.model.LauncherState
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.domain.entity.UserId

@Composable
fun Launcher(activityActions: MainActivity.Actions, viewModel: BaseLauncherViewModel) {
    val state by viewModel.state.collectAsState(LauncherState.Processing)

    when (state) {
        LauncherState.AccountNeeded -> viewModel.submit(BaseLauncherViewModel.Action.AddAccount)
        LauncherState.PrimaryExist -> Home(
            activityActions = activityActions,
            launcherActions = Launcher.Actions(
                onPasswordManagement = { viewModel.submit(BaseLauncherViewModel.Action.OpenPasswordManagement) },
                onRecoveryEmail = { viewModel.submit(BaseLauncherViewModel.Action.OpenRecoveryEmail) },
                onReportBug = { viewModel.submit(BaseLauncherViewModel.Action.OpenReport) },
                onSignIn = { viewModel.submit(BaseLauncherViewModel.Action.SignIn(it)) },
                onSubscription = { viewModel.submit(BaseLauncherViewModel.Action.OpenSubscription) },
                onSwitchAccount = { viewModel.submit(BaseLauncherViewModel.Action.Switch(it)) }
            )
        )
        LauncherState.Processing,
        LauncherState.StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
    }
}

object Launcher {

    /**
     * A set of actions that can be executed in the scope of Core's Orchestrators
     */
    data class Actions(
        val onSignIn: (UserId?) -> Unit,
        val onSwitchAccount: (UserId) -> Unit,
        val onSubscription: () -> Unit,
        val onReportBug: () -> Unit,
        val onPasswordManagement: () -> Unit,
        val onRecoveryEmail: () -> Unit
    )
}
