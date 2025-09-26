/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.presentation.settings.signature.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAppSettingsItemNorm
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonMainSettingsIcon
import ch.protonmail.android.design.compose.component.ProtonOutlinedTextField
import ch.protonmail.android.design.compose.component.ProtonSettingsDetailsAppBar
import ch.protonmail.android.design.compose.component.ProtonSettingsToggleItem
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonInvertedTheme
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailsettings.domain.model.MobileSignatureStatus
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.R.string
import ch.protonmail.android.mailsettings.presentation.settings.signature.model.MobileSignatureSettingsUiModel
import ch.protonmail.android.mailsettings.presentation.settings.signature.model.MobileSignatureState
import ch.protonmail.android.mailsettings.presentation.settings.signature.model.MobileSignatureViewAction

@Composable
fun MobileSignatureSettingsScreen(
    modifier: Modifier = Modifier,
    signatureActions: MobileSignatureSettingsScreen.Actions,
    viewModel: MobileSignatureSettingsViewModel = hiltViewModel()
) {

    val actions = signatureActions.copy(
        onToggleSignatureEnabled = { viewModel.submit(MobileSignatureViewAction.ToggleSignatureEnabled(it)) },
        onEditSignatureClick = { viewModel.submit(MobileSignatureViewAction.EditSignatureValue) },
        onSignatureUpdated = { newSignature ->
            viewModel.submit(MobileSignatureViewAction.UpdateSignatureValue(newSignature))
        }
    )
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        MobileSignatureState.Loading -> ProtonCenteredProgress()
        is MobileSignatureState.Data -> MobileSignatureSettingsScreen(
            modifier = modifier,
            settings = state.settings,
            actions = actions
        )
    }
}

@Composable
private fun MobileSignatureSettingsScreen(
    modifier: Modifier = Modifier,
    settings: MobileSignatureSettingsUiModel,
    actions: MobileSignatureSettingsScreen.Actions
) {
    val editSignatureDialogState = rememberSaveable { mutableStateOf(false) }

    ConsumableLaunchedEffect(effect = settings.editSignatureEffect) {
        editSignatureDialogState.value = true
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsDetailsAppBar(
                title = stringResource(id = string.mail_settings_app_customization_mobile_signature_header),
                onBackClick = actions.onBackClick
            )
        },
        content = { paddingValues ->
            MobileSignatureSettingsContent(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = ProtonDimens.Spacing.Large),
                settings = settings,
                actions = actions
            )
        }
    )

    if (editSignatureDialogState.value) {
        EditSignatureDialog(
            editSignatureDialogState = editSignatureDialogState,
            settings = settings,
            signatureUpdated = actions.onSignatureUpdated
        )
    }
}

@Composable
private fun EditSignatureDialog(
    editSignatureDialogState: MutableState<Boolean>,
    settings: MobileSignatureSettingsUiModel,
    signatureUpdated: (String) -> Unit
) {
    var signatureText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(settings.signatureValue))
    }

    ProtonAlertDialog(
        text = {
            Column {
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))
                ProtonOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = signatureText,
                    onValueChange = {
                        signatureText = it
                    },
                    singleLine = false,
                    textStyle = ProtonTheme.typography.bodyLargeNorm
                )
            }
        },
        dismissButton = {
            ProtonAlertDialogButton(
                titleResId = string.presentation_alert_cancel
            ) { editSignatureDialogState.value = false }
        },
        confirmButton = {
            ProtonAlertDialogButton(
                titleResId = string.presentation_alert_ok
            ) {
                signatureUpdated(signatureText.text)
                editSignatureDialogState.value = false
            }
        },
        onDismissRequest = { editSignatureDialogState.value = false }
    )
}


@Composable
private fun MobileSignatureSettingsContent(
    modifier: Modifier = Modifier,
    settings: MobileSignatureSettingsUiModel,
    actions: MobileSignatureSettingsScreen.Actions
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProtonTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(),
            colors = CardDefaults.cardColors().copy(
                containerColor = ProtonTheme.colors.backgroundInvertedSecondary
            )
        ) {
            ProtonSettingsToggleItem(
                modifier = Modifier.padding(ProtonDimens.Spacing.Large),
                name = stringResource(string.mail_settings_app_customization_mobile_signature_enable),
                value = settings.enabled,
                onToggle = actions.onToggleSignatureEnabled
            )
        }

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProtonTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(),
            colors = CardDefaults.cardColors().copy(
                containerColor = ProtonTheme.colors.backgroundInvertedSecondary
            )
        ) {
            ProtonAppSettingsItemNorm(
                name = settings.signatureValue,
                enabled = settings.enabled,
                isClickable = settings.enabled,
                onClick = actions.onEditSignatureClick,
                icon = {
                    ProtonMainSettingsIcon(
                        iconRes = R.drawable.ic_proton_pencil,
                        contentDescription = "",
                        tint = ProtonTheme.colors.iconHint
                    )
                }
            )
        }
    }
}

object MobileSignatureSettingsScreen {
    data class Actions(
        val onBackClick: () -> Unit,
        val onToggleSignatureEnabled: (Boolean) -> Unit,
        val onEditSignatureClick: () -> Unit,
        val onSignatureUpdated: (String) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onBackClick = {},
                onToggleSignatureEnabled = {},
                onEditSignatureClick = {},
                onSignatureUpdated = {}
            )
        }
    }
}

@Preview(name = "Mobile Signature – Enabled", showBackground = true)
@Composable
private fun PreviewMobileSignatureEnabled() {
    ProtonInvertedTheme {
        MobileSignatureSettingsScreen(
            settings = MobileSignatureSettingsUiModel(
                enabled = true,
                signatureValue = "Sent from Proton Mail",
                signatureStatus = MobileSignatureStatus.Enabled,
                editSignatureEffect = Effect.empty()
            ),
            actions = MobileSignatureSettingsScreen.Actions.Empty
        )
    }
}

@Preview(name = "Mobile Signature – Disabled", showBackground = true)
@Composable
private fun PreviewMobileSignatureDisabled() {
    ProtonInvertedTheme {
        MobileSignatureSettingsContent(
            settings = MobileSignatureSettingsUiModel(
                enabled = false,
                signatureStatus = MobileSignatureStatus.Disabled,
                signatureValue = "Sent from Proton Mail",
                editSignatureEffect = Effect.empty()
            ),
            actions = MobileSignatureSettingsScreen.Actions.Empty
        )
    }
}
