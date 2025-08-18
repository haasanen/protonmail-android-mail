/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.design.compose.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.R
import ch.protonmail.android.design.compose.component.appbar.ProtonMediumTopAppBar
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyLargeWeak
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.textNorm

/**
 * A full-size [LazyColumn] list styled with [ProtonTheme]
 * which displays the given content
 */
@Composable
fun ProtonSettingsList(modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {
    Surface(
        color = ProtonTheme.colors.backgroundNorm,
        contentColor = ProtonTheme.colors.textNorm,
        modifier = modifier.fillMaxSize()
    ) {
        val state = rememberLazyListState()

        LazyColumn(
            state = state,
            content = content
        )
    }
}

/**
 * A [TopAppBar] styled with [ProtonTheme] to be used in settings screens
 * By default, shows a back icon and the given title
 * @param onBackClick callback to handle back icon click
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtonSettingsTopBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    onBackClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    ProtonMediumTopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = { Text(title) },
        actions = actions,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.presentation_back))
            }
        },
        scrollBehavior = scrollBehavior
    )
}

/**
 * A [TopAppBar] styled with [ProtonTheme] to be used in settings screens
 * By default, shows a back icon and the given title
 * @param onBackClick callback to handle back icon click
 */
@Composable
fun ProtonSettingsDetailsAppBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    onBackClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = { Text(title) },
        actions = actions,
        backgroundColor = ProtonTheme.colors.backgroundInvertedNorm,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.presentation_back))
            }
        }
    )
}

@Composable
fun ProtonSettingsHeader(modifier: Modifier = Modifier, @StringRes title: Int) {
    ProtonSettingsHeader(
        modifier = modifier,
        title = stringResource(id = title)
    )
}

@Composable
fun ProtonSettingsHeader(modifier: Modifier = Modifier, title: String) {
    ProtonRawListItem(modifier = modifier.padding(ProtonDimens.Spacing.Large)) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = title,
            color = ProtonTheme.colors.textWeak,
            style = ProtonTheme.typography.titleMedium
        )
    }
}

@Composable
fun ProtonMainSettingsItem(
    modifier: Modifier = Modifier,
    name: String,
    nameColor: Color = ProtonTheme.colors.textNorm,
    @DrawableRes iconRes: Int,
    iconColor: Color = ProtonTheme.colors.textNorm,
    iconBackgroundColor: Color = ProtonTheme.colors.backgroundNorm,
    isClickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    ProtonMainSettingsItem(
        modifier = modifier,
        name = name,
        nameColor = nameColor,
        icon = {
            ProtonMainSettingsIcon(
                iconRes = iconRes,
                contentDescription = name,
                tint = iconColor,
                backgroundColor = iconBackgroundColor
            )
        },
        isClickable = isClickable,
        onClick = onClick
    )
}

@Composable
fun ProtonMainSettingsItem(
    modifier: Modifier = Modifier,
    name: String,
    nameColor: Color = ProtonTheme.colors.textWeak,
    icon: @Composable () -> Unit,
    hint: @Composable () -> Unit = {},
    isClickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(isClickable, onClick = onClick)
            .padding(ProtonDimens.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(ProtonDimens.IconSize.MediumLarge),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Column(
            modifier = Modifier
                .padding(start = ProtonDimens.Spacing.Large)
                .semantics(mergeDescendants = true) {}
                .weight(1f)
        ) {
            Text(
                modifier = Modifier,
                text = name,
                color = nameColor,
                style = ProtonTheme.typography.bodyLargeWeak
            )
            hint()
        }

        Icon(
            modifier = Modifier
                .padding(start = ProtonDimens.Spacing.Large),
            painter = painterResource(id = R.drawable.ic_proton_chevron_right),
            contentDescription = name,
            tint = ProtonTheme.colors.iconDisabled
        )
    }
}


@Composable
fun ProtonAppSettingsItemInvert(
    modifier: Modifier = Modifier,
    name: String,
    icon: @Composable () -> Unit,
    hint: String? = null,
    isClickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    ProtonAppSettingsItem(
        modifier = modifier,
        name = name,
        icon = icon,
        hint = hint,
        isClickable = isClickable,
        onClick = onClick,
        nameTextStyle = ProtonTheme.typography.bodyMediumWeak,
        nameTextColor = ProtonTheme.colors.textWeak,
        hintTextStyle = ProtonTheme.typography.bodyLargeNorm,
        hintTextColor = ProtonTheme.colors.textNorm
    )
}

@Composable
fun ProtonAppSettingsItemNorm(
    modifier: Modifier = Modifier,
    name: String,
    icon: @Composable () -> Unit,
    hint: String? = null,
    isClickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    ProtonAppSettingsItem(
        modifier = modifier,
        name = name,
        icon = icon,
        hint = hint,
        isClickable = isClickable,
        onClick = onClick,
        nameTextStyle = ProtonTheme.typography.bodyLargeNorm,
        nameTextColor = ProtonTheme.colors.textNorm,
        hintTextStyle = ProtonTheme.typography.bodyMediumWeak,
        hintTextColor = ProtonTheme.colors.textWeak
    )
}

@Composable
private fun ProtonAppSettingsItem(
    modifier: Modifier = Modifier,
    name: String,
    icon: @Composable () -> Unit,
    hint: String? = null,
    isClickable: Boolean = true,
    onClick: () -> Unit = {},
    nameTextStyle: TextStyle,
    nameTextColor: Color,
    hintTextStyle: TextStyle,
    hintTextColor: Color
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(isClickable, onClick = onClick)
            .padding(horizontal = ProtonDimens.Spacing.Large, vertical = ProtonDimens.Spacing.Standard),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier
                .padding(vertical = ProtonDimens.Spacing.Compact)
                .semantics(mergeDescendants = true) {}
                .weight(1f)
        ) {
            Text(
                modifier = Modifier,
                text = name,
                color = nameTextColor,
                style = nameTextStyle
            )
            hint?.let {
                Text(
                    modifier = Modifier.padding(top = ProtonDimens.Spacing.Small),
                    text = hint,
                    color = hintTextColor,
                    style = hintTextStyle
                )
            }

        }

        Box(
            modifier = Modifier
                .padding(start = ProtonDimens.Spacing.Large)
                .size(ProtonDimens.IconSize.Default),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
fun ProtonMainSettingsIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    tint: Color = ProtonTheme.colors.textNorm,
    backgroundColor: Color = ProtonTheme.colors.backgroundNorm
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(backgroundColor, shape = ProtonTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun ProtonSettingsItem(
    modifier: Modifier = Modifier,
    name: String,
    hint: String? = null,
    isClickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    ProtonRawListItem(
        modifier = modifier
            .clickable(isClickable, onClick = onClick)
            .padding(
                vertical = ProtonDimens.ListItemTextStartPadding,
                horizontal = ProtonDimens.Spacing.Large
            )
    ) {
        Column(
            modifier = Modifier
                .semantics(mergeDescendants = true) {}
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier,
                text = name,
                color = ProtonTheme.colors.textNorm,
                style = ProtonTheme.typography.bodyLargeNorm
            )
            hint?.let {
                Text(
                    modifier = Modifier.padding(top = ProtonDimens.Spacing.Small),
                    text = hint,
                    color = ProtonTheme.colors.textHint,
                    style = ProtonTheme.typography.bodyMediumNorm
                )
            }
        }
    }
}

@Composable
fun ProtonSettingsToggleItem(
    modifier: Modifier = Modifier,
    name: String,
    hint: String? = null,
    value: Boolean?,
    onToggle: (Boolean) -> Unit = {}
) {
    val isSwitchChecked = value ?: false
    val isViewEnabled = value != null

    Column(modifier = modifier.fillMaxWidth()) {
        ProtonRawListItem(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    color = ProtonTheme.colors.textNorm(isViewEnabled),
                    style = ProtonTheme.typography.titleMedium
                )
                VerticalSpacer(height = ProtonDimens.Spacing.Small)
                hint?.let {
                    Text(
                        text = hint,
                        color = ProtonTheme.colors.textWeak,
                        style = ProtonTheme.typography.bodyMediumNorm
                    )
                }
            }

            Switch(
                checked = isSwitchChecked,
                onCheckedChange = if (isViewEnabled) {
                    { onToggle(it) }
                } else {
                    null
                },
                enabled = isViewEnabled
            )
        }
    }
}

@Composable
fun ProtonSettingsRadioItem(
    modifier: Modifier = Modifier,
    name: String,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onItemSelected: () -> Unit
) {
    ProtonRawListItem(
        modifier = modifier
            .selectable(
                selected = isSelected,
                enabled = isEnabled,
                role = Role.RadioButton
            ) {
                if (isEnabled) onItemSelected()
            }
            .sizeIn(minHeight = ProtonDimens.ListItemHeight)
            .padding(horizontal = ProtonDimens.Spacing.Large),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            color = if (isEnabled) ProtonTheme.colors.textNorm else ProtonTheme.colors.textDisabled,
            style = ProtonTheme.typography.bodyLargeNorm
        )
        RadioButton(
            selected = isSelected,
            enabled = isEnabled,
            onClick = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Proton settings top bar light mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "Proton settings top bar dark mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun SettingsTopBarPreview() {
    ProtonSettingsTopBar(title = "Setting", onBackClick = {})
}

@Preview(
    name = "Proton settings item with name and hint",
    showBackground = true
)
@Composable
fun SettingsItemPreview() {
    ProtonSettingsItem(name = "Setting name", hint = "This settings does nothing")
}

@Preview(
    name = "Proton main settings item with name and icon",
    showBackground = true
)
@Composable
fun MainSettingsItemPreview() {
    ProtonMainSettingsItem(iconRes = R.drawable.ic_proton_storage, name = "Setting name")
}

@Preview(
    name = "Proton settings toggleable item",
    showBackground = true
)
@Composable
fun SettingsToggleableItemPreview() {
    ProtonSettingsToggleItem(name = "Setting toggle", value = true)
}

@Preview(
    name = "Proton settings toggleable item disabled",
    showBackground = true
)
@Composable
fun DisabledSettingsToggleableItemPreview() {
    ProtonSettingsToggleItem(name = "Setting toggle", value = null)
}

@Preview(
    name = "Proton settings toggleable item with hint",
    showBackground = true
)
@Composable
fun SettingsToggleableItemWithHintPreview() {
    ProtonSettingsToggleItem(
        name = "Setting toggle",
        hint = "Use this space to provide an explanation of what toggling this setting does",
        value = true
    )
}

@Preview(
    name = "Proton settings item with name only",
    showBackground = true
)
@Composable
fun SettingsItemWithNameOnlyPreview() {
    ProtonSettingsItem(name = "Setting name")
}

@Preview(
    name = "Proton settings radio item",
    showBackground = true
)
@Composable
fun SettingsRadioItemPreview() {
    ProtonSettingsRadioItem(
        name = "Setting option",
        isSelected = true,
        onItemSelected = { }
    )
}

@Preview(
    name = "Settings screen light mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "Settings screen dark mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun SettingsPreview() {
    ProtonSettingsList {
        item { ProtonSettingsHeader(title = "Account settings") }
        item { ProtonSettingsItem(name = "Test user", hint = "testuser@proton.ch") {} }
    }
}
