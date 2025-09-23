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

package ch.protonmail.android.feature.appicon.ui

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.R
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonSettingsDetailsAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.feature.appicon.AppIconSettingsViewModel
import ch.protonmail.android.feature.appicon.model.AppIconUiModel
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData
import coil.compose.AsyncImage

@Composable
fun AppIconSettingsScreen(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    val viewModel: AppIconSettingsViewModel = hiltViewModel()
    val availableIcons = viewModel.getAvailableIcons()

    AppIconSettingsScreen(
        modifier = modifier,
        availableIcons = availableIcons,
        activeIcon = viewModel.getCurrentAppIcon(),
        onIconSelected = { viewModel.setNewAppIcon(it) },
        onBackClick = onBackClick
    )
}

@Composable
private fun AppIconSettingsScreen(
    modifier: Modifier = Modifier,
    availableIcons: List<AppIconUiModel>,
    activeIcon: AppIconUiModel,
    onIconSelected: (AppIconUiModel) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsDetailsAppBar(
                title = stringResource(id = R.string.settings_change_icon_title),
                onBackClick = onBackClick
            )
        },
        content = { paddingValues ->
            AppIconSettingsScreen(
                modifier = Modifier.padding(paddingValues),
                availableIcons,
                activeIcon
            ) { onIconSelected(it) }
        }
    )
}

@Composable
private fun AppIconSettingsScreen(
    modifier: Modifier = Modifier,
    availableIcons: List<AppIconUiModel>,
    activeIcon: AppIconUiModel,
    onItemConfirmed: (AppIconUiModel) -> Unit
) {
    var showRestartDialog by remember { mutableStateOf(false) }
    var currentIcon: AppIconUiModel by remember { mutableStateOf(activeIcon) }
    var pendingIcon: AppIconUiModel by remember { mutableStateOf(activeIcon) }

    val brandIconList = availableIcons.filter { it.data.category == AppIconData.IconCategory.ProtonMail }
    val discreetIconList = availableIcons.filter { it.data.category == AppIconData.IconCategory.Discreet }

    val currentlySelected: (AppIconUiModel) -> Boolean = { it == pendingIcon }
    val onItemSelected: (AppIconUiModel) -> Unit = {
        pendingIcon = it
        showRestartDialog = true
    }

    if (showRestartDialog) {
        ProtonAlertDialog(
            title = stringResource(id = R.string.settings_change_icon_confirmation_title),
            text = {
                Text(
                    text = stringResource(id = R.string.settings_change_icon_confirmation_details),
                    style = ProtonTheme.typography.bodyMediumWeak
                )
            },
            confirmButton = {
                ProtonAlertDialogButton(
                    titleResId = R.string.settings_change_icon_change_icon
                ) {
                    onItemConfirmed(pendingIcon)
                    showRestartDialog = false
                }
            },
            dismissButton = {
                ProtonAlertDialogButton(
                    titleResId = R.string.settings_change_icon_action_cancel
                ) {
                    pendingIcon = currentIcon
                    showRestartDialog = false
                }
            },
            onDismissRequest = {
                pendingIcon = currentIcon
                showRestartDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.Large)
    ) {
        Text(
            modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Large),
            text = stringResource(id = R.string.settings_change_icon_header_protonmail),
            style = ProtonTheme.typography.titleMedium
        )

        FlowRow {
            brandIconList.forEach {
                AppIconElement(
                    preset = it,
                    isSelected = currentlySelected.invoke(it),
                    onClick = {
                        if (!currentlySelected.invoke(it)) {
                            onItemSelected(it)
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = ProtonDimens.Spacing.Large)
                        .weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))
        Text(
            modifier = Modifier.padding(top = ProtonDimens.Spacing.Large),
            text = stringResource(id = R.string.settings_change_icon_header_discreet),
            style = ProtonTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Small))

        Text(
            modifier = Modifier.padding(bottom = ProtonDimens.Spacing.Large),
            text = stringResource(id = R.string.settings_change_icon_description),
            style = ProtonTheme.typography.bodyMedium
        )
        FlowRow {
            discreetIconList.forEach {
                AppIconElement(
                    preset = it,
                    isSelected = currentlySelected.invoke(it),
                    onClick = {
                        if (!currentlySelected.invoke(it)) {
                            onItemSelected(it)
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = ProtonDimens.Spacing.Large)
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AppIconElement(
    preset: AppIconUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIcon(preset = preset, onClick = onClick)
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
        Text(
            text = stringResource(id = preset.labelResId),
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.bodyMedium,
            color = ProtonTheme.colors.textWeak
        )

        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
private fun AppIcon(
    modifier: Modifier = Modifier,
    preset: AppIconUiModel,
    onClick: () -> Unit
) {
    val imageModifier = modifier
        .size(ProtonDimens.IconSize.ExtraExtraLarge)
        .clip(CircleShape)
        .border(1.dp, ProtonTheme.colors.separatorNorm, CircleShape)
        .clickable(onClick = onClick)

    AsyncImage(
        model = preset.iconPreviewResId,
        contentDescription = NO_CONTENT_DESCRIPTION,
        modifier = imageModifier,
        contentScale = ContentScale.Crop
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppSettingsIconPreview() {
    ProtonTheme {
        AppIconSettingsScreen(
            availableIcons = AppIconData.ALL_ICONS.map {
                AppIconUiModel(it, R.mipmap.ic_launcher, R.string.app_name)
            },
            activeIcon = AppIconUiModel(
                AppIconData.DEFAULT,
                R.mipmap.ic_launcher,
                R.string.app_name
            )
        ) { }
    }
}
