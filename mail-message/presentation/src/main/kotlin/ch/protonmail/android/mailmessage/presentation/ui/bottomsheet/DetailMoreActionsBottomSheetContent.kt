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

package ch.protonmail.android.mailmessage.presentation.ui.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber

@Composable
fun DetailMoreActionsBottomSheetContent(
    state: DetailMoreActionsBottomSheetState,
    actions: DetailMoreActionsBottomSheetContent.Actions
) {
    when (state) {
        is DetailMoreActionsBottomSheetState.Data -> DetailMoreActionsBottomSheetContent(
            uiModel = state.detailDataUiModel,
            replyActions = state.replyActions,
            messageActions = state.messageActions,
            moveActions = state.moveActions,
            genericActions = state.genericActions,
            customizeToolbarActionUiModel = state.customizeToolbarActionUiModel,
            actionCallbacks = actions
        )

        else -> ProtonCenteredProgress()
    }
}

@Composable
fun DetailMoreActionsBottomSheetContent(
    uiModel: DetailMoreActionsBottomSheetState.DetailDataUiModel,
    replyActions: ImmutableList<ActionUiModel>,
    messageActions: ImmutableList<ActionUiModel>,
    moveActions: ImmutableList<ActionUiModel>,
    genericActions: ImmutableList<ActionUiModel>,
    customizeToolbarActionUiModel: ActionUiModel?,
    actionCallbacks: DetailMoreActionsBottomSheetContent.Actions
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        SheetTitle(
            modifier = Modifier.fillMaxWidth(),
            headerSubjectText = uiModel.headerSubjectText,
            onCloseSheetClicked = actionCallbacks.onCloseSheet
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ProtonDimens.Spacing.Large
                )
                .verticalScroll(rememberScrollState())
        ) {

            if (uiModel.messageIdInConversation != null) {
                QuickReplyRow(replyActions) { actionUiModel ->
                    callbackForAction(actionUiModel.action, actionCallbacks).invoke(
                        MessageId(uiModel.messageIdInConversation)
                    )
                }
            }

            ActionGroup(
                modifier = Modifier.padding(top = ProtonDimens.Spacing.Large),
                items = messageActions,
                onItemClicked = { actionUiModel ->
                    resolveCallbackForAction(uiModel.messageIdInConversation, actionUiModel.action, actionCallbacks)
                }
            ) { action, onClick ->
                ActionGroupItem(
                    icon = action.icon,
                    description = action.description.string(),
                    contentDescription = action.contentDescription.string(),
                    onClick = onClick
                )
            }

            ActionGroup(
                modifier = Modifier.padding(top = ProtonDimens.Spacing.Large),
                items = moveActions,
                onItemClicked = { actionUiModel ->
                    resolveCallbackForAction(uiModel.messageIdInConversation, actionUiModel.action, actionCallbacks)
                }
            ) { action, onClick ->
                ActionGroupItem(
                    icon = action.icon,
                    description = action.description.string(),
                    contentDescription = action.contentDescription.string(),
                    onClick = onClick
                )
            }

            ActionGroup(
                modifier = Modifier.padding(top = ProtonDimens.Spacing.Large),
                items = genericActions
                    .toImmutableList(),
                onItemClicked = { actionUiModel ->
                    resolveCallbackForAction(uiModel.messageIdInConversation, actionUiModel.action, actionCallbacks)
                }
            ) { action, onClick ->
                ActionGroupItem(
                    icon = action.icon,
                    description = action.description.string(),
                    contentDescription = action.contentDescription.string(),
                    onClick = onClick
                )
            }

            if (customizeToolbarActionUiModel != null) {
                ActionGroup(
                    modifier = Modifier.padding(top = ProtonDimens.Spacing.Large),
                    items = persistentListOf(customizeToolbarActionUiModel),
                    onItemClicked = { actionUiModel ->
                        callbackForConversation(actionUiModel.action, actionCallbacks).invoke()
                    }
                ) { action, onClick ->
                    ActionGroupItem(
                        icon = action.icon,
                        description = action.description.string(),
                        contentDescription = action.contentDescription.string(),
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(
    modifier: Modifier,
    headerSubjectText: TextUiModel,
    onCloseSheetClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ProtonDimens.Spacing.Small,
                vertical = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
            text = headerSubjectText.string(),
            style = ProtonTheme.typography.titleLargeNorm,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            modifier = Modifier.size(ProtonDimens.DefaultButtonMinHeight),
            onClick = onCloseSheetClicked
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                tint = ProtonTheme.colors.iconNorm,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun QuickReplyRow(replyActions: ImmutableList<ActionUiModel>, onActionClicked: (ActionUiModel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Standard)
    ) {
        for (action in replyActions) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = action.description.string(),
                icon = action.icon,
                onClick = { onActionClicked(action) }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ProtonTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .height(MailDimens.DetailsMoreQuickReplyButtonSize)
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = ProtonTheme.colors.iconNorm
        )
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))
        Text(
            text = label,
            style = ProtonTheme.typography.bodyMediumWeak,
            maxLines = 1
        )
    }
}

private fun resolveCallbackForAction(
    messageIdInconversation: String?,
    action: Action,
    actionCallbacks: DetailMoreActionsBottomSheetContent.Actions
) {
    if (messageIdInconversation != null) {
        callbackForAction(action, actionCallbacks).invoke(
            MessageId(messageIdInconversation)
        )
    } else {
        callbackForConversation(action, actionCallbacks).invoke()
    }
}

private fun callbackForConversation(
    action: Action,
    actionCallbacks: DetailMoreActionsBottomSheetContent.Actions
): () -> Unit = when (action) {
    Action.MarkRead -> actionCallbacks.onMarkReadConversation
    Action.MarkUnread -> actionCallbacks.onMarkUnreadConversation
    Action.Star -> actionCallbacks.onStarConversation
    Action.Unstar -> actionCallbacks.onUnStarConversation
    Action.Label -> actionCallbacks.onLabelConversation
    Action.Trash -> actionCallbacks.onMoveToTrashConversation
    Action.Archive -> actionCallbacks.onMoveToArchiveConversation
    Action.Inbox -> actionCallbacks.onMoveToInboxConversation
    Action.Delete -> actionCallbacks.onDeleteConversation
    Action.Spam -> actionCallbacks.onMoveToSpamConversation
    Action.Move -> actionCallbacks.onMoveConversation
    Action.Print -> actionCallbacks.onPrintConversation
    Action.SavePdf -> actionCallbacks.onSaveConversationAsPdf
    Action.CustomizeToolbar -> actionCallbacks.onCustomizeToolbar
    Action.Snooze -> actionCallbacks.onSnooze
    else -> {
        { Timber.d("Action not handled $action.") }
    }
}

private fun callbackForAction(
    action: Action,
    actionCallbacks: DetailMoreActionsBottomSheetContent.Actions
): (MessageId) -> Unit = when (action) {
    Action.Reply -> actionCallbacks.onReply
    Action.ReplyAll -> actionCallbacks.onReplyAll
    Action.Forward -> actionCallbacks.onForward
    Action.MarkUnread -> actionCallbacks.onMarkUnread
    Action.Star -> actionCallbacks.onStarMessage
    Action.Unstar -> actionCallbacks.onUnStarMessage
    Action.Label -> actionCallbacks.onLabel
    Action.ViewInLightMode -> actionCallbacks.onViewInLightMode
    Action.ViewInDarkMode -> actionCallbacks.onViewInDarkMode
    Action.Trash -> actionCallbacks.onMoveToTrash
    Action.Delete -> actionCallbacks.onDelete
    Action.Archive -> actionCallbacks.onMoveToArchive
    Action.Spam -> actionCallbacks.onMoveToSpam
    Action.Inbox -> actionCallbacks.onMoveToInbox
    Action.Move -> actionCallbacks.onMove
    Action.Print -> actionCallbacks.onPrint
    Action.ReportPhishing -> actionCallbacks.onReportPhishing
    Action.SavePdf -> actionCallbacks.onSaveMessageAsPdf

    else -> {
        { Timber.d("Action not handled $action.") }
    }
}

object DetailMoreActionsBottomSheetContent {

    data class Actions(
        val onReply: (MessageId) -> Unit,
        val onReplyAll: (MessageId) -> Unit,
        val onForward: (MessageId) -> Unit,
        val onMarkUnread: (MessageId) -> Unit,
        val onStarMessage: (MessageId) -> Unit,
        val onUnStarMessage: (MessageId) -> Unit,
        val onLabel: (MessageId) -> Unit,
        val onViewInLightMode: (MessageId) -> Unit,
        val onViewInDarkMode: (MessageId) -> Unit,
        val onMoveToTrash: (MessageId) -> Unit,
        val onMoveToArchive: (MessageId) -> Unit,
        val onDelete: (MessageId) -> Unit,
        val onMoveToSpam: (MessageId) -> Unit,
        val onMoveToInbox: (MessageId) -> Unit,
        val onMove: (MessageId) -> Unit,
        val onPrint: (MessageId) -> Unit,
        val onReportPhishing: (MessageId) -> Unit,
        val onSaveMessageAsPdf: (MessageId) -> Unit,
        val onMarkReadConversation: () -> Unit,
        val onMarkUnreadConversation: () -> Unit,
        val onLabelConversation: () -> Unit,
        val onMoveToTrashConversation: () -> Unit,
        val onMoveToInboxConversation: () -> Unit,
        val onMoveToArchiveConversation: () -> Unit,
        val onDeleteConversation: () -> Unit,
        val onMoveToSpamConversation: () -> Unit,
        val onStarConversation: () -> Unit,
        val onUnStarConversation: () -> Unit,
        val onMoveConversation: () -> Unit,
        val onPrintConversation: () -> Unit,
        val onCloseSheet: () -> Unit,
        val onCustomizeToolbar: () -> Unit,
        val onSaveConversationAsPdf: () -> Unit,
        val onSnooze: () -> Unit
    )
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetContentPreview() {
    ProtonTheme {
        DetailMoreActionsBottomSheetContent(
            state = DetailMoreActionsBottomSheetState.Data(
                detailDataUiModel = DetailMoreActionsBottomSheetState.DetailDataUiModel(
                    TextUiModel("Kudos on a Successful Completion of a Challenging Project!"),
                    "123"
                ),
                replyActions = persistentListOf(
                    ActionUiModel(Action.Reply),
                    ActionUiModel(Action.ReplyAll),
                    ActionUiModel(Action.Forward)
                ),
                messageActions = persistentListOf(
                    ActionUiModel(Action.Delete),
                    ActionUiModel(Action.Spam),
                    ActionUiModel(Action.Inbox),
                    ActionUiModel(Action.Archive)
                ),
                moveActions = persistentListOf(
                    ActionUiModel(Action.Move),
                    ActionUiModel(Action.Trash)
                ),
                genericActions = persistentListOf(
                    ActionUiModel(Action.ReportPhishing),
                    ActionUiModel(Action.Print)
                ),
                customizeToolbarActionUiModel = null
            ),
            actions = DetailMoreActionsBottomSheetContent.Actions(
                onReply = {},
                onReplyAll = {},
                onForward = {},
                onMarkUnread = {},
                onStarMessage = {},
                onUnStarMessage = {},
                onSaveMessageAsPdf = {},
                onLabel = {},
                onViewInLightMode = {},
                onViewInDarkMode = {},
                onMoveToTrash = {},
                onMoveToArchive = {},
                onDelete = {},
                onMoveToSpam = {},
                onMoveToInbox = {},
                onMove = {},
                onPrint = {},
                onReportPhishing = {},
                onMarkReadConversation = {},
                onMarkUnreadConversation = {},
                onLabelConversation = {},
                onMoveToTrashConversation = {},
                onMoveToInboxConversation = {},
                onMoveToArchiveConversation = {},
                onMoveToSpamConversation = {},
                onMoveConversation = {},
                onDeleteConversation = {},
                onUnStarConversation = {},
                onStarConversation = {},
                onPrintConversation = {},
                onCloseSheet = {},
                onCustomizeToolbar = {},
                onSaveConversationAsPdf = {},
                onSnooze = {}
            )
        )
    }
}
