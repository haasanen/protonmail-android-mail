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

@file:Suppress("UseComposableActions")

package me.proton.android.core.auth.presentation.secondfactor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputForm
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun SecondFactorInputScreen(
    onClose: () -> Unit,
    onError: (String?) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SecondFactorInputViewModel = hiltViewModel(),
    externalAction: StateFlow<SecondFactorInputAction?> = MutableStateFlow(null)
) {
    val action by externalAction.collectAsStateWithLifecycle()
    action?.let { viewModel.submit(it) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    SecondFactorInputScreen(
        state = state,
        modifier = modifier,
        onClose = onClose,
        onError = onError,
        onSuccess = onSuccess,
        onBackClicked = { viewModel.submit(SecondFactorInputAction.Close) }
    )
}

@Composable
fun SecondFactorInputScreen(
    state: SecondFactorInputState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    LaunchedEffect(state) {
        when (state) {
            is SecondFactorInputState.Close -> onClose()
            is SecondFactorInputState.Idle -> Unit
        }
    }
    SecondFactorInputScaffold(
        modifier = modifier,
        onBackClicked = onBackClicked,
        onClose = onClose,
        onError = onError,
        onSuccess = onSuccess
    )
}

@Composable
fun SecondFactorInputScaffold(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painterResource(R.drawable.ic_proton_arrow_back),
                            contentDescription = stringResource(id = R.string.presentation_back)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
                Text(
                    style = LocalTypography.current.headline,
                    text = stringResource(R.string.auth_second_factor_title)
                )
                OneTimePasswordInputForm(
                    onError = onError,
                    onSuccess = onSuccess,
                    onClose = onClose,
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )
            }
        }
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
@Preview
fun SecondFactorInputScreenPreview() {
    ProtonTheme {
        SecondFactorInputScreen(
            state = SecondFactorInputState.Idle
        )
    }
}
