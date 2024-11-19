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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.compose.SmallNonClickableIcon
import ch.protonmail.android.mailcommon.presentation.extension.isItemRead
import ch.protonmail.android.mailcommon.presentation.extension.tintColor
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maillabel.presentation.model.LabelUiModel
import ch.protonmail.android.maillabel.presentation.ui.LabelsList
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemLocationUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ParticipantsUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata.MailboxItemUiModelPreviewData
import ch.protonmail.android.mailmessage.presentation.ui.ParticipantAvatar
import kotlinx.collections.immutable.ImmutableList
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MailboxItem(
    modifier: Modifier = Modifier,
    actions: ComposeMailboxItem.Actions,
    item: MailboxItemUiModel,
    selectionMode: Boolean = false,
    isSelected: Boolean = false
) {
    Row(
        modifier = modifier
            .combinedClickable(
                onClick = { actions.onItemClicked(item) },
                onLongClick = { actions.onItemLongClicked(item) }
            )
            .semantics { isItemRead = item.isRead }
            .padding(
                start = ProtonDimens.Spacing.ModeratelyLarge,
                end = ProtonDimens.Spacing.Large,
                top = ProtonDimens.Spacing.ModeratelyLarge,
                bottom = ProtonDimens.Spacing.ModeratelyLarge
            )
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.large)
    ) {
        val fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold
        val fontColor = if (item.isRead) ProtonTheme.colors.textWeak else ProtonTheme.colors.textNorm
        val iconColor = if (item.isRead) ProtonTheme.colors.iconWeak else ProtonTheme.colors.iconNorm

        ParticipantAvatar(
            modifier = Modifier.align(Alignment.CenterVertically),
            avatarUiModel = if (selectionMode) AvatarUiModel.SelectionMode(isSelected) else item.avatar,
            onClick = { actions.onAvatarClicked(item) }
        )
        Column(
            modifier = Modifier.padding(
                start = ProtonDimens.Spacing.ModeratelyLarge,
                top = ProtonDimens.Spacing.Standard
            )
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ActionIcons(
                    item = item,
                    iconColor = fontColor,
                    modifier = Modifier.padding(end = ProtonDimens.Spacing.Small)
                )
                Participants(
                    participants = item.participants,
                    fontWeight = fontWeight,
                    fontColor = fontColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = ProtonDimens.Spacing.Small)
                )
                Time(time = item.time, fontWeight = fontWeight, fontColor = fontColor)
            }
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LocationIcons(
                    iconResIds = item.locations,
                    iconColor = fontColor,
                    modifier = Modifier.padding(end = ProtonDimens.Spacing.Small)
                )
                Row(
                    Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Subject(
                        subject = item.subject,
                        fontWeight = fontWeight,
                        fontColor = fontColor,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = ProtonDimens.Spacing.Small)
                    )
                    Count(
                        count = item.numMessages,
                        fontWeight = fontWeight,
                        fontColor = fontColor,
                        iconColor = iconColor
                    )
                }
                Icons(
                    item = item,
                    iconColor = fontColor,
                    modifier = Modifier.padding(start = ProtonDimens.Spacing.Small)
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = ProtonDimens.Spacing.Small, bottom = ProtonDimens.Spacing.Small)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpirationLabel(
                    hasExpirationTime = item.shouldShowExpirationLabel,
                    modifier = Modifier.padding(end = ProtonDimens.Spacing.Small)
                )
                Labels(labels = item.labels)
            }
        }
    }
}

@Composable
private fun ActionIcons(
    modifier: Modifier = Modifier,
    item: MailboxItemUiModel,
    iconColor: Color
) {
    val someIconShown = item.shouldShowRepliedIcon || item.shouldShowRepliedAllIcon || item.shouldShowForwardedIcon
    if (!someIconShown) {
        return
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.shouldShowRepliedIcon) {
            SmallNonClickableIcon(R.drawable.ic_proton_arrow_up_and_left, iconColor = iconColor)
        }
        if (item.shouldShowRepliedAllIcon) {
            SmallNonClickableIcon(R.drawable.ic_proton_arrows_up_and_left, iconColor = iconColor)
        }
        if (item.shouldShowForwardedIcon) {
            SmallNonClickableIcon(R.drawable.ic_proton_arrow_right, iconColor = iconColor)
        }
    }
}

@Composable
private fun Participants(
    modifier: Modifier = Modifier,
    participants: ParticipantsUiModel,
    fontWeight: FontWeight,
    fontColor: Color
) {
    when (participants) {
        is ParticipantsUiModel.Participants -> {
            ParticipantsList(
                modifier = modifier.wrapContentSize(),
                participants = participants,
                fontWeight = fontWeight,
                fontColor = fontColor
            )
        }

        is ParticipantsUiModel.NoParticipants -> {
            Text(
                modifier = modifier.testTag(ParticipantsListTestTags.NoParticipant),
                text = participants.message.string(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = ProtonTheme.typography.bodyLargeNorm.copy(fontWeight = fontWeight, color = fontColor)
            )
        }
    }
}

@Composable
private fun Time(
    modifier: Modifier = Modifier,
    time: TextUiModel,
    fontWeight: FontWeight,
    fontColor: Color
) {
    Text(
        modifier = modifier.testTag(MailboxItemTestTags.Date),
        text = time.string(),
        maxLines = 1,
        textAlign = TextAlign.End,
        style = ProtonTheme.typography.bodySmallNorm.copy(fontWeight = fontWeight, color = fontColor)
    )
}

@Composable
private fun LocationIcons(
    modifier: Modifier = Modifier,
    iconResIds: ImmutableList<MailboxItemLocationUiModel>,
    iconColor: Color
) {
    if (iconResIds.isEmpty()) {
        return
    }

    Row(
        modifier = modifier.testTag(MailboxItemTestTags.LocationIcons),
        horizontalArrangement = Arrangement.Start
    ) {
        iconResIds.forEach {
            SmallNonClickableIcon(
                modifier = Modifier.semantics { tintColor = it.color },
                iconId = it.icon,
                iconColor = it.color ?: iconColor
            )
        }
    }
}

@Composable
private fun Subject(
    modifier: Modifier = Modifier,
    subject: String,
    fontWeight: FontWeight,
    fontColor: Color
) {
    Text(
        modifier = modifier.testTag(MailboxItemTestTags.Subject),
        text = subject,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = ProtonTheme.typography.bodyMediumNorm.copy(fontWeight = fontWeight, color = fontColor)
    )
}

@Composable
private fun Count(
    modifier: Modifier = Modifier,
    count: Int?,
    fontWeight: FontWeight,
    fontColor: Color,
    iconColor: Color
) {
    if (count == null) {
        return
    }

    val stroke = BorderStroke(MailDimens.DefaultBorder, iconColor)
    Box(
        modifier = modifier
            .border(stroke, ProtonTheme.shapes.small)
    ) {
        Text(
            modifier = Modifier
                .testTag(MailboxItemTestTags.Count)
                .padding(horizontal = ProtonDimens.Spacing.Small),
            text = count.toString(),
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.bodySmallNorm.copy(fontWeight = fontWeight, color = fontColor)
        )
    }
}

@Composable
private fun Icons(
    modifier: Modifier = Modifier,
    item: MailboxItemUiModel,
    iconColor: Color
) {
    if (item.hasIconsToShow().not()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        if (item.shouldShowCalendarIcon) {
            SmallNonClickableIcon(iconId = R.drawable.ic_proton_calendar_grid, iconColor = iconColor)
        }
        if (item.shouldShowAttachmentIcon) {
            SmallNonClickableIcon(iconId = R.drawable.ic_proton_paper_clip, iconColor = iconColor)
        }
        if (item.showStar) {
            SmallNonClickableIcon(iconId = R.drawable.ic_proton_star_filled, tintId = R.color.notification_warning)
        }
    }
}

@Composable
private fun ExpirationLabel(modifier: Modifier = Modifier, hasExpirationTime: Boolean) {
    if (hasExpirationTime) {
        Box(
            modifier = modifier
                .background(ProtonTheme.colors.interactionWeakNorm, ProtonTheme.shapes.large)
                .size(ProtonDimens.SmallIconSize)
                .padding(ProtonDimens.Spacing.Tiny),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_hourglass),
                tint = ProtonTheme.colors.iconNorm,
                contentDescription = NO_CONTENT_DESCRIPTION
            )
        }
    }
}

@Composable
private fun Labels(modifier: Modifier = Modifier, labels: ImmutableList<LabelUiModel>) {
    LabelsList(
        modifier = modifier.testTag(MailboxItemTestTags.LabelsList),
        labels = labels
    )
}

object ComposeMailboxItem {
    data class Actions(
        val onItemClicked: (MailboxItemUiModel) -> Unit,
        val onItemLongClicked: (MailboxItemUiModel) -> Unit,
        val onAvatarClicked: (MailboxItemUiModel) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onAvatarClicked = {},
                onItemLongClicked = {},
                onItemClicked = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun DroidConMailboxItemPreview() {
    ProtonTheme {
        MailboxItem(
            modifier = Modifier,
            item = MailboxItemUiModelPreviewData.Conversation.DroidConLondon,
            actions = ComposeMailboxItem.Actions.Empty
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun DroidConWithoutCountMailboxItemPreview() {
    ProtonTheme {
        MailboxItem(
            modifier = Modifier,
            item = MailboxItemUiModelPreviewData.Conversation.DroidConLondonWithZeroMessages,
            actions = ComposeMailboxItem.Actions.Empty
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun WeatherMailboxItemPreview() {
    ProtonTheme {
        MailboxItem(
            modifier = Modifier,
            item = MailboxItemUiModelPreviewData.Conversation.WeatherForecast,
            actions = ComposeMailboxItem.Actions.Empty
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun LongRecipientItemPreview() {
    ProtonTheme {
        MailboxItem(
            modifier = Modifier,
            item = MailboxItemUiModelPreviewData.Conversation.MultipleRecipientWithLabel,
            actions = ComposeMailboxItem.Actions.Empty
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun LongSubjectItemPreview() {
    ProtonTheme {
        MailboxItem(
            modifier = Modifier,
            item = MailboxItemUiModelPreviewData.Conversation.LongSubjectWithIcons,
            actions = ComposeMailboxItem.Actions.Empty
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun LongSubjectWithIconItemPreview() {
    ProtonTheme {
        MailboxItem(
            modifier = Modifier,
            item = MailboxItemUiModelPreviewData.Conversation.LongSubjectWithoutIcons,
            actions = ComposeMailboxItem.Actions.Empty
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun NoRecipientIconItemPreview() {
    ProtonTheme {
        MailboxItem(
            modifier = Modifier,
            item = MailboxItemUiModelPreviewData.Conversation.NoParticipant,
            actions = ComposeMailboxItem.Actions.Empty
        )
    }
}

object MailboxItemTestTags {

    const val ItemRow = "MailboxItemRow"
    const val LocationIcons = "LocationIcons"
    const val LabelsList = "LabelsList"
    const val Subject = "Subject"
    const val Date = "Date"
    const val Count = "Count"
}
