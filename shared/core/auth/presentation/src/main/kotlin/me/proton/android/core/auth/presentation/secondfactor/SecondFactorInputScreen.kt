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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputAction
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.util.LaunchOnScreenView
import me.proton.core.presentation.utils.launchOnScreenView

@Composable
fun SecondFactorInputScreen(
    onClose: () -> Unit,
    onError: (String?) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    externalAction: StateFlow<Fido2InputAction?> = MutableStateFlow(null),
    onEmitAction: (Fido2InputAction?) -> Unit,
    viewModel: SecondFactorInputViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        lifecycleOwner.launchOnScreenView(savedStateRegistryOwner.savedStateRegistry) {
            viewModel.onScreenView()
        }
    }

    BackHandler(enabled = true) {
        viewModel.perform(SecondFactorInputAction.Close)
    }

    SecondFactorInputScreen(
        state = state,
        modifier = modifier,
        onClose = onClose,
        onError = onError,
        onSuccess = onSuccess,
        onBackClicked = { viewModel.perform(SecondFactorInputAction.Close) },
        onTabSelected = { viewModel.perform(SecondFactorInputAction.SelectTab(it)) },
        onEmitAction = onEmitAction,
        externalAction = externalAction,
        onScreenView = viewModel::onScreenView
    )
}

@Composable
fun SecondFactorInputScreen(
    state: SecondFactorInputState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    externalAction: StateFlow<Fido2InputAction?>,
    onEmitAction: (Fido2InputAction?) -> Unit,
    onScreenView: () -> Unit = {}
) {
    LaunchedEffect(state) {
        when (state) {
            is SecondFactorInputState.Closed -> onClose()
            else -> Unit
        }
    }

    LaunchOnScreenView(enqueue = onScreenView)

    when (state) {
        is SecondFactorInputState.Loading -> {
            val actualSelectedTabIndex = state.tabs.actualTabIndex(selectedTab = state.selectedTab)

            SecondFactorInputScaffold(
                modifier = modifier,
                onBackClicked = onBackClicked,
                onTabSelected = onTabSelected,
                onClose = onClose,
                onError = onError,
                onSuccess = onSuccess,
                selectedTabIndex = actualSelectedTabIndex,
                selectedTab = state.selectedTab,
                onEmitAction = onEmitAction,
                externalAction = externalAction,
                tabs = state.tabs
            )
        }

        else -> Unit
    }
}

@Composable
fun SecondFactorInputScaffold(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    selectedTabIndex: Int = 0,
    selectedTab: SecondFactorTab,
    tabs: List<SecondFactorTab>,
    externalAction: StateFlow<Fido2InputAction?>,
    onEmitAction: (Fido2InputAction?) -> Unit
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
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Column(modifier = Modifier.padding(DefaultSpacing)) {
                Text(
                    style = LocalTypography.current.headline,
                    text = stringResource(R.string.auth_second_factor_title)
                )

                if (tabs.size > 1) {
                    Text(
                        text = stringResource(R.string.auth_second_factor_subtitle),
                        style = ProtonTypography.Default.defaultSmallWeak,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = DefaultSpacing)
                    )
                }

                SecondFactorTabContent(
                    selectedTab = selectedTab,
                    selectedTabIndex = selectedTabIndex,
                    tabs = tabs,
                    onTabSelected = onTabSelected,
                    onClose = onClose,
                    onError = onError,
                    onSuccess = onSuccess,
                    onEmitAction = onEmitAction,
                    externalAction = externalAction,
                    modifier = Modifier.padding(top = DefaultSpacing)
                )
            }
        }
    }
}

@Composable
fun SecondFactorTab.localizedLabel(): String {
    return when (this) {
        SecondFactorTab.SecurityKey -> stringResource(R.string.auth_second_factor_tab_security_key)
        SecondFactorTab.Otp -> stringResource(R.string.auth_second_factor_tab_otp)
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
            state = SecondFactorInputState.Loading(
                selectedTab = SecondFactorTab.SecurityKey,
                tabs = listOf(SecondFactorTab.SecurityKey, SecondFactorTab.Otp)
            ),
            onTabSelected = {},
            onEmitAction = {},
            externalAction = MutableStateFlow(null)
        )
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
fun SecondFactorInputScreenOtpOnlyPreview() {
    ProtonTheme {
        SecondFactorInputScreen(
            state = SecondFactorInputState.Loading(
                selectedTab = SecondFactorTab.Otp,
                tabs = listOf(SecondFactorTab.Otp)
            ),
            onTabSelected = {},
            externalAction = MutableStateFlow(null),
            onEmitAction = {}
        )
    }
}
