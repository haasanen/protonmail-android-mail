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

package ch.protonmail.android.mailmailbox.presentation.mailbox.mapper

import androidx.compose.ui.graphics.Color
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.usecase.FormatShortTime
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.presentation.model.LabelUiModel
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.usecase.GetParticipantsResolvedNames
import ch.protonmail.android.mailmailbox.domain.usecase.ParticipantsResolvedNamesResult
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ParticipantUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ParticipantsUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.usecase.GetMailboxItemLocationIcon
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailsettings.domain.model.FolderColorSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.domain.arch.Mapper
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class MailboxItemUiModelMapper @Inject constructor(
    private val mailboxAvatarUiModelMapper: MailboxAvatarUiModelMapper,
    private val colorMapper: ColorMapper,
    private val formatMailboxItemTime: FormatShortTime,
    private val getMailboxItemLocationIcon: GetMailboxItemLocationIcon,
    private val getParticipantsResolvedNames: GetParticipantsResolvedNames,
    private val expiryInformationUiModelMapper: ExpiryInformationUiModelMapper,
    private val attachmentMetadataUiModelMapper: AttachmentMetadataUiModelMapper
) : Mapper<MailboxItem, MailboxItemUiModel> {

    suspend fun toUiModel(
        userId: UserId,
        mailboxItem: MailboxItem,
        folderColorSettings: FolderColorSettings,
        isShowingSearchResults: Boolean
    ): MailboxItemUiModel {
        val participantsResolvedNamesResult = getParticipantsResolvedNames(userId, mailboxItem)

        return MailboxItemUiModel(
            avatar = mailboxAvatarUiModelMapper(mailboxItem),
            type = mailboxItem.type,
            userId = userId.id,
            id = mailboxItem.id,
            conversationId = mailboxItem.conversationId,
            time = formatMailboxItemTime(mailboxItem.time.seconds),
            isRead = mailboxItem.read,
            labels = toLabelUiModels(mailboxItem.labels),
            subject = mailboxItem.subject,
            participants = participantsResolvedNamesResult.toParticipantsUiModel(),
            shouldShowRepliedIcon = shouldShowRepliedIcon(mailboxItem),
            shouldShowRepliedAllIcon = shouldShowRepliedAllIcon(mailboxItem),
            shouldShowForwardedIcon = shouldShowForwardedIcon(mailboxItem),
            numMessages = mailboxItem.numMessages.takeIf { it >= 2 },
            isStarred = mailboxItem.isStarred,
            locations = getLocationIconsToDisplay(userId, mailboxItem, folderColorSettings, isShowingSearchResults),
            expiryInformation = expiryInformationUiModelMapper.toUiModel(mailboxItem.expirationTime),
            shouldShowCalendarIcon = hasCalendarAttachment(mailboxItem),
            shouldOpenInComposer = mailboxItem.isDraft,
            attachments = mailboxItem.attachments.map(attachmentMetadataUiModelMapper::toUiModel).toImmutableList()
        )
    }

    private suspend fun getLocationIconsToDisplay(
        userId: UserId,
        mailboxItem: MailboxItem,
        folderColorSettings: FolderColorSettings,
        isShowingSearchResults: Boolean
    ) = when (
        val iconResult = getMailboxItemLocationIcon(
            userId,
            mailboxItem, folderColorSettings, isShowingSearchResults
        )
    ) {
        is GetMailboxItemLocationIcon.Result.None -> emptyList()
        is GetMailboxItemLocationIcon.Result.Icon -> listOfNotNull(iconResult.icon)
    }.toImmutableList()

    private fun hasCalendarAttachment(mailboxItem: MailboxItem) = mailboxItem.calendarAttachmentCount > 0

    private fun shouldShowRepliedIcon(mailboxItem: MailboxItem): Boolean {
        if (mailboxItem.type == MailboxItemType.Conversation) {
            return false
        }

        return if (mailboxItem.isRepliedAll) {
            false
        } else {
            mailboxItem.isReplied
        }
    }

    private fun shouldShowRepliedAllIcon(mailboxItem: MailboxItem) =
        if (mailboxItem.type == MailboxItemType.Conversation) {
            false
        } else {
            mailboxItem.isRepliedAll
        }

    private fun shouldShowForwardedIcon(mailboxItem: MailboxItem) =
        if (mailboxItem.type == MailboxItemType.Conversation) {
            false
        } else {
            mailboxItem.isForwarded
        }

    private fun toLabelUiModels(labels: List<Label>): ImmutableList<LabelUiModel> =
        labels.filter { it.type == LabelType.MessageLabel }.map { label ->
            LabelUiModel(
                name = label.name,
                color = colorMapper.toColor(label.color).getOrElse { Color.Unspecified },
                id = label.labelId.id
            )
        }.toImmutableList()

    private fun ParticipantsResolvedNamesResult.toParticipantsUiModel(): ParticipantsUiModel {
        return if (this.list.any { it.name.isNotBlank() }) {
            ParticipantsUiModel.Participants(
                this.list.map { ParticipantUiModel(it.name, it.isProton) }
                    .toImmutableList()
            )
        } else {
            when (this) {
                is ParticipantsResolvedNamesResult.Recipients -> ParticipantsUiModel.NoParticipants(
                    TextUiModel(R.string.mailbox_default_recipient)
                )

                is ParticipantsResolvedNamesResult.Senders -> ParticipantsUiModel.NoParticipants(
                    TextUiModel(R.string.mailbox_default_sender)
                )
            }
        }
    }
}
