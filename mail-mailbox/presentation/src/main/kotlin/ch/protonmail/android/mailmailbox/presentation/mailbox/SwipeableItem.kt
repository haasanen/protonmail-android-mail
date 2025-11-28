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

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.unit.Density
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.SwipeThreshold
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.SwipeActionsUiModel
import kotlinx.coroutines.flow.filter
import me.proton.core.mailsettings.domain.entity.SwipeAction

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SwipeableItem(
    modifier: Modifier = Modifier,
    swipeActionsUiModel: SwipeActionsUiModel?,
    swipeActionCallbacks: SwipeActions.Actions,
    swipingEnabled: Boolean = true,
    content: @Composable () -> Unit
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val haptic = LocalHapticFeedback.current
    val threshold = SwipeThreshold.SWIPE_THRESHOLD_PERCENTAGE

    // Ensure the action is triggered only once - multiple confirmValueChange calls may happen.
    var actionTriggered by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissBoxValue ->
            if (!actionTriggered) {
                swipeActionsUiModel?.let {
                    when (dismissBoxValue) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            callbackForSwipeAction(it.start.swipeAction, swipeActionCallbacks)()
                            actionTriggered = true
                        }

                        SwipeToDismissBoxValue.EndToStart -> {
                            callbackForSwipeAction(it.end.swipeAction, swipeActionCallbacks)()
                            actionTriggered = true
                        }

                        SwipeToDismissBoxValue.Settled -> Unit
                    }
                }
            }
            false // Always reject - keep the item in place for visual feedback
        },
        positionalThreshold = { _ -> width * threshold },
        enableFling = false
    )

    // Reset action trigger flag when the user finishes the swipe action
    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) {
            actionTriggered = false
        }
    }

    val progressFlow = remember { snapshotFlow { dismissState.progress } }
    var hapticTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        progressFlow
            .filter { it > threshold }
            .collect {
                if (it == 1.0f) {
                    hapticTriggered = false
                } else if (!hapticTriggered) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    hapticTriggered = true
                }
            }
    }

    val enableDismissFromStartToEnd = swipeActionsUiModel?.start?.let {
        it.swipeAction != SwipeAction.None && it.isEnabled
    } ?: false
    val enableDismissFromEndToStart = swipeActionsUiModel?.end?.let {
        it.swipeAction != SwipeAction.None && it.isEnabled
    } ?: false

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        gesturesEnabled = swipingEnabled,
        enableDismissFromStartToEnd = enableDismissFromStartToEnd,
        enableDismissFromEndToStart = enableDismissFromEndToStart,
        backgroundContent = {
            swipeActionsUiModel ?: return@SwipeToDismissBox

            val properties = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> SwipeActionProperties(
                    swipeActionsUiModel.start.getColor(),
                    Alignment.CenterStart,
                    swipeActionsUiModel.start.icon,
                    swipeActionsUiModel.start.descriptionRes
                )

                SwipeToDismissBoxValue.EndToStart -> SwipeActionProperties(
                    swipeActionsUiModel.end.getColor(),
                    Alignment.CenterEnd,
                    swipeActionsUiModel.end.icon,
                    swipeActionsUiModel.end.descriptionRes
                )

                SwipeToDismissBoxValue.Settled -> return@SwipeToDismissBox
            }

            val scale by animateFloatAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> 0.75f
                    SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> 1f
                },
                animationSpec = tween(durationMillis = 500),
                label = "swipe_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(properties.color)
                    .padding(horizontal = ProtonDimens.Spacing.Medium),
                contentAlignment = properties.alignment
            ) {
                Icon(
                    modifier = Modifier
                        .align(properties.alignment)
                        .scale(scale),
                    painter = painterResource(id = properties.icon),
                    contentDescription = stringResource(id = properties.description),
                    tint = ProtonTheme.colors.iconInverted
                )
            }
        }
    ) {
        content()
    }
}

// We're using the deprecated [SwipeToDismissBoxState.Saver] and [SwipeToDismissBoxState] constructors on purpose here.
// There is no proper way to control the velocity threshold that is needed to prevent accidental swipes.
// See https://issuetracker.google.com/issues/252334353
@Composable
@Suppress("Deprecation")
private fun rememberSwipeToDismissBoxState(
    initialValue: SwipeToDismissBoxValue = SwipeToDismissBoxValue.Settled,
    confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true },
    positionalThreshold: (totalDistance: Float) -> Float = SwipeToDismissBoxDefaults.positionalThreshold,
    enableFling: Boolean = false
): SwipeToDismissBoxState {
    val density = if (enableFling) LocalDensity.current else Density(Float.POSITIVE_INFINITY)
    return rememberSaveable(
        saver = SwipeToDismissBoxState.Saver(
            confirmValueChange = confirmValueChange,
            density = density,
            positionalThreshold = positionalThreshold
        )
    ) {
        SwipeToDismissBoxState(initialValue, density, confirmValueChange, positionalThreshold)
    }
}

private data class SwipeActionProperties(
    val color: Color,
    val alignment: Alignment,
    val icon: Int,
    val description: Int
)

private fun callbackForSwipeAction(action: SwipeAction, swipeActionCallbacks: SwipeActions.Actions) = when (action) {
    SwipeAction.None -> swipeActionCallbacks.onNone
    SwipeAction.Trash -> swipeActionCallbacks.onTrash
    SwipeAction.Spam -> swipeActionCallbacks.onSpam
    SwipeAction.Star -> swipeActionCallbacks.onStar
    SwipeAction.Archive -> swipeActionCallbacks.onArchive
    SwipeAction.MarkRead -> swipeActionCallbacks.onMarkRead
    SwipeAction.MoveTo -> swipeActionCallbacks.onMoveTo
    SwipeAction.LabelAs -> swipeActionCallbacks.onLabelAs
}

fun getAccessibilityActionsForTalkback(
    swipingEnabled: Boolean,
    swipeActionsUiModel: SwipeActionsUiModel,
    swipeActions: SwipeActions.Actions,
    context: Context
) = mutableListOf<CustomAccessibilityAction>().apply {
    if (swipingEnabled) {
        if (swipeActionsUiModel.start.isEnabled) {
            add(
                CustomAccessibilityAction(
                    label = context.getString(swipeActionsUiModel.start.descriptionRes)
                ) {
                    callbackForSwipeAction(swipeActionsUiModel.start.swipeAction, swipeActions)()
                    true
                }
            )
        }
        if (swipeActionsUiModel.end.isEnabled) {
            add(
                CustomAccessibilityAction(
                    label = context.getString(swipeActionsUiModel.end.descriptionRes)
                ) {
                    callbackForSwipeAction(swipeActionsUiModel.end.swipeAction, swipeActions)()
                    true
                }
            )
        }
    }
}

object SwipeActions {
    data class Actions(
        val onNone: () -> Unit = {},
        val onTrash: () -> Unit,
        val onSpam: () -> Unit,
        val onStar: () -> Unit,
        val onArchive: () -> Unit,
        val onMarkRead: () -> Unit,
        val onMoveTo: () -> Unit,
        val onLabelAs: () -> Unit
    )
}

private const val DELAY_ANIMATION_VALUE = 200L
