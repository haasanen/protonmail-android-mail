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

package ch.protonmail.android.mailcontact.presentation.contactlist.ui

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
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactSwipeToDeleteUiModel

@Composable
fun SwipeableContactListItem(contact: ContactListItemUiModel.Contact, actions: ContactListScreen.Actions) {
    val deleteSwipeAction = ContactSwipeToDeleteUiModel(
        icon = R.drawable.ic_proton_trash,
        color = ProtonTheme.colors.notificationError,
        descriptionRes = R.string.action_delete_description
    )

    SwipeableItem(
        swipeActionUiModel = deleteSwipeAction,
        onSwipeToDeleteContact = { actions.onDeleteContactRequest(contact) },
        itemKey = contact.id.id,
        content = {
            ContactListItem(contact = contact, actions = actions)
        }
    )
}

@Composable
fun SwipeableItem(
    modifier: Modifier = Modifier,
    itemKey: Any,
    swipeActionUiModel: ContactSwipeToDeleteUiModel,
    onSwipeToDeleteContact: () -> Unit,
    content: @Composable () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled
    )

    // Reset to settled when item changes
    LaunchedEffect(itemKey) {
        swipeState.snapTo(SwipeToDismissBoxValue.Settled)
    }

    // Haptic Feedback
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(swipeState.targetValue) {
        if (swipeState.targetValue != SwipeToDismissBoxValue.Settled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Handle swipe action when state changes
    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSwipeToDeleteContact()
            // Reset after delete action
            swipeState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        modifier = modifier,
        backgroundContent = {
            DismissBackground(
                dismissState = swipeState,
                swipeActionUiModel = swipeActionUiModel
            )
        },
        content = {
            Box(modifier = Modifier.background(ProtonTheme.colors.backgroundNorm)) {
                content()
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    )
}

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState, swipeActionUiModel: ContactSwipeToDeleteUiModel) {
    val direction = dismissState.dismissDirection
    if (direction == SwipeToDismissBoxValue.Settled) return


    val scale by animateFloatAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
        label = "swipe_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(swipeActionUiModel.color)
            .padding(horizontal = ProtonDimens.Spacing.ExtraLarge),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            modifier = Modifier
                .scale(scale),
            painter = painterResource(id = swipeActionUiModel.icon),
            contentDescription = stringResource(id = swipeActionUiModel.descriptionRes),
            tint = ProtonTheme.colors.iconInverted
        )
    }
}
