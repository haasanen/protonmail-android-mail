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

package me.proton.android.core.devicemigration.presentation.origin.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.devicemigration.presentation.OriginDeviceMigrationOutput
import me.proton.android.core.devicemigration.presentation.R
import me.proton.android.core.devicemigration.presentation.StartDeviceMigrationFromOrigin
import me.proton.core.compose.activity.rememberLauncher
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.viewmodel.hiltViewModelOrNull

/**
 * @param content The composable content that will be displayed, if Easy Device Migration is available for the user.
 * @param onResult The result of the QR code login.
 */
@Composable
public fun SignInOnTargetDeviceItem(
    content: SignInOnTargetDeviceContent,
    onResult: (OriginDeviceMigrationOutput?) -> Unit = {},
    viewModel: SignInOnTargetDeviceViewModel? = hiltViewModelOrNull()
) {
    val state by viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { derivedStateOf { SignInOnTargetDeviceState.Visible(isEnabled = false) } }
    SignInOnTargetDeviceItem(
        content = content,
        onResult = onResult,
        state = state
    )
}

@Composable
public fun SignInOnTargetDeviceItem(
    content: SignInOnTargetDeviceContent,
    onResult: (OriginDeviceMigrationOutput?) -> Unit = {},
    state: SignInOnTargetDeviceState
) {
    when (state) {
        is SignInOnTargetDeviceState.Hidden -> Unit
        is SignInOnTargetDeviceState.Visible -> {
            val launcher = rememberLauncher(StartDeviceMigrationFromOrigin(), onResult = onResult)
            content(
                label = stringResource(R.string.intro_origin_sign_in_title),
                onClick = {
                    if (state.isEnabled) launcher.launch(Unit)
                }
            )
        }
    }
}

public fun interface SignInOnTargetDeviceContent {

    @Composable
    public operator fun invoke(label: String, onClick: () -> Unit)
}

@Composable
@Preview(showBackground = true)
private fun SignInToAnotherDeviceItemPreview() {
    ProtonTheme {
        SignInOnTargetDeviceItem(
            content = { label, onClick -> ProtonSettingsItem(name = label, onClick = onClick) }
        )
    }
}
