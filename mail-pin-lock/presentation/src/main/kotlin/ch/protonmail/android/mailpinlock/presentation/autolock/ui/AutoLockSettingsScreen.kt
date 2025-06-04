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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonAppSettingsItemInvert
import ch.protonmail.android.design.compose.component.ProtonAppSettingsItemNorm
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonMainSettingsIcon
import ch.protonmail.android.design.compose.component.ProtonSettingsToggleItem
import ch.protonmail.android.design.compose.component.ProtonSettingsTopBar
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockBiometricsUiModel
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsViewAction
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsViewAction.ToggleAutoLockPreference
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsViewModel
import ch.protonmail.android.mailpinlock.presentation.autolock.AutolockSettings
import ch.protonmail.android.mailpinlock.presentation.autolock.AutolockSettingsUiState
import ch.protonmail.android.mailpinlock.presentation.autolock.ProtectionType
import ch.protonmail.android.mailpinlock.presentation.autolock.ui.AutoLockSettingsScreen.Actions
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLockSettingsScreen(
    modifier: Modifier = Modifier,
    actions: Actions,
    viewModel: AutoLockSettingsViewModel = hiltViewModel()
) {
    val snackbarHostState = ProtonSnackbarHostState()
    val effects = viewModel.effects.collectAsStateWithLifecycle().value
    val state: AutolockSettingsUiState by viewModel.state.collectAsState()

    SnackbarWarnings(
        snackbarHostState = snackbarHostState,
        effects.autoLockBiometricsHwError,
        effects.autoLockBiometricsEnrollmentError
    )
    SnackbarErrors(
        snackbarHostState = snackbarHostState,
        effects.updateError
    )
    ConsumableLaunchedEffect(effects.forceOpenPinCreation) {
        // ET-6548 onPinScreenNavigation(AutoLockInsertionMode.CreatePin)
    }
    ConsumableLaunchedEffect(effects.forceOpenPinCreation) {
        // ET-6548 onPinScreenNavigation(AutoLockInsertionMode.ChangePin)
    }

    when (val uiState = state) {
        AutolockSettingsUiState.Loading -> ProtonCenteredProgress()
        is AutolockSettingsUiState.Data -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    ProtonSettingsTopBar(
                        title = stringResource(id = R.string.mail_pinlock_settings_title),
                        onBackClick = actions.onBackClick
                    )
                },
                snackbarHost = { DismissableSnackbarHost(protonSnackbarHostState = snackbarHostState) },
                content = { paddingValues ->
                    AutolockSettingScreen(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = ProtonDimens.Spacing.Large),
                        settings = uiState.settings,
                        submitAction = { viewModel.submit(it) },
                        actions = actions
                    )
                }
            )
        }
    }
}

@Composable
private fun AutolockSettingScreen(
    modifier: Modifier = Modifier,
    settings: AutolockSettings,
    actions: Actions,
    submitAction: (AutoLockSettingsViewAction) -> Unit
) {
    Column(modifier = modifier) {
        AutolockOnOffToggle(
            toggleOn = settings.isEnabled,
            onSubmit = { submitAction(ToggleAutoLockPreference(it)) }
        )

        if (settings.isEnabled) {
            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ProtonTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(),
                colors = CardDefaults.cardColors().copy(
                    containerColor = ProtonTheme.colors.backgroundInvertedSecondary
                )
            ) {
                ChangePinOption(onClickChangePin = actions.onPinScreenNavigation)
                ChangeIntervalOption(
                    selectedChoice = settings.selectedUiInterval,
                    onClickChangeInterval = actions.onChangeIntervalClick
                )
                if (settings.biometricsAvailable) {
                    BiometricsOnOffToggle(
                        toggleOn = settings.biometricsEnabled,
                        onSubmit = {
                            submitAction(
                                AutoLockSettingsViewAction.ToggleAutoLockBiometricsPreference(
                                    AutoLockBiometricsUiModel(
                                        enabled = it,
                                        biometricsEnrolled = false,
                                        biometricsHwAvailable = false
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangeIntervalOption(
    modifier: Modifier = Modifier,
    selectedChoice: TextUiModel,
    onClickChangeInterval: () -> Unit
) {
    ProtonAppSettingsItemInvert(
        modifier = modifier,
        name = stringResource(id = R.string.mail_pinlock_settings_change_interval_title),
        hint = selectedChoice.string(),
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

@Composable
private fun ChangePinOption(modifier: Modifier = Modifier, onClickChangePin: () -> Unit) {
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

@Composable
private fun AutolockOnOffToggle(
    modifier: Modifier = Modifier,
    toggleOn: Boolean,
    onSubmit: (Boolean) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        ProtonSettingsToggleItem(
            modifier = Modifier.padding(ProtonDimens.Spacing.Large),
            name = stringResource(id = R.string.mail_pinlock_settings_toggle_autolock_title),
            hint = stringResource(id = R.string.mail_pinlock_settings_toggle_autolock_description),
            value = toggleOn,
            onToggle = onSubmit
        )
    }
}

@Composable
private fun BiometricsOnOffToggle(
    modifier: Modifier = Modifier,
    toggleOn: Boolean,
    onSubmit: (Boolean) -> Unit
) {
    ProtonSettingsToggleItem(
        modifier = modifier.padding(ProtonDimens.Spacing.Large),
        name = stringResource(id = R.string.unlock_using_biometrics),
        value = toggleOn,
        onToggle = onSubmit
    )
}

@Composable
private fun SnackbarEffect(
    snackbarHostState: ProtonSnackbarHostState,
    type: ProtonSnackbarType,
    effects: Array<out Effect<TextUiModel>>
) {
    effects.forEach {
        ConsumableTextEffect(it) {
            snackbarHostState.showSnackbar(
                message = it,
                type = type
            )
        }
    }
}

@Composable
private fun SnackbarWarnings(snackbarHostState: ProtonSnackbarHostState, vararg effects: Effect<TextUiModel>) {
    SnackbarEffect(snackbarHostState, ProtonSnackbarType.WARNING, effects)
}

@Composable
private fun SnackbarErrors(snackbarHostState: ProtonSnackbarHostState, vararg effects: Effect<TextUiModel>) {
    SnackbarEffect(snackbarHostState, ProtonSnackbarType.ERROR, effects)
}

@Preview(name = "Autolock Settings Screen Enabled", showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewAutolockSettingScreenEnabled() {
    AutolockSettingScreen(
        settings = AutolockSettings(
            TextUiModel("15 minutes"),
            protectionType = ProtectionType.Biometrics,
            biometricsAvailable = true
        ),
        actions = Actions(),
        submitAction = {}
    )
}


@Preview(name = "Autolock Settings Screen Disabled", showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewAutolockSettingScreenDisabled() {
    AutolockSettingScreen(
        settings = AutolockSettings(
            TextUiModel("None"),
            protectionType = ProtectionType.None,
            biometricsAvailable = false
        ),
        actions = Actions(),
        submitAction = {}
    )
}

object AutoLockSettingsScreen {

    data class Actions(
        val onChangeIntervalClick: () -> Unit = {},
        val onBackClick: () -> Unit = {},
        val onPinScreenNavigation: () -> Unit = {}
    )
}
