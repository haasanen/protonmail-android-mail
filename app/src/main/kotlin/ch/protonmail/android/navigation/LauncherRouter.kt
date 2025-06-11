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

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.MainActivity
import ch.protonmail.android.mailpinlock.presentation.autolock.ui.LockScreenInterstitial

@Composable
internal fun LauncherRouter(
    activityActions: MainActivity.Actions,
    launcherActions: Launcher.Actions,
    viewModel: LauncherRouterViewModel = hiltViewModel()
) {
    val showAutoLock by viewModel.displayAutoLockState.collectAsStateWithLifecycle()

    if (showAutoLock) {
        LockScreenInterstitial(
            onClose = { activityActions.finishActivity() },
            onNavigateToPinInsertion = {
                activityActions.onNavigateToPinInsertion()
            }
        )
    }

    Home(
        modifier = Modifier.then(
            if (showAutoLock) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(radius = 16.dp)
                } else {
                    Modifier.background(Color.Black.copy(alpha = 0.7f))
                }
            } else {
                Modifier
            }
        ),
        activityActions,
        launcherActions
    )
}
