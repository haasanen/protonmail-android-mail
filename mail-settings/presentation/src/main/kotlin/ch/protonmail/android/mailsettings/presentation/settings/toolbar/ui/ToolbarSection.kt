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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailsettings.presentation.R

@Composable
internal fun ToolbarSection(
    title: String,
    description: String,
    actions: List<ActionUiModel>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = ProtonTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = ProtonTheme.typography.bodyMedium,
                    color = ProtonTheme.colors.textWeak
                )
            }

            Box {
                IconButton(
                    onClick = { isExpanded = true },
                    modifier = Modifier.size(ProtonDimens.IconSize.Default)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        tint = ProtonTheme.colors.iconWeak,
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    modifier = Modifier.background(ProtonTheme.colors.backgroundSecondary),
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    modifier = Modifier.size(ProtonDimens.IconSize.Medium),
                                    painter = painterResource(R.drawable.ic_proton_pencil),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Standard))
                                Text(
                                    stringResource(R.string.mail_settings_custom_toolbar_edit_actions),
                                    style = ProtonTheme.typography.bodyLarge,
                                    color = ProtonTheme.colors.textNorm
                                )
                            }
                        },
                        onClick = {
                            isExpanded = false
                            onClick()
                        },
                        contentPadding = PaddingValues(ProtonDimens.Spacing.Standard, 0.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.large,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            actions.forEachIndexed { index, action ->
                ToolbarActionSetItem(action = action)

                if (index != actions.lastIndex) {
                    HorizontalDivider(color = ProtonTheme.colors.backgroundInvertedNorm)
                }
            }
        }
    }
}

