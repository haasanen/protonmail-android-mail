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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ch.protonmail.android.R
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonSwitch
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.feature.appicon.model.AppIconUiModel
import ch.protonmail.android.mailcommon.presentation.ui.MailDivider
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun AppIconSettingsContent(
    modifier: Modifier = Modifier,
    availableIcons: ImmutableList<AppIconUiModel>,
    activeIcon: AppIconUiModel,
    onIconConfirmed: (AppIconUiModel) -> Unit,
    onLearnMoreClick: () -> Unit
) {
    var showRestartDialog by remember { mutableStateOf(false) }
    var currentIcon: AppIconUiModel by remember { mutableStateOf(activeIcon) }
    var pendingIcon: AppIconUiModel by remember { mutableStateOf(activeIcon) }
    var isDiscreetEnabled by remember {
        mutableStateOf(activeIcon.data.category == AppIconData.IconCategory.Discreet)
    }

    val discreetIconList = availableIcons.filter { it.data.category == AppIconData.IconCategory.Discreet }
    val defaultIcon = availableIcons.firstOrNull { it.data.category == AppIconData.IconCategory.ProtonMail }

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
                    currentIcon = pendingIcon
                    onIconConfirmed(pendingIcon)
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
            .fillMaxSize()
            .padding(ProtonDimens.Spacing.Large)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProtonTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(),
            colors = CardDefaults.cardColors().copy(
                containerColor = ProtonTheme.colors.backgroundInvertedSecondary
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ProtonDimens.Spacing.Large)
                ) {
                    AppIconPreview(
                        preset = currentIcon,
                        modifier = Modifier.size(64.dp),
                        showBorder = currentIcon.data.category == AppIconData.IconCategory.ProtonMail
                    )

                    Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Medium))

                    Text(
                        text = if (currentIcon.data.category == AppIconData.IconCategory.Discreet) {
                            stringResource(id = R.string.settings_app_icon_title_discreet)
                        } else {
                            stringResource(id = R.string.settings_app_icon_title_default)
                        },
                        style = ProtonTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Small))

                    val descriptionText = buildAnnotatedString {
                        append(stringResource(id = R.string.settings_app_icon_description))
                        append(" ")
                        withStyle(style = SpanStyle(color = ProtonTheme.colors.brandNorm)) {
                            append(stringResource(id = R.string.settings_app_icon_learn_more))
                        }
                    }

                    Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Small))

                    Text(
                        text = descriptionText,
                        style = ProtonTheme.typography.bodyMedium,
                        color = ProtonTheme.colors.textWeak,
                        modifier = Modifier.clickable { onLearnMoreClick() }
                    )
                }

                MailDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = ProtonDimens.Spacing.Large,
                            vertical = ProtonDimens.Spacing.Small
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_app_icon_discreet_toggle),
                        style = ProtonTheme.typography.bodyLarge,
                        color = ProtonTheme.colors.textNorm
                    )

                    ProtonSwitch(
                        checked = isDiscreetEnabled,
                        onCheckedChange = { enabled ->
                            isDiscreetEnabled = enabled
                            if (!enabled && currentIcon.data.category == AppIconData.IconCategory.Discreet) {
                                // Turning off while using a discreet icon - revert to default
                                defaultIcon?.let { icon ->
                                    pendingIcon = icon
                                    showRestartDialog = true
                                }
                            }
                        }
                    )
                }
            }
        }

        if (isDiscreetEnabled && discreetIconList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ProtonTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(),
                colors = CardDefaults.cardColors().copy(
                    containerColor = ProtonTheme.colors.backgroundInvertedSecondary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ProtonDimens.Spacing.Large),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    discreetIconList.forEach { icon ->
                        SelectableAppIcon(
                            preset = icon,
                            isSelected = icon == currentIcon,
                            onClick = {
                                if (icon != currentIcon) {
                                    pendingIcon = icon
                                    showRestartDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
