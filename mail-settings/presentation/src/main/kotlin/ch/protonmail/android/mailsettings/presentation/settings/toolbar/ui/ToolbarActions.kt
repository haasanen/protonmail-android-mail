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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.ui.MailDivider
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditOperation
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditState

@Composable
internal fun ToolbarActions(
    state: CustomizeToolbarEditState.Data,
    disclaimer: TextUiModel,
    actions: ToolbarActions.UiActions,
    modifier: Modifier
) {
    val items = state.selectedActions
    val remainingItems = state.remainingActions

    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(
        listState,
        startIndex = 0,
        count = items.size,
        onMove = { from, to ->
            actions.onOperation(CustomizeToolbarEditOperation.ActionMoved(fromIndex = from, toIndex = to))
        }
    )

    ConsumableLaunchedEffect(state.close) {
        actions.onClose()
    }

    ConsumableLaunchedEffect(state.error) {
        actions.onError()
    }

    Column(
        modifier = modifier
            .background(ProtonTheme.colors.backgroundSecondary)
            .padding(ProtonDimens.Spacing.Large)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ToolbarDisclaimer(
            text = disclaimer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.Spacing.Large)
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))

        Text(
            text = stringResource(R.string.mail_settings_custom_toolbar_set_actions),
            style = ProtonTheme.typography.titleMedium,
            color = ProtonTheme.colors.textWeak,
            modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Large)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProtonTheme.shapes.large,
            elevation = CardDefaults.cardElevation(),
            colors = CardDefaults.cardColors().copy(
                containerColor = ProtonTheme.colors.backgroundInvertedSecondary
            )
        ) {
            var lazyColumnPosY by remember { mutableFloatStateOf(0f) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 512.dp) // Fixed height to prevent infinite height issues
                    .onGloballyPositioned { coords ->
                        lazyColumnPosY = coords.localToWindow(Offset.Zero).y
                    }
            ) {
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.action.action.ordinal }
                ) { index, item ->
                    DraggableItem(dragDropState, index) { _ ->
                        var rowPosY by remember { mutableFloatStateOf(0f) }
                        SelectedToolbarActionDisplay(
                            item,
                            reorderButton = {
                                ActionDragHandle(
                                    modifier = Modifier
                                        .pointerInput(dragDropState) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { localBoxOffset ->
                                                    val boxTopInWindow = rowPosY + localBoxOffset.y
                                                    val boxTopInLazyCol = boxTopInWindow - lazyColumnPosY
                                                    dragDropState.onDragStart(Offset(0f, boxTopInLazyCol))
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragDropState.onDrag(dragAmount)
                                                },
                                                onDragEnd = { dragDropState.onDragInterrupted() },
                                                onDragCancel = { dragDropState.onDragInterrupted() }
                                            )
                                        }
                                )
                            },
                            onRemoveClicked = {
                                actions.onOperation(CustomizeToolbarEditOperation.ActionRemoved(item.action.action))
                            },
                            modifier = Modifier
                                .padding(start = ProtonDimens.Spacing.Standard)
                                .fillMaxWidth()
                                .onGloballyPositioned { layoutCoordinates ->
                                    rowPosY = layoutCoordinates.localToWindow(Offset.Zero).y
                                }
                        )
                    }

                    // Hide the item-specific divider during drag, as we'd need to take its index into account as well.
                    if (index != items.size - 1 && dragDropState.draggingItemIndex != index) {
                        MailDivider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

        Text(
            text = stringResource(R.string.mail_settings_custom_toolbar_remaining_actions),
            color = ProtonTheme.colors.textWeak,
            style = ProtonTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Large)
        )

        Column {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ProtonTheme.shapes.large,
                elevation = CardDefaults.cardElevation(),
                colors = CardDefaults.cardColors().copy(
                    containerColor = ProtonTheme.colors.backgroundInvertedSecondary
                )
            ) {
                remainingItems.forEachIndexed { index, item ->
                    UnselectedToolbarActionDisplay(
                        item,
                        onAddClicked = {
                            actions.onOperation(CustomizeToolbarEditOperation.ActionSelected(item.action.action))
                        },
                        modifier = Modifier.padding(start = ProtonDimens.Spacing.Standard)
                    )

                    if (index != remainingItems.size - 1) {
                        MailDivider()
                    }
                }
            }
        }
    }
}

internal object ToolbarActions {
    data class UiActions(
        val onOperation: (CustomizeToolbarEditOperation) -> Unit,
        val onClose: () -> Unit,
        val onError: suspend () -> Unit
    )
}
