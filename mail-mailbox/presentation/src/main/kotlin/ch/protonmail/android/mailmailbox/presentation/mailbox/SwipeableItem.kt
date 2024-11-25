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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.SwipeActionsUiModel
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.SwipeThreshold
import me.proton.core.mailsettings.domain.entity.SwipeAction

@Composable
fun SwipeableItem(
    modifier: Modifier = Modifier,
    swipeActionsUiModel: SwipeActionsUiModel?,
    swipeActionCallbacks: SwipeActions.Actions,
    swipingEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    swipeActionsUiModel?.start?.swipeAction?.let {
                        callbackForSwipeAction(it, swipeActionCallbacks)()
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    swipeActionsUiModel?.end?.swipeAction?.let {
                        callbackForSwipeAction(it, swipeActionCallbacks)()
                    }
                }
                else -> false
            }
            return@rememberSwipeToDismissBoxState true
        },
        positionalThreshold = SwipeThreshold.defaultPositionalThreshold()
    )

    // Haptic Feedback
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(key1 = swipeState.dismissDirection) {
        if (swipeState.dismissDirection != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        modifier = modifier,
        backgroundContent = {
            swipeActionsUiModel?.let {
                DismissBackground(
                    dismissState = swipeState,
                    swipeActionsUiModel = swipeActionsUiModel
                )
            }
        },
        content = {
            content()
        },
        enableDismissFromStartToEnd = swipingEnabled && swipeActionsUiModel?.start != null,
        enableDismissFromEndToStart = swipingEnabled && swipeActionsUiModel?.end != null
    )
}

@Suppress("ReturnCount")
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState, swipeActionsUiModel: SwipeActionsUiModel) {
    val direction = dismissState.dismissDirection
    if (direction == SwipeToDismissBoxValue.Settled) return

    val color = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> swipeActionsUiModel.start.getColor()
        SwipeToDismissBoxValue.EndToStart -> swipeActionsUiModel.end.getColor()
        else -> return
    }

    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> return
    }

    val icon = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> swipeActionsUiModel.start.icon
        SwipeToDismissBoxValue.EndToStart -> swipeActionsUiModel.end.icon
        else -> return
    }

    val description = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> swipeActionsUiModel.start.descriptionRes
        SwipeToDismissBoxValue.EndToStart -> swipeActionsUiModel.end.descriptionRes
        else -> return
    }

    val scale by animateFloatAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
        label = "swipe_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = ProtonDimens.Spacing.ExtraLarge),
        contentAlignment = alignment
    ) {
        Icon(
            modifier = Modifier
                .scale(scale),
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = description),
            tint = ProtonTheme.colors.iconInverted
        )
    }
}


fun callbackForSwipeAction(action: SwipeAction, swipeActionCallbacks: SwipeActions.Actions) = when (action) {
    SwipeAction.Trash -> swipeActionCallbacks.onTrash
    SwipeAction.Spam -> swipeActionCallbacks.onSpam
    SwipeAction.Star -> swipeActionCallbacks.onStar
    SwipeAction.Archive -> swipeActionCallbacks.onArchive
    SwipeAction.MarkRead -> swipeActionCallbacks.onMarkRead
}

object SwipeActions {
    data class Actions(
        val onTrash: () -> Unit,
        val onSpam: () -> Unit,
        val onStar: () -> Unit,
        val onArchive: () -> Unit,
        val onMarkRead: () -> Unit
    )
}
