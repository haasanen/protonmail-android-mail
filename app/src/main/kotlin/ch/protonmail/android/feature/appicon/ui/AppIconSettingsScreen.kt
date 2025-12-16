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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.protonmail.android.R
import ch.protonmail.android.design.compose.component.ProtonSettingsDetailsAppBar
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.feature.appicon.AppIconSettingsViewModel
import ch.protonmail.android.feature.appicon.model.AppIconUiModel
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AppIconSettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLearnMoreClick: () -> Unit = {}
) {
    val viewModel: AppIconSettingsViewModel = hiltViewModel()
    val availableIcons = viewModel.getAvailableIcons()

    AppIconSettingsScreen(
        modifier = modifier,
        availableIcons = availableIcons,
        activeIcon = viewModel.getCurrentAppIcon(),
        onIconSelected = { viewModel.setNewAppIcon(it) },
        onBackClick = onBackClick,
        onLearnMoreClick = onLearnMoreClick
    )
}

@Composable
private fun AppIconSettingsScreen(
    modifier: Modifier = Modifier,
    availableIcons: ImmutableList<AppIconUiModel>,
    activeIcon: AppIconUiModel,
    onIconSelected: (AppIconUiModel) -> Unit,
    onBackClick: () -> Unit,
    onLearnMoreClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsDetailsAppBar(
                title = stringResource(id = R.string.settings_app_icon_toolbar_title),
                onBackClick = onBackClick
            )
        },
        containerColor = ProtonTheme.colors.backgroundInvertedNorm,
        content = { paddingValues ->
            AppIconSettingsContent(
                modifier = Modifier.padding(paddingValues),
                availableIcons = availableIcons,
                activeIcon = activeIcon,
                onIconConfirmed = onIconSelected,
                onLearnMoreClick = onLearnMoreClick
            )
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppIconSettingsScreenPreview() {
    ProtonTheme {
        AppIconSettingsScreen(
            availableIcons = AppIconData.ALL_ICONS.map {
                AppIconUiModel(it, R.drawable.ic_proton_pencil, R.string.app_name)
            }.toImmutableList(),
            activeIcon = AppIconUiModel(
                AppIconData.DEFAULT,
                R.mipmap.ic_launcher,
                R.string.app_name
            ),
            onIconSelected = {},
            onBackClick = {},
            onLearnMoreClick = {}
        )
    }
}
