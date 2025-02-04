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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.AdaptivePreviews
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MailboxMoreActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.previewdata.MailboxMoreActionBottomSheetPreviewDataProvider
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeWeak
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailmessage.presentation.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MailboxMoreActionBottomSheetContent(
    modifier: Modifier = Modifier,
    state: MailboxMoreActionsBottomSheetState,
    actionCallbacks: MoreActionBottomSheetContent.Actions
) {
    when (state) {
        is MailboxMoreActionsBottomSheetState.Data ->
            MailboxMoreActionBottomSheetContent(modifier, state, actionCallbacks)

        else -> ProtonCenteredProgress()
    }
}

@Composable
fun MailboxMoreActionBottomSheetContent(
    modifier: Modifier = Modifier,
    state: MailboxMoreActionsBottomSheetState.Data,
    actionCallbacks: MoreActionBottomSheetContent.Actions
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .padding(ProtonDimens.Spacing.Large)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .scrollable(
                    rememberScrollableState(consumeScrollDelta = { 0f }),
                    orientation = Orientation.Vertical
                )
        ) {
            Text(
                modifier = Modifier
                    .background(ProtonTheme.colors.backgroundInvertedNorm)
                    .align(Alignment.CenterHorizontally),
                text = pluralStringResource(
                    id = R.plurals.selected_count_title,
                    state.selectedCount,
                    state.selectedCount
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ProtonTheme.typography.titleLargeNorm,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            ActionGroup(
                actionUiModels = state.hiddenActionUiModels,
                actionCallbacks = actionCallbacks
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            ActionGroup(
                actionUiModels = state.visibleActionUiModels,
                actionCallbacks = actionCallbacks
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            ActionGroup(
                actionUiModels = persistentListOf(state.customizeToolbarActionUiModel),
                actionCallbacks = actionCallbacks
            )
        }
    }
}


@Composable
private fun ActionGroup(
    modifier: Modifier = Modifier,
    actionUiModels: ImmutableList<ActionUiModel>,
    actionCallbacks: MoreActionBottomSheetContent.Actions
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            actionUiModels.forEachIndexed { index, action ->
                ActionGroupItem(
                    action = action,
                    onClick = callbackForAction(action.action, actionCallbacks)
                )

                if (index < actionUiModels.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(0.dp),
                        thickness = 1.dp,
                        color = ProtonTheme.colors.separatorNorm
                    )
                }
            }
        }
    }
}

@Composable
internal fun ActionGroupItem(
    modifier: Modifier = Modifier,
    action: ActionUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                vertical = ProtonDimens.Spacing.Large,
                horizontal = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .testTag(MoreActionsBottomSheetTestTags.ActionItem)
                .padding(end = ProtonDimens.Spacing.Large),
            painter = painterResource(id = action.icon),
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            modifier = Modifier
                .testTag(MoreActionsBottomSheetTestTags.LabelIcon)
                .weight(1f),
            text = action.description.string(),
            style = ProtonTheme.typography.bodyLargeWeak,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun callbackForAction(action: Action, actionCallbacks: MoreActionBottomSheetContent.Actions): () -> Unit =
    when (action) {
        Action.Star -> actionCallbacks.onStar
        Action.Unstar -> actionCallbacks.onUnStar
        Action.Archive -> actionCallbacks.onArchive
        Action.Spam -> actionCallbacks.onSpam
        Action.Label -> actionCallbacks.onLabel
        Action.MarkRead -> actionCallbacks.onMarkRead
        Action.MarkUnread -> actionCallbacks.onMarkUnread
        Action.Trash -> actionCallbacks.onTrash
        Action.Delete -> actionCallbacks.onDelete
        Action.Move -> actionCallbacks.onMoveTo
        Action.Inbox -> actionCallbacks.onInbox
        Action.CustomizeToolbar -> actionCallbacks.onCustomizeToolbar
        else -> {
            {}
        }
    }

object MoreActionBottomSheetContent {

    data class Actions(
        val onStar: () -> Unit,
        val onUnStar: () -> Unit,
        val onArchive: () -> Unit,
        val onSpam: () -> Unit,
        val onLabel: () -> Unit,
        val onMarkRead: () -> Unit,
        val onMarkUnread: () -> Unit,
        val onTrash: () -> Unit,
        val onDelete: () -> Unit,
        val onMoveTo: () -> Unit,
        val onInbox: () -> Unit,
        val onCustomizeToolbar: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onStar = {},
                onUnStar = {},
                onArchive = {},
                onSpam = {},
                onLabel = {},
                onMarkRead = {},
                onMarkUnread = {},
                onTrash = {},
                onDelete = {},
                onMoveTo = {},
                onInbox = {},
                onCustomizeToolbar = {}
            )
        }
    }
}

@Composable
@AdaptivePreviews
private fun MoreActionBottomSheetContentPreview(
    @PreviewParameter(MailboxMoreActionBottomSheetPreviewDataProvider::class)
    state: MailboxMoreActionsBottomSheetState.Data
) {
    ProtonTheme {
        MailboxMoreActionBottomSheetContent(state = state, actionCallbacks = MoreActionBottomSheetContent.Actions.Empty)
    }
}

object MoreActionsBottomSheetTestTags {

    const val ActionItem = "MoreActionsBottomSheetActionItem"
    const val LabelIcon = "MoreActionsBottomSheetLabelIcon"

}
