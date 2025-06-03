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

package ch.protonmail.android.mailfeatureflags.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonOutlinedButton
import ch.protonmail.android.design.compose.component.ProtonSettingsHeader
import ch.protonmail.android.design.compose.component.ProtonSettingsToggleItem
import ch.protonmail.android.design.compose.component.ProtonSettingsTopBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.ProtonTypography
import ch.protonmail.android.design.compose.theme.bodySmallHint
import ch.protonmail.android.mailfeatureflags.presentation.R
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagListItem
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagOverridesState
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagUiModel
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeatureFlagOverridesScreenContent(
    state: FeatureFlagOverridesState.Loaded,
    actions: FeatureFlagOverridesScreen.Actions
) {
    Scaffold(
        topBar = {
            ProtonSettingsTopBar(
                title = stringResource(R.string.feature_flag_overrides_title),
                onBackClick = actions.onBack
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            items(
                items = state.featureFlagListItems,
                key = { item ->
                    when (item) {
                        is FeatureFlagListItem.Header -> "header-${item.categoryName}"
                        is FeatureFlagListItem.FeatureFlag -> "flag-${item.model.key}"
                    }
                }
            ) { item ->
                when (item) {
                    is FeatureFlagListItem.Header -> ProtonSettingsHeader(title = item.categoryName)
                    is FeatureFlagListItem.FeatureFlag -> {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Large)) {
                                ProtonSettingsToggleItem(
                                    name = item.model.name,
                                    hint = item.model.description,
                                    value = item.model.enabled,
                                    onToggle = { actions.onToggled(item.model.key) },
                                    modifier = Modifier.wrapContentHeight()
                                )

                                if (item.model.overridden) {
                                    Text(
                                        text = stringResource(R.string.feature_flag_overridden_description),
                                        color = ProtonTheme.colors.notificationWarning,
                                        style = ProtonTypography.Default.bodySmallHint,
                                        modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Large)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))
                ProtonOutlinedButton(
                    onClick = actions.onResetAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ProtonDimens.Spacing.Large)
                ) {
                    Text(text = stringResource(R.string.feature_flag_overrides_reset_button))
                }
            }
        }
    }
}

@Preview
@Composable
private fun FeatureFlagScreenPreview() {
    ProtonTheme {
        FeatureFlagOverridesScreenContent(
            state = sampleData,
            actions = FeatureFlagOverridesScreen.Actions.Empty
        )
    }
}

val sampleData = FeatureFlagOverridesState.Loaded(
    featureFlagListItems = listOf(
        FeatureFlagListItem.Header("Composer"),
        FeatureFlagListItem.FeatureFlag(
            FeatureFlagUiModel(
                key = "key-composer",
                name = "Navigation",
                description = "Allow access to all Composer flows.",
                enabled = false,
                overridden = false
            )
        ),
        FeatureFlagListItem.Header("System"),
        FeatureFlagListItem.FeatureFlag(
            FeatureFlagUiModel(
                key = "key-notifs",
                name = "Notifications",
                description = "Enable push notifications.",
                enabled = false,
                overridden = false
            )
        )
    ).toImmutableList()
)
