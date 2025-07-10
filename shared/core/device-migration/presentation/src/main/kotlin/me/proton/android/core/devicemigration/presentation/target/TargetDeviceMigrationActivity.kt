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

package me.proton.android.core.devicemigration.presentation.target

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.devicemigration.presentation.TargetDeviceMigrationResult
import me.proton.android.core.devicemigration.presentation.target.TargetDeviceMigrationRoutes.addSignInScreen
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.enableProtonEdgeToEdge

@AndroidEntryPoint
public class TargetDeviceMigrationActivity : ProtonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }

    @Composable
    private fun Content(navController: NavHostController = rememberNavController()) = ProtonTheme {
        NavHost(
            navController,
            startDestination = TargetDeviceMigrationRoutes.Route.SignIn.Deeplink,
            modifier = Modifier.safeDrawingPadding()
        ) {
            addSignInScreen(
                onBackToSignIn = {
                    setResult(
                        RESULT_CANCELED,
                        Intent().apply {
                            putExtra(ARG_RESULT, TargetDeviceMigrationResult.NavigateToSignIn)
                        }
                    )
                    finish()
                },
                onNavigateBack = { finish() },
                onSuccess = { userId: CoreUserId -> onSuccess(userId) }
            )
        }
    }

    private fun onSuccess(userId: CoreUserId) {
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(ARG_RESULT, TargetDeviceMigrationResult.SignedIn(userId.id))
            }
        )
        finish()
    }

    internal companion object {

        const val ARG_RESULT = "result"
    }
}
