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

package ch.protonmail.android.mailpinlock.presentation.autolock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ch.protonmail.android.design.compose.component.ProtonAppSettingsItemNorm
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonMainSettingsIcon
import ch.protonmail.android.design.compose.component.ProtonSettingsDetailsAppBar
import ch.protonmail.android.design.compose.component.ProtonSettingsHeader
import ch.protonmail.android.design.compose.component.ProtonSettingsRadioItem
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockInsertionMode
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettings
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsUiState
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsViewAction
import ch.protonmail.android.mailpinlock.presentation.autolock.model.BiometricsOperationFollowUp
import ch.protonmail.android.mailpinlock.presentation.autolock.model.DialogType
import ch.protonmail.android.mailpinlock.presentation.autolock.model.ProtectionType
import ch.protonmail.android.mailpinlock.presentation.autolock.ui.AutoLockSettingsScreen.Actions
import ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel.AutoLockSettingsViewModel
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockPinScreenDialogKeys.AutoLockPinDialogResultKey
import ch.protonmail.android.mailsettings.domain.model.autolock.biometric.BiometricPromptCallback
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost

@Composable
fun AutoLockSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    actions: Actions,
    viewModel: AutoLockSettingsViewModel = hiltViewModel()
) {
    val snackbarHostState = ProtonSnackbarHostState()
    val effects by viewModel.effects.collectAsStateWithLifecycle()
    val state: AutoLockSettingsUiState by viewModel.state.collectAsState()

    val biometricPrompt = rememberBiometricAuthenticator(
        title = stringResource(R.string.mail_settings_biometrics_title_confirm),
        subtitle = stringResource(R.string.mail_settings_biometrics_subtitle_default),
        negativeButtonText = stringResource(R.string.mail_settings_biometrics_button_negative),
        onAuthenticationError = { _, _ ->
            /* no op, handling delegated to system prompt */
        },
        onAuthenticationFailed = { /* no op, handling delegated to system prompt */ },
        onAuthenticationSucceeded = {
            viewModel.submit(AutoLockSettingsViewAction.SetBiometricsPreference)
        }
    )

    ConsumableTextEffect(effects.updateError) {
        snackbarHostState.showSnackbar(message = it, type = ProtonSnackbarType.ERROR)
    }

    ConsumableLaunchedEffect(effects.openPinCreation) {
        actions.onPinScreenNavigation(AutoLockInsertionMode.CreatePin)
    }

    ConsumableLaunchedEffect(effects.pinLockRemovalRequested) {
        actions.onDialogNavigation(DialogType.DisablePin)
    }

    ConsumableLaunchedEffect(effects.pinLockToBiometricsRequested) {
        actions.onDialogNavigation(DialogType.MigrateToBiometrics)
    }

    ConsumableLaunchedEffect(effects.pinLockChangeRequested) {
        actions.onDialogNavigation(DialogType.ChangePin)
    }

    ConsumableLaunchedEffect(effects.requestBiometricsAuth) {
        val callback = BiometricPromptCallback(
            onAuthenticationError = {},
            onAuthenticationFailed = {},
            onAuthenticationSucceeded = {
                val followUp = when (it) {
                    BiometricsOperationFollowUp.SetNone -> AutoLockSettingsViewAction.RemoveBiometricsProtection
                    BiometricsOperationFollowUp.SetPin -> AutoLockSettingsViewAction.SetPinPreference
                    BiometricsOperationFollowUp.SetBiometrics -> AutoLockSettingsViewAction.SetBiometricsPreference
                    BiometricsOperationFollowUp.RemovePinAndSetBiometrics ->
                        AutoLockSettingsViewAction.MigrateFromPinToBiometrics
                }

                viewModel.submit(followUp)
            }
        )
        biometricPrompt.authenticate(callback)
    }

    LaunchedEffect(navController) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<String?>(AutoLockPinDialogResultKey, null)
            ?.collect { result ->
                if (result == null) return@collect

                when (result) {
                    DialogType.ChangePin.resultKey ->
                        viewModel.submit(AutoLockSettingsViewAction.ForceRequestPinCreation)

                    DialogType.DisablePin.resultKey -> Unit
                    DialogType.MigrateToBiometrics.resultKey ->
                        viewModel.submit(AutoLockSettingsViewAction.RequestBiometricsProtection)
                }
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(AutoLockPinDialogResultKey)
            }
    }

    when (val uiState = state) {
        AutoLockSettingsUiState.Loading -> ProtonCenteredProgress()
        is AutoLockSettingsUiState.Data -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    ProtonSettingsDetailsAppBar(
                        title = stringResource(id = R.string.mail_pinlock_settings_title),
                        onBackClick = actions.onBackClick
                    )
                },
                snackbarHost = { DismissableSnackbarHost(protonSnackbarHostState = snackbarHostState) },
                content = { paddingValues ->
                    AutoLockSettingScreen(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = ProtonDimens.Spacing.Large),
                        settings = uiState.settings,
                        submitAction = { viewModel.submit(it) },
                        onChangeIntervalNavigation = actions.onChangeIntervalClick,
                        onChangePin = { viewModel.submit(AutoLockSettingsViewAction.RequestPinProtectionChange) }
                    )
                }
            )
        }
    }
}

@Composable
@Suppress("UseComposableActions")
private fun AutoLockSettingScreen(
    modifier: Modifier = Modifier,
    settings: AutoLockSettings,
    submitAction: (AutoLockSettingsViewAction) -> Unit,
    onChangeIntervalNavigation: () -> Unit = {},
    onChangePin: () -> Unit
) {

    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProtonTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(),
            colors = CardDefaults.cardColors().copy(
                containerColor = ProtonTheme.colors.backgroundInvertedSecondary
            )
        ) {
            ProtonSettingsRadioItem(
                name = stringResource(R.string.mail_pinlock_settings_no_lock),
                isSelected = settings.protectionType == ProtectionType.None,
                onItemSelected = {
                    submitAction(AutoLockSettingsViewAction.RequestProtectionRemoval)
                }
            )
            HorizontalDivider(color = ProtonTheme.colors.backgroundInvertedNorm)
            ProtonSettingsRadioItem(
                name = stringResource(R.string.mail_pinlock_settings_with_pin),
                isSelected = settings.protectionType == ProtectionType.Pin,
                onItemSelected = {
                    submitAction(AutoLockSettingsViewAction.RequestPinProtection)
                }
            )
            HorizontalDivider(color = ProtonTheme.colors.backgroundInvertedNorm)

            if (settings.biometricsAvailable) {
                ProtonSettingsRadioItem(
                    name = stringResource(R.string.mail_pinlock_settings_with_biometrics),
                    isSelected = settings.protectionType == ProtectionType.Biometrics,
                    onItemSelected = {
                        submitAction(AutoLockSettingsViewAction.RequestBiometricsProtection)
                    }
                )
            }
        }

        Text(
            modifier = Modifier
                .padding(vertical = ProtonDimens.Spacing.Standard)
                .padding(horizontal = ProtonDimens.Spacing.Large),
            text = stringResource(R.string.mail_pinlock_settings_logout_disclaimer),
            style = ProtonTheme.typography.bodyMedium,
            color = ProtonTheme.colors.textWeak
        )

        if (settings.protectionType != ProtectionType.None) {
            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))

            if (settings.protectionType == ProtectionType.Pin) {
                ChangePinOption(onClickChangePin = onChangePin)
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))
            }

            ChangeIntervalOption(
                selectedChoice = settings.selectedUiInterval,
                onClickChangeInterval = onChangeIntervalNavigation
            )
            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))
        }
    }
}

@Composable
private fun ChangeIntervalOption(
    modifier: Modifier = Modifier,
    selectedChoice: TextUiModel,
    onClickChangeInterval: () -> Unit
) {
    ProtonSettingsHeader(title = R.string.mail_pinlock_settings_change_interval_title)
    Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Small))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        ProtonAppSettingsItemNorm(
            modifier = modifier,
            name = selectedChoice.string(),
            onClick = onClickChangeInterval,
            icon = {
                ProtonMainSettingsIcon(
                    iconRes = R.drawable.ic_proton_chevron_up_down,
                    contentDescription = stringResource(id = R.string.mail_pinlock_settings_change_interval_title),
                    tint = ProtonTheme.colors.iconHint
                )
            }
        )
    }
}

@Composable
private fun ChangePinOption(modifier: Modifier = Modifier, onClickChangePin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        ProtonAppSettingsItemNorm(
            modifier = modifier,
            name = stringResource(id = R.string.mail_pinlock_settings_change_pin_description),
            onClick = onClickChangePin,
            icon = {
                ProtonMainSettingsIcon(
                    iconRes = R.drawable.ic_proton_chevron_right,
                    contentDescription = stringResource(id = R.string.mail_pinlock_settings_change_pin_description),
                    tint = ProtonTheme.colors.iconHint
                )
            }
        )
    }
}

object AutoLockSettingsScreen {

    data class Actions(
        val onChangeIntervalClick: () -> Unit,
        val onBackClick: () -> Unit,
        val onPinScreenNavigation: (AutoLockInsertionMode) -> Unit,
        val onDialogNavigation: (DialogType) -> Unit
    )
}

@Preview(name = "Autolock Settings Screen Enabled", showBackground = true, device = "id:pixel_5")
@Composable
private fun PreviewAutoLockSettingScreenEnabled() {
    AutoLockSettingScreen(
        settings = AutoLockSettings(
            TextUiModel("15 minutes"),
            protectionType = ProtectionType.Biometrics,
            biometricsAvailable = true
        ),
        submitAction = {},
        onChangePin = {}
    )
}


@Preview(name = "Autolock Settings Screen Disabled", showBackground = true, device = "id:pixel_5")
@Composable
private fun PreviewAutoLockSettingScreenDisabled() {
    AutoLockSettingScreen(
        settings = AutoLockSettings(
            TextUiModel("None"),
            protectionType = ProtectionType.None,
            biometricsAvailable = false
        ),
        submitAction = {},
        onChangePin = {}
    )
}
