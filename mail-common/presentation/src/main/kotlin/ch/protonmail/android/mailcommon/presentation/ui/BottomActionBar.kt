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

package ch.protonmail.android.mailcommon.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.ProtonTypography
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.AdaptivePreviews
import ch.protonmail.android.mailcommon.presentation.R
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.model.BottomBarState
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailcommon.presentation.previewdata.BottomActionBarPreviewProvider
import timber.log.Timber

@Composable
fun BottomActionBar(
    state: BottomBarState,
    viewActionCallbacks: BottomActionBar.Actions,
    modifier: Modifier = Modifier
) {
    if (state is BottomBarState.Data.Hidden) return
    Column(
        modifier = modifier
            .shadow(
                elevation = ProtonDimens.ShadowElevation.Large,
                ambientColor = ProtonTheme.colors.shadowSoft,
                spotColor = ProtonTheme.colors.shadowSoft
            )
            .background(ProtonTheme.colors.backgroundNorm)
    ) {
        HorizontalDivider(thickness = MailDimens.SeparatorHeight, color = ProtonTheme.colors.separatorNorm)

        Row(
            modifier = Modifier
                .testTag(BottomActionBarTestTags.RootItem)
                .clickable(
                    enabled = false,
                    onClick = {
                        // this is needed otherwise the click event is passed down the view hierarchy
                    }
                )
                .fillMaxWidth()
                .padding(vertical = ProtonDimens.Spacing.Standard),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            when (state) {
                is BottomBarState.Loading -> {
                    ProtonCenteredProgress(
                        modifier = Modifier
                            .padding(vertical = ProtonDimens.Spacing.Standard)
                            .size(MailDimens.ProgressDefaultSize)
                    )
                }

                is BottomBarState.Error -> Text(
                    modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Standard),
                    text = stringResource(id = R.string.common_error_loading_actions),
                    style = ProtonTypography.Default.bodyLargeNorm
                )

                is BottomBarState.Data.Shown -> {
                    state.actions.forEachIndexed { index, uiModel ->
                        if (index.exceedsMaxActionsShowed()) {
                            return@forEachIndexed
                        }
                        BottomBarIcon(
                            modifier = Modifier.testTag("${BottomActionBarTestTags.Button}$index"),
                            iconId = uiModel.icon,
                            description = uiModel.contentDescription,
                            onClick = callbackForAction(uiModel.action, viewActionCallbacks)
                        )
                    }
                }

                else -> {
                    // no-op, Hidden state is handled at the beginning
                }
            }
        }
    }
}

@SuppressWarnings("ComplexMethod")
fun callbackForAction(action: Action, viewActionCallbacks: BottomActionBar.Actions) = when (action) {
    Action.MarkRead -> viewActionCallbacks.onMarkRead
    Action.MarkUnread -> viewActionCallbacks.onMarkUnread
    Action.Star -> viewActionCallbacks.onStar
    Action.Unstar -> viewActionCallbacks.onUnstar
    Action.Label -> viewActionCallbacks.onLabel
    Action.Move -> viewActionCallbacks.onMove
    Action.Trash -> viewActionCallbacks.onTrash
    Action.Delete -> viewActionCallbacks.onDelete
    Action.Archive -> viewActionCallbacks.onArchive
    Action.Spam -> viewActionCallbacks.onSpam
    Action.ViewInLightMode -> viewActionCallbacks.onViewInLightMode
    Action.ViewInDarkMode -> viewActionCallbacks.onViewInDarkMode
    Action.Print -> viewActionCallbacks.onPrint
    Action.ViewHeaders -> viewActionCallbacks.onViewHeaders
    Action.ViewHtml -> viewActionCallbacks.onViewHtml
    Action.ReportPhishing -> viewActionCallbacks.onReportPhishing
    Action.Remind -> viewActionCallbacks.onRemind
    Action.SavePdf -> viewActionCallbacks.onSavePdf
    Action.SenderEmails -> viewActionCallbacks.onSenderEmail
    Action.SaveAttachments -> viewActionCallbacks.onSaveAttachments
    Action.More -> viewActionCallbacks.onMore
    Action.Inbox -> viewActionCallbacks.onMoveToInbox
    Action.CustomizeToolbar -> viewActionCallbacks.onCustomizeToolbar
    Action.Pin,
    Action.Unpin,
    Action.Reply,
    Action.ReplyAll,
    Action.Forward -> {
        { Timber.d("Action not handled for BottomActionBar - $action.") }
    }
}

@Composable
private fun Int.exceedsMaxActionsShowed() = this > BottomActionBar.MAX_ACTIONS_COUNT

@Composable
private fun BottomBarIcon(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    description: TextUiModel,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier,
            painter = painterResource(id = iconId),
            contentDescription = description.string(),
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

object BottomActionBar {

    internal const val MAX_ACTIONS_COUNT = 5

    data class Actions(
        val onMarkRead: () -> Unit,
        val onMarkUnread: () -> Unit,
        val onStar: () -> Unit,
        val onUnstar: () -> Unit,
        val onMove: () -> Unit,
        val onLabel: () -> Unit,
        val onTrash: () -> Unit,
        val onDelete: () -> Unit,
        val onArchive: () -> Unit,
        val onSpam: () -> Unit,
        val onMoveToInbox: () -> Unit,
        val onViewInLightMode: () -> Unit,
        val onViewInDarkMode: () -> Unit,
        val onPrint: () -> Unit,
        val onViewHeaders: () -> Unit,
        val onViewHtml: () -> Unit,
        val onReportPhishing: () -> Unit,
        val onRemind: () -> Unit,
        val onSavePdf: () -> Unit,
        val onSenderEmail: () -> Unit,
        val onSaveAttachments: () -> Unit,
        val onMore: () -> Unit,
        val onCustomizeToolbar: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onMarkRead = {},
                onMarkUnread = {},
                onStar = {},
                onUnstar = {},
                onMove = {},
                onLabel = {},
                onTrash = {},
                onDelete = {},
                onArchive = {},
                onSpam = {},
                onMoveToInbox = {},
                onViewInLightMode = {},
                onViewInDarkMode = {},
                onPrint = {},
                onViewHeaders = {},
                onViewHtml = {},
                onReportPhishing = {},
                onRemind = {},
                onSavePdf = {},
                onSenderEmail = {},
                onSaveAttachments = {},
                onMore = {},
                onCustomizeToolbar = {}
            )
        }
    }

}

@Composable
@AdaptivePreviews
private fun BottomActionPreview(@PreviewParameter(BottomActionBarPreviewProvider::class) state: BottomBarState) {
    ProtonTheme {
        BottomActionBar(state = state, viewActionCallbacks = BottomActionBar.Actions.Empty)
    }
}

object BottomActionBarTestTags {

    const val RootItem = "BottomActionBarRootItem"
    const val Button = "BottomActionBarIcon"
}
