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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.component.ProtonButton
import ch.protonmail.android.design.compose.component.ProtonSettingsDetailsAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditOperation
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditState
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui.ResetToDefaultConfirmationDialog

@Composable
internal fun EditToolbarTopBar(
    onBackClick: () -> Unit,
    state: CustomizeToolbarEditState,
    onAction: (CustomizeToolbarEditOperation) -> Unit
) {
    ProtonSettingsDetailsAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = (state as? CustomizeToolbarEditState.Data)?.toolbarTitle?.string()
            ?: stringResource(id = R.string.mail_settings_custom_toolbar_nav_title_list),
        onBackClick = onBackClick,
        actions = {
            if (state is CustomizeToolbarEditState.Data) {
                EditToolbarActions(
                    onSave = {
                        onAction(CustomizeToolbarEditOperation.SaveClicked)
                    },
                    onReset = {
                        onAction(CustomizeToolbarEditOperation.ResetToDefaultConfirmed)
                    }
                )
            }
        }
    )
}

@Composable
private fun EditToolbarActions(onSave: () -> Unit, onReset: () -> Unit) {

    var showResetToDefaultConfirmationDialog by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ProtonButton(
            shape = ProtonTheme.shapes.huge,
            border = null,
            colors = ButtonDefaults.buttonColors(containerColor = ProtonTheme.colors.interactionBrandDefaultNorm),
            elevation = null,
            onClick = onSave
        ) {
            Text(
                stringResource(R.string.mail_settings_custom_toolbar_nav_button_save),
                color = ProtonTheme.colors.textInverted,
                style = ProtonTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Large))

        IconButton(
            onClick = { isDropdownExpanded = true },
            modifier = Modifier.size(ProtonDimens.IconSize.Default)
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Large))

        DropdownMenu(
            modifier = Modifier.background(ProtonTheme.colors.backgroundSecondary),
            expanded = isDropdownExpanded,
            offset = DpOffset(x = -ProtonDimens.Spacing.Large, y = 0.dp),
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(ProtonDimens.IconSize.Medium),
                            painter = painterResource(R.drawable.ic_proton_arrow_rotate_right),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Standard))
                        Text(
                            stringResource(R.string.mail_settings_custom_toolbar_reset),
                            style = ProtonTheme.typography.bodyLarge
                        )
                    }
                },
                onClick = {
                    isDropdownExpanded = false
                    showResetToDefaultConfirmationDialog = true
                },
                contentPadding = PaddingValues(ProtonDimens.Spacing.Standard, 0.dp)
            )
        }
    }

    if (showResetToDefaultConfirmationDialog) {
        ResetToDefaultConfirmationDialog(
            onConfirmClicked = {
                showResetToDefaultConfirmationDialog = false
                onReset()
            },
            onCancelClicked = {
                showResetToDefaultConfirmationDialog = false
            }
        )
    }
}
