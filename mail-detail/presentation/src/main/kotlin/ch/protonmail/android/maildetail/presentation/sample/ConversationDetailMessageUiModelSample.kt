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

package ch.protonmail.android.maildetail.presentation.sample

import java.util.UUID
import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.presentation.model.AvatarImageUiModel
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maildetail.presentation.model.ExpirationBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageBannersUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageDetailFooterUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageLocationUiModel
import ch.protonmail.android.maildetail.presentation.model.ParticipantUiModel
import ch.protonmail.android.maillabel.domain.sample.LabelSample
import ch.protonmail.android.maillabel.presentation.model.LabelUiModel
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.sample.MessageSample
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.presentation.sample.AttachmentMetadataUiModelSamples
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

object ConversationDetailMessageUiModelSample {

    val AugWeatherForecast = buildCollapsed(
        message = MessageSample.AugWeatherForecast
    )

    val AugWeatherForecastExpanded = buildExpanded(
        message = MessageSample.AugWeatherForecast
    )

    val AugWeatherForecastExpanding = buildExpanding(AugWeatherForecast)

    val EmptyDraft = buildCollapsed(
        message = MessageSample.EmptyDraft,
        avatar = AvatarUiModel.DraftIcon
    )

    val ExpiringInvitation = buildCollapsed(
        message = MessageSample.ExpiringInvitation,
        expiration = TextUiModel("12h")
    )

    val InvoiceForwarded = buildCollapsed(
        message = MessageSample.Invoice,
        forwardedIcon = ConversationDetailMessageUiModel.ForwardedIcon.Forwarded
    )

    val InvoiceReplied = buildCollapsed(
        message = MessageSample.Invoice,
        repliedIcon = ConversationDetailMessageUiModel.RepliedIcon.Replied
    )

    val InvoiceRepliedAll = buildCollapsed(
        message = MessageSample.Invoice,
        repliedIcon = ConversationDetailMessageUiModel.RepliedIcon.RepliedAll
    )

    val LotteryScam = buildCollapsed(
        message = MessageSample.LotteryScam
    )

    val SepWeatherForecast = buildCollapsed(
        message = MessageSample.SepWeatherForecast
    )

    val StarredInvoice = buildCollapsed(
        message = MessageSample.Invoice,
        isStarred = true
    )

    val UnreadInvoice = buildCollapsed(
        message = MessageSample.UnreadInvoice
    )

    val InvoiceWithTwoLabels = buildCollapsed(
        message = MessageSample.Invoice.copy(
            customLabels = listOf(
                LabelSample.Document,
                LabelSample.Label2021,
                LabelSample.Label2022
            )
        )
    )

    val InvoiceWithLabel = buildCollapsed(
        message = MessageSample.Invoice.copy(
            customLabels = listOf(
                LabelSample.Document,
                LabelSample.Label2021
            )
        )
    )

    val InvoiceWithLabelExpanded = buildExpanded(
        message = MessageSample.Invoice
    )

    val InvoiceWithLabelExpanding = buildExpanding(
        collapsed = InvoiceWithLabel
    )

    val InvoiceWithLabelExpandingUnread = buildExpanding(
        collapsed = InvoiceWithLabel.copy(isUnread = true)
    )

    val InvoiceWithoutLabels = buildCollapsed(
        message = MessageSample.Invoice
    )

    val InvoiceWithoutLabelsCustomFolderExpanded = buildExpanded(
        message = MessageSample.Invoice,
        locationUiModel = MessageLocationUiModelSample.CustomFolder
    )

    val AnotherInvoiceWithoutLabels = buildCollapsed(
        message = MessageSample.Invoice
    )

    val MessageWithRemoteContentBlocked = buildExpanded(
        message = MessageSample.LotteryScam,
        messageBodyUiModel = MessageDetailBodyUiModelSample.withBlockedRemoteContent
    )

    val MessageWithRemoteContentLoaded = buildExpanded(
        message = MessageSample.LotteryScam,
        messageBodyUiModel = MessageDetailBodyUiModelSample.withAllowedRemoteContent
    )

    val MessageWithEmbeddedImagesBlocked = buildExpanded(
        message = MessageSample.AugWeatherForecast,
        messageBodyUiModel = MessageDetailBodyUiModelSample.withBlockedEmbeddedImages
    )

    val MessageWithEmbeddedImagesLoaded = buildExpanded(
        message = MessageSample.AugWeatherForecast,
        messageBodyUiModel = MessageDetailBodyUiModelSample.withAllowedEmbeddedImages
    )

    val WithRemoteAndEmbeddedContentBlocked = buildExpanded(
        message = MessageSample.Invoice,
        messageBodyUiModel = MessageDetailBodyUiModelSample.withBlockedContent
    )

    val WithRemoteAndEmbeddedContentLoaded = buildExpanded(
        message = MessageSample.Invoice,
        messageBodyUiModel = MessageDetailBodyUiModelSample.withAllowedContent
    )

    fun invoiceExpandedWithAttachments(limit: Int) = buildExpanded(
        message = MessageSample.Invoice,
        messageBodyUiModel = MessageDetailBodyUiModelSample.build(
            messageBody = "Invoice",
            attachments = AttachmentGroupUiModel(
                limit = limit,
                attachments = listOf(
                    AttachmentMetadataUiModelSamples.Document,
                    AttachmentMetadataUiModelSamples.DocumentWithReallyLongFileName,
                    AttachmentMetadataUiModelSamples.Invoice,
                    AttachmentMetadataUiModelSamples.Image
                ),
                expandCollapseMode = AttachmentListExpandCollapseMode.Collapsed
            )
        )
    )

    @Suppress("LongParameterList")
    private fun buildCollapsed(
        message: Message = MessageSample.build(),
        avatar: AvatarUiModel = AvatarUiModel.ParticipantAvatar(
            initial = message.avatarInformation.initials,
            address = message.sender.address,
            bimiSelector = message.sender.bimiSelector,
            color = Color.Unspecified
        ),
        expiration: TextUiModel? = null,
        forwardedIcon: ConversationDetailMessageUiModel.ForwardedIcon =
            ConversationDetailMessageUiModel.ForwardedIcon.None,
        repliedIcon: ConversationDetailMessageUiModel.RepliedIcon = ConversationDetailMessageUiModel.RepliedIcon.None,
        isStarred: Boolean = false
    ): ConversationDetailMessageUiModel.Collapsed = ConversationDetailMessageUiModel.Collapsed(
        avatar = avatar,
        avatarImage = AvatarImageUiModel.NoImageAvailable,
        expiration = expiration,
        forwardedIcon = forwardedIcon,
        hasAttachments = message.numAttachments > message.attachmentCount.calendar,
        isStarred = isStarred,
        isUnread = message.isUnread,
        locationIcon = MessageLocationUiModelSample.AllMail,
        repliedIcon = repliedIcon,
        sender = ParticipantUiModel(message.sender.name, message.sender.address, R.drawable.ic_proton_lock, false),
        shortTime = TextUiModel("10:00"),
        labels = emptyList<LabelUiModel>().toImmutableList(),
        messageId = MessageIdUiModel(message.messageId.id),
        isDraft = false,
        recipients = emptyList<ParticipantUiModel>().toImmutableList(),
        shouldShowUndisclosedRecipients = (message.toList + message.ccList + message.bccList).isEmpty()
    )

    private fun buildExpanded(
        message: Message = MessageSample.build(),
        avatar: AvatarUiModel = AvatarUiModel.ParticipantAvatar(
            initial = message.avatarInformation.initials,
            address = message.sender.address,
            bimiSelector = message.sender.bimiSelector,
            color = Color.Unspecified
        ),
        isStarred: Boolean = false,
        messageBodyUiModel: MessageBodyUiModel = MessageDetailBodyUiModelSample.build(UUID.randomUUID().toString()),
        locationUiModel: MessageLocationUiModel = MessageLocationUiModelSample.AllMail
    ): ConversationDetailMessageUiModel.Expanded = ConversationDetailMessageUiModel.Expanded(
        messageId = MessageIdUiModel(message.messageId.id),
        isUnread = message.isUnread,
        messageDetailHeaderUiModel = MessageDetailHeaderUiModelSample.build(
            avatar = avatar,
            sender = ParticipantUiModel(
                participantName = message.sender.name,
                participantAddress = message.sender.address,
                participantPadlock = 0,
                shouldShowOfficialBadge = false
            ),
            isStarred = isStarred,
            location = locationUiModel,
            time = TextUiModel("10:00"),
            extendedTime = TextUiModel("10:00"),
            allRecipients = emptyList<ParticipantUiModel>().toImmutableList(),
            toRecipients = emptyList<ParticipantUiModel>().toImmutableList(),
            ccRecipients = emptyList<ParticipantUiModel>().toImmutableList(),
            bccRecipients = emptyList<ParticipantUiModel>().toImmutableList(),
            labels = persistentListOf()
        ),
        messageDetailFooterUiModel = MessageDetailFooterUiModel(
            messageId = MessageIdUiModel(message.messageId.id),
            shouldShowReplyAll = false
        ),
        messageBannersUiModel = MessageBannersUiModel(
            shouldShowPhishingBanner = true,
            shouldShowSpamBanner = false,
            shouldShowBlockedSenderBanner = false,
            expirationBannerUiModel = ExpirationBannerUiModel.NoExpiration
        ),
        messageBodyUiModel = messageBodyUiModel,
        requestPhishingLinkConfirmation = false
    )

    private fun buildExpanding(
        collapsed: ConversationDetailMessageUiModel.Collapsed
    ): ConversationDetailMessageUiModel.Expanding = ConversationDetailMessageUiModel.Expanding(
        messageId = collapsed.messageId,
        collapsed = collapsed
    )
}
