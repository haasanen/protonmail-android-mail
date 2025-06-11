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

package ch.protonmail.android.mailpinlock.presentation.pin.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonTextButton
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.standalone.LocalLockScreenEntryPointIsStandalone
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinViewAction
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinViewModel
import ch.protonmail.android.mailpinlock.presentation.pin.ConfirmButtonUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.DescriptionUiModel
import timber.log.Timber

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLockPinScreen(
    onClose: () -> Unit,
    onShowSuccessSnackbar: (String) -> Unit,
    viewModel: AutoLockPinViewModel = hiltViewModel()
) {
    val state: AutoLockPinState by viewModel.state.collectAsStateWithLifecycle()

    val pinTextFieldState = viewModel.pinTextFieldState

    val configuration = LocalConfiguration.current
    val isCompact = configuration.screenHeightDp < 480 || configuration.fontScale > 1.3f

    val isStandalone = LocalLockScreenEntryPointIsStandalone.current

    val actions = AutoLockPinScreenV2.Actions(
        onBack = { viewModel.submit(AutoLockPinViewAction.PerformBack) },
        onClose = onClose,
        onNext = { viewModel.submit(AutoLockPinViewAction.PerformConfirm) },
        onShowSuccessSnackbar = onShowSuccessSnackbar
    )

    val signOutActions = AutoLockPinScreenV2.SignOutActions(
        onSignOut = { viewModel.submit(AutoLockPinViewAction.RequestSignOut) },
        onSignOutConfirmed = { viewModel.submit(AutoLockPinViewAction.ConfirmSignOut) },
        onSignOutCanceled = { viewModel.submit(AutoLockPinViewAction.CancelSignOut) }
    )

    val containerColor = if (isStandalone) {
        ProtonTheme.colors.backgroundNorm.copy(alpha = 0.25f)
    } else {
        ProtonTheme.colors.backgroundNorm
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = containerColor,
            topBar = {
                if (!isStandalone) {
                    PinScreenTopBar(
                        state = state,
                        isCompact = isCompact,
                        onBackClick = actions.onBack
                    )
                }
            }
        ) { paddingValues ->
            when (state) {
                AutoLockPinState.Loading -> ProtonCenteredProgress()
                is AutoLockPinState.DataLoaded -> {
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        PinScreenContent(
                            state = state as AutoLockPinState.DataLoaded,
                            isCompact = isCompact,
                            pinTextFieldState = pinTextFieldState,
                            actions = actions,
                            signOutActions = signOutActions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PinScreenTopBar(
    state: AutoLockPinState,
    isCompact: Boolean,
    onBackClick: () -> Unit
) {
    val uiModel = (state as? AutoLockPinState.DataLoaded)?.topBarState?.topBarStateUiModel ?: return

    ProtonTopAppBar(
        title = { if (isCompact) Text(text = stringResource(id = uiModel.textRes)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.presentation_back)
                )
            }
        }
    )
}

@Composable
private fun PinScreenContent(
    state: AutoLockPinState.DataLoaded,
    modifier: Modifier = Modifier,
    isCompact: Boolean,
    pinTextFieldState: TextFieldState,
    actions: AutoLockPinScreenV2.Actions,
    signOutActions: AutoLockPinScreenV2.SignOutActions
) {
    val imeHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(state.pinInsertionState) {
        Timber.tag("Moved to state").d("${state.pinInsertionState}")
    }

    BackHandler(true) {
        actions.onBack()
    }

    ConsumableLaunchedEffect(state.closeScreenEffect) {
        actions.onClose()
    }

    ConsumableTextEffect(state.snackbarSuccessEffect) {
        actions.onShowSuccessSnackbar(it)
    }



    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ProtonDimens.Spacing.Large)
    ) {
        @Suppress("MagicNumber")
        Spacer(Modifier.weight(.3f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoLockPinScreenHeader(
                descriptionUiModel = state.pinInsertionState.descriptionUiModel,
                isCompact = isCompact
            )

            PinInputSection(
                modifier = Modifier.focusRequester(focusRequester),
                pinTextFieldState = pinTextFieldState,
                error = state.pinInsertionState.error,
                maxLength = 21
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

            if (imeHeight > 0.dp) {
                @Suppress("MagicNumber")
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Jumbo * 3))
            }
        }

        Column(
            modifier = Modifier.padding(bottom = imeHeight)
        ) {
            if (isCompact && imeHeight == 0.dp) {
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))
            }

            PinScreenButton(
                uiModel = state.confirmButtonState.confirmButtonUiModel,
                onClick = actions.onNext
            )


            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))

            AutoLockPinSignOutItem(state = state.signOutButtonState, actions = signOutActions)

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Jumbo))
        }
    }
}


@Composable
private fun PinScreenButton(uiModel: ConfirmButtonUiModel, onClick: () -> Unit) {
    ProtonTextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.Standard)
            .background(
                color = ProtonTheme.colors.brandNorm,
                shape = RoundedCornerShape(56.dp)
            ),
        onClick = {
            onClick()
        }
    ) {
        Text(
            text = stringResource(uiModel.textRes),
            style = ProtonTheme.typography.titleMedium,
            color = ProtonTheme.colors.textInverted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AutoLockPinScreenHeader(descriptionUiModel: DescriptionUiModel, isCompact: Boolean) {

    Column(
        modifier = Modifier.padding(ProtonDimens.Spacing.Standard),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isCompact) {
            AutoLockIcon()

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))

            Text(
                text = stringResource(descriptionUiModel.titleRes),
                style = ProtonTheme.typography.titleLargeNorm,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(descriptionUiModel.descriptionRes),
            style = ProtonTheme.typography.bodyMediumNorm,
            color = ProtonTheme.colors.textWeak,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))
    }
}

object AutoLockPinScreenV2 {
    data class Actions(
        val onBack: () -> Unit,
        val onClose: () -> Unit,
        val onNext: () -> Unit,
        val onShowSuccessSnackbar: (snackbarText: String) -> Unit
    )

    data class SignOutActions(
        val onSignOut: () -> Unit,
        val onSignOutConfirmed: () -> Unit,
        val onSignOutCanceled: () -> Unit
    )
}

object AutoLockPinScreen {

    const val AutoLockPinModeKey = "auto_lock_pin_open_mode"
}

@Composable
@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AutoLockPinScreenPreview() {
    AutoLockPinScreen(onClose = {}, onShowSuccessSnackbar = { _ -> })
}
