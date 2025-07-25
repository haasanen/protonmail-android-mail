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

package ch.protonmail.android.mailmessage.presentation.ui.bottomsheet.snooze

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonButton
import ch.protonmail.android.design.compose.component.ProtonHorizontallyCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonSecondaryButton
import ch.protonmail.android.design.compose.component.protonSecondaryButtonColors
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailmessage.presentation.R
import ch.protonmail.android.mailmessage.presentation.model.snooze.CustomSnoozeUiModel
import ch.protonmail.android.mailmessage.presentation.model.snooze.SnoozeOperationViewAction
import ch.protonmail.android.mailmessage.presentation.model.snooze.SnoozeOptionUiModel
import ch.protonmail.android.mailmessage.presentation.model.snooze.SnoozeOptionsState
import ch.protonmail.android.mailmessage.presentation.model.snooze.SnoozeUntilUiModel
import ch.protonmail.android.mailmessage.presentation.model.snooze.UpgradeToSnoozeUiModel
import ch.protonmail.android.mailmessage.presentation.viewmodel.SnoozeOptionsBottomSheetViewModel

@Composable
fun SnoozeOptionsBottomSheetScreen(
    modifier: Modifier = Modifier,
    viewModel: SnoozeOptionsBottomSheetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle(SnoozeOptionsState.Loading)

    SnoozeOptionsBottomSheetScreen(modifier = modifier, state = state, onEvent = {
        // coming up
    })
}

@Composable
fun SnoozeOptionsBottomSheetScreen(
    modifier: Modifier = Modifier,
    state: SnoozeOptionsState,
    onEvent: (SnoozeOperationViewAction) -> Unit = {}
) {
    when (state) {
        is SnoozeOptionsState.Loading -> ProtonHorizontallyCenteredProgress()
        is SnoozeOptionsState.Data -> {
            Column(
                modifier = modifier
                    .padding(all = ProtonDimens.Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Large)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = ProtonDimens.Spacing.Large),
                    text = stringResource(id = R.string.snooze_sheet_snooze_title),
                    style = ProtonTheme.typography.titleMediumNorm,
                    textAlign = TextAlign.Center
                )

                OptionsGrid(
                    items = state.snoozeOptions
                        .chunked(size = 2)
                        .toMutableList()
                        .apply { add(listOf(state.customSnoozeOption)) },
                    onEvent = onEvent
                )

                if (state.showUnSnooze) {
                    UnsnoozeButton()
                }
            }
        }
    }
}

@Composable
fun OptionsGrid(
    modifier: Modifier = Modifier,
    items: List<List<SnoozeOptionUiModel>>,
    onEvent: (SnoozeOperationViewAction) -> Unit = {}
) {
    items.forEach { rowItems ->
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Medium)
        ) {
            rowItems.forEach { item ->
                when (item) {
                    is SnoozeUntilUiModel -> SnoozeUntilButton(
                        modifier.weight(1f),
                        item.icon,
                        item.title,
                        item.detail
                    ) {
                        onEvent(item.action)
                    }

                    is CustomSnoozeUiModel -> CustomSnoozeButton(modifier) { onEvent(item.action) }
                    is UpgradeToSnoozeUiModel -> UpsellSnoozeButton(modifier) { onEvent(item.action) }
                }
            }
        }
    }
}

@Composable
fun UnsnoozeButton(modifier: Modifier = Modifier, onEvent: () -> Unit = {}) {
    ProtonButton(
        modifier = modifier.fillMaxWidth(),
        onClick = { onEvent() },
        colors = ButtonDefaults.protonSecondaryButtonColors(false),
        elevation = null,
        shape = ProtonTheme.shapes.extraLarge,
        border = null
    )  {
        Text(
            modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Large),
            text = stringResource(R.string.snooze_sheet_option_unsnooze),
            style = ProtonTheme.typography.bodyLargeNorm,
            textAlign = TextAlign.Start,
            maxLines = 1
        )
    }
}

@Composable
fun CustomSnoozeButton(modifier: Modifier = Modifier, onEvent: () -> Unit = {}) {
    ProtonButton(
        modifier = modifier,
        onClick = { onEvent() },
        colors = ButtonDefaults.protonSecondaryButtonColors(false),
        elevation = null,
        shape = ProtonTheme.shapes.extraLarge,
        border = null
    )  {
        Column(
            modifier = modifier
                .padding(all = ProtonDimens.Spacing.Large)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(bottom = ProtonDimens.Spacing.Tiny),
                text = stringResource(R.string.snooze_sheet_option_custom),
                style = ProtonTheme.typography.bodyLargeNorm,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.snooze_sheet_option_custom_detail),
                style = ProtonTheme.typography.bodyMediumWeak,
                maxLines = 1
            )
        }
    }
}

@Composable
fun UpsellSnoozeButton(modifier: Modifier = Modifier, onEvent: () -> Unit = {}) {
    val stroke = Brush.linearGradient(ProtonTheme.colors.upsell)
    ProtonButton(
        modifier = modifier.border(width = 0.5.dp, brush = stroke, shape = ProtonTheme.shapes.extraLarge),
        onClick = { onEvent() },
        colors = ButtonDefaults.protonSecondaryButtonColors(false),
        elevation = null,
        shape = ProtonTheme.shapes.extraLarge,
        border = null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Column(
                modifier = modifier
                    .padding(all = ProtonDimens.Spacing.Large)
                    .weight(1f)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = ProtonDimens.Spacing.Tiny),
                    text = stringResource(R.string.snooze_sheet_option_custom),
                    style = ProtonTheme.typography.bodyLargeNorm,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.snooze_sheet_option_upsell_detail),
                    style = ProtonTheme.typography.bodyMediumWeak,
                    maxLines = 1
                )
            }
            Icon(
                contentDescription = null,
                modifier = Modifier
                    .defaultMinSize(minWidth = ProtonDimens.IconSize.MediumLarge),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_proton_mail_upsell),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun SnoozeUntilButton(
    modifier: Modifier,
    @DrawableRes icon: Int,
    title: TextUiModel,
    detail: TextUiModel,
    onEvent: () -> Unit = {}
) {
    ProtonButton(
        modifier = modifier,
        onClick = { onEvent() },
        colors = ButtonDefaults.protonSecondaryButtonColors(false),
        elevation = null,
        shape = ProtonTheme.shapes.extraLarge,
        border = null
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = ProtonDimens.Spacing.Large)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                contentDescription = null,
                modifier = Modifier
                    .defaultMinSize(minWidth = ProtonDimens.IconSize.MediumLarge)
                    .padding(bottom = ProtonDimens.Spacing.Tiny),
                painter = painterResource(icon)
            )
            Text(
                modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Tiny),
                text = title.string(),
                style = ProtonTheme.typography.bodyLargeNorm,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = detail.string(),
                style = ProtonTheme.typography.bodyMediumWeak,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Upsell Button")
@Composable
fun PreviewUpsellButton() {
    CustomSnoozeButton()
}

@Preview(name = "Unsnooze Button")
@Composable
fun PreviewUnsnoozeButton() {
    UnsnoozeButton()
}

@Preview(name = "Snooze Grid")
@Composable
fun PreviewSnoozeGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Large)) {
        OptionsGrid(
            items = listOf(
                listOf(
                    SnoozeUntilUiModel(
                        action = SnoozeOperationViewAction.SnoozeUntil,
                        R.drawable.ic_pin_angled,
                        TextUiModel("Next Week"),
                        TextUiModel("9 am")
                    ),
                    SnoozeUntilUiModel(
                        action = SnoozeOperationViewAction.SnoozeUntil,
                        R.drawable.ic_pin_angled,
                        TextUiModel("Next Week"),
                        TextUiModel("9 am")
                    )
                ),
                listOf(
                    SnoozeUntilUiModel(
                        action = SnoozeOperationViewAction.SnoozeUntil,
                        R.drawable.ic_pin_angled,
                        TextUiModel("Next Week"),
                        TextUiModel("9 am")
                    ),
                    SnoozeUntilUiModel(
                        action = SnoozeOperationViewAction.SnoozeUntil,
                        R.drawable.ic_pin_angled,
                        TextUiModel("Next Week"),
                        TextUiModel("9 am")
                    )
                )
            )
        )
    }
}


