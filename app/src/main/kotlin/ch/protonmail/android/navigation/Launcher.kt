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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.MainActivity
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.navigation.model.LauncherState
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.R
import ch.protonmail.android.design.compose.theme.ProtonColors
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.bodyLargeNorm

@Composable
fun Launcher(activityActions: MainActivity.Actions, viewModel: LauncherViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(LauncherState.Processing)

    when (state) {
        LauncherState.AccountNeeded -> viewModel.submit(LauncherViewModel.Action.AddAccount)
        LauncherState.PrimaryExist -> Home(
            activityActions = activityActions,
            launcherActions = Launcher.Actions(
                onPasswordManagement = { viewModel.submit(LauncherViewModel.Action.OpenPasswordManagement) },
                onRecoveryEmail = { viewModel.submit(LauncherViewModel.Action.OpenRecoveryEmail) },
                onReportBug = { viewModel.submit(LauncherViewModel.Action.OpenReport) },
                onSignIn = { viewModel.submit(LauncherViewModel.Action.SignIn(it)) },
                onSubscription = { viewModel.submit(LauncherViewModel.Action.OpenSubscription) },
                onSwitchToAccount = { viewModel.submit(LauncherViewModel.Action.SwitchToAccount(it)) },
                onRequestNotificationPermission = {
                    viewModel.submit(LauncherViewModel.Action.RequestNotificationPermission)
                }
            )
        )

        LauncherState.Processing,
        LauncherState.StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())

        LauncherState.MigrationInProgress,
        LauncherState.ProcessingAfterMigration -> MigrationLoadingScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
fun MigrationLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ProtonColors.Light.shade10),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.padding(ProtonDimens.Spacing.MediumLight)) {
                CircularProgressIndicator(
                    strokeWidth = ProtonDimens.BorderSize.Large,
                    color = ProtonTheme.colors.brandNorm,
                    modifier = Modifier
                        .size(ProtonDimens.IconSize.MediumLarge)
                )
            }

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Huge))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Jumbo)
            ) {
                Text(
                    text = stringResource(R.string.loading_mailbox),
                    style = ProtonTheme.typography.bodyLargeNorm,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))
                Text(
                    text = stringResource(R.string.loading_mailbox_subtitle),
                    style = ProtonTheme.typography.bodyLargeNorm,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

object Launcher {

    data class Actions(
        val onSignIn: (UserId?) -> Unit,
        val onSubscription: () -> Unit,
        val onReportBug: () -> Unit,
        val onPasswordManagement: () -> Unit,
        val onRecoveryEmail: () -> Unit,
        val onSwitchToAccount: (UserId) -> Unit,
        val onRequestNotificationPermission: () -> Unit
    )
}
