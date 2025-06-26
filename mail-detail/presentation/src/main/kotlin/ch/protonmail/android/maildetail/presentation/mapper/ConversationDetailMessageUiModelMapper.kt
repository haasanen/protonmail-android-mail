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

package ch.protonmail.android.maildetail.presentation.mapper

import androidx.compose.ui.graphics.Color
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.mapper.ExpirationTimeMapper
import ch.protonmail.android.mailcommon.presentation.usecase.FormatShortTime
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.presentation.model.LabelUiModel
import ch.protonmail.android.mailmessage.domain.model.AvatarImageState
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBanner
import ch.protonmail.android.mailmessage.domain.model.MessageTheme
import ch.protonmail.android.mailmessage.presentation.mapper.AvatarImageUiModelMapper
import ch.protonmail.android.mailmessage.presentation.model.ViewModePreference
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@Suppress("LongParameterList")
class ConversationDetailMessageUiModelMapper @Inject constructor(
    private val messageIdUiModelMapper: MessageIdUiModelMapper,
    private val avatarUiModelMapper: DetailAvatarUiModelMapper,
    private val expirationTimeMapper: ExpirationTimeMapper,
    private val colorMapper: ColorMapper,
    private val formatShortTime: FormatShortTime,
    private val messageLocationUiModelMapper: MessageLocationUiModelMapper,
    private val messageDetailHeaderUiModelMapper: MessageDetailHeaderUiModelMapper,
    private val messageDetailFooterUiModelMapper: MessageDetailFooterUiModelMapper,
    private val messageBannersUiModelMapper: MessageBannersUiModelMapper,
    private val messageBodyUiModelMapper: MessageBodyUiModelMapper,
    private val participantUiModelMapper: ParticipantUiModelMapper,
    private val avatarImageUiModelMapper: AvatarImageUiModelMapper
) {

    fun toUiModel(
        message: Message,
        avatarImageState: AvatarImageState,
        primaryUserAddress: String?
    ): ConversationDetailMessageUiModel.Collapsed {
        return ConversationDetailMessageUiModel.Collapsed(
            avatar = avatarUiModelMapper(message.isDraft, message.avatarInformation, message.sender),
            avatarImage = avatarImageUiModelMapper.toUiModel(avatarImageState),
            expiration = message.expirationTimeOrNull()?.let(expirationTimeMapper::toUiModel),
            forwardedIcon = getForwardedIcon(isForwarded = message.isForwarded),
            hasAttachments = message.numAttachments > message.attachmentCount.calendar,
            isStarred = message.isStarred,
            isUnread = message.isUnread,
            locationIcon = messageLocationUiModelMapper(message.exclusiveLocation),
            repliedIcon = getRepliedIcon(isReplied = message.isReplied, isRepliedAll = message.isRepliedAll),
            sender = participantUiModelMapper.senderToUiModel(message.sender),
            shortTime = formatShortTime(message.time.seconds),
            labels = toLabelUiModels(message.customLabels),
            messageId = messageIdUiModelMapper.toUiModel(message.messageId),
            isDraft = message.isDraft,
            recipients = (message.toList + message.ccList + message.bccList).map {
                participantUiModelMapper.recipientToUiModel(it, primaryUserAddress)
            }.toImmutableList(),
            shouldShowUndisclosedRecipients = message.hasUndisclosedRecipients()
        )
    }

    suspend fun toUiModel(
        message: Message,
        avatarImageState: AvatarImageState,
        primaryUserAddress: String?,
        decryptedMessageBody: DecryptedMessageBody
    ): ConversationDetailMessageUiModel.Expanded {
        return ConversationDetailMessageUiModel.Expanded(
            messageId = messageIdUiModelMapper.toUiModel(message.messageId),
            isUnread = message.isUnread,
            messageDetailHeaderUiModel = messageDetailHeaderUiModelMapper.toUiModel(
                message,
                primaryUserAddress,
                avatarImageState,
                decryptedMessageBody.transformations.messageThemeOptions?.themeOverride.toViewModePreference()
            ),
            messageDetailFooterUiModel = messageDetailFooterUiModelMapper.toUiModel(message),
            messageBannersUiModel = messageBannersUiModelMapper.toUiModel(decryptedMessageBody.banners),
            requestPhishingLinkConfirmation = decryptedMessageBody.banners.contains(MessageBanner.PhishingAttempt),
            messageBodyUiModel = messageBodyUiModelMapper.toUiModel(decryptedMessageBody)
        )
    }

    fun toUiModel(
        messageUiModel: ConversationDetailMessageUiModel.Expanded,
        message: Message,
        avatarImageState: AvatarImageState
    ): ConversationDetailMessageUiModel.Expanded {
        return messageUiModel.copy(
            isUnread = message.isUnread,
            messageDetailHeaderUiModel = messageDetailHeaderUiModelMapper.toUiModel(
                message,
                null,
                avatarImageState,
                messageUiModel.messageBodyUiModel.viewModePreference
            )
        )
    }

    fun toUiModel(collapsed: ConversationDetailMessageUiModel.Collapsed): ConversationDetailMessageUiModel.Expanding {
        return ConversationDetailMessageUiModel.Expanding(
            collapsed = collapsed,
            messageId = collapsed.messageId
        )
    }

    fun toUiModel(message: Message): ConversationDetailMessageUiModel.Hidden {
        return ConversationDetailMessageUiModel.Hidden(
            messageId = messageIdUiModelMapper.toUiModel(message.messageId),
            isUnread = message.isUnread
        )
    }

    private fun getForwardedIcon(isForwarded: Boolean): ConversationDetailMessageUiModel.ForwardedIcon = when {
        isForwarded -> ConversationDetailMessageUiModel.ForwardedIcon.Forwarded
        else -> ConversationDetailMessageUiModel.ForwardedIcon.None
    }

    private fun getRepliedIcon(
        isReplied: Boolean,
        isRepliedAll: Boolean
    ): ConversationDetailMessageUiModel.RepliedIcon = when {
        isRepliedAll -> ConversationDetailMessageUiModel.RepliedIcon.RepliedAll
        isReplied -> ConversationDetailMessageUiModel.RepliedIcon.Replied
        else -> ConversationDetailMessageUiModel.RepliedIcon.None
    }

    private fun toLabelUiModels(labels: List<Label>): ImmutableList<LabelUiModel> =
        labels.filter { it.type == LabelType.MessageLabel }.map { label ->
            LabelUiModel(
                name = label.name,
                color = colorMapper.toColor(label.color).getOrElse { Color.Unspecified },
                id = label.labelId.id
            )
        }.toImmutableList()

    private fun Message.hasUndisclosedRecipients() = (toList + ccList + bccList).isEmpty()
}

fun MessageTheme?.toViewModePreference(): ViewModePreference {
    return when (this) {
        MessageTheme.Light -> ViewModePreference.LightMode
        MessageTheme.Dark -> ViewModePreference.DarkMode
        null -> ViewModePreference.ThemeDefault
    }
}
