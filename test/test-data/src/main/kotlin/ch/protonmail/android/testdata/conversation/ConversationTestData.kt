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

package ch.protonmail.android.testdata.conversation

import ch.protonmail.android.mailattachments.domain.model.AttachmentCount
import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.domain.model.Sender
import ch.protonmail.android.testdata.user.UserIdTestData.userId
import me.proton.core.domain.entity.UserId

object ConversationTestData {

    const val RAW_CONVERSATION_ID = "rawConversationId"
    const val RAW_SUBJECT = "Here's a new email"

    val conversation = buildConversation(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        labelIds = listOf(SystemLabelId.Inbox.labelId.id),
        numMessages = 1
    )

    val conversationWithArchiveLabel = buildConversation(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        labelIds = listOf(SystemLabelId.Archive.labelId.id),
        numMessages = 1
    )

    val conversationWithInformation = buildConversation(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        labelIds = listOf(SystemLabelId.Inbox.labelId.id),
        numMessages = 1,
        numAttachments = 5,
        numUnRead = 6
    )

    val conversationWith3Messages = buildConversation(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        numMessages = 3
    )

    val starredConversation = buildConversation(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        labelIds = listOf(
            SystemLabelId.Inbox.labelId.id,
            SystemLabelId.Starred.labelId.id
        ),
        numMessages = 1,
        isStarred = true
    )

    val trashAndSpamConversation = buildConversation(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        labelIds = listOf(
            SystemLabelId.Trash.labelId.id,
            SystemLabelId.Spam.labelId.id,
            SystemLabelId.AllMail.labelId.id
        )
    )

    val trashConversationWithAllSentAllDrafts = buildConversation(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        labelIds = listOf(
            SystemLabelId.Trash.labelId.id,
            SystemLabelId.AllSent.labelId.id,
            SystemLabelId.AllDrafts.labelId.id
        )
    )

    val conversationWithConversationLabels = buildConversationWithConversationLabels(
        userId = userId,
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        labels = listOf(buildLabel(RAW_CONVERSATION_ID), buildLabel(RAW_CONVERSATION_ID))
    )

    private fun buildConversation(
        userId: UserId,
        id: String,
        subject: String,
        numMessages: Int = 1,
        labelIds: List<String> = listOf("0"),
        numAttachments: Int = 0,
        expirationTime: Long = 0,
        attachmentCount: AttachmentCount = AttachmentCount(0),
        numUnRead: Int = 0,
        isStarred: Boolean = false,
        exclusiveLocation: ExclusiveLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
    ) = Conversation(
        conversationId = ConversationId(id),
        order = 0,
        subject = subject,
        senders = listOf(Sender("address", "name")),
        recipients = emptyList(),
        expirationTime = expirationTime,
        numMessages = numMessages,
        numUnread = numUnRead,
        numAttachments = numAttachments,
        attachmentCount = attachmentCount,
        isStarred = isStarred,
        time = 0.toLong(),
        size = 0.toLong(),
        customLabels = labelIds.map { buildLabel(id) },
        avatarInformation = AvatarInformationSample.avatarSample,
        exclusiveLocation = exclusiveLocation,
        attachments = listOf(AttachmentMetadataSamples.Pdf)
    )

    private fun buildConversationWithConversationLabels(
        userId: UserId,
        id: String,
        subject: String,
        numMessages: Int = 1,
        labels: List<Label>,
        numAttachments: Int = 0,
        expirationTime: Long = 0,
        attachmentCount: AttachmentCount = AttachmentCount(0),
        exclusiveLocation: ExclusiveLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
    ) = Conversation(
        conversationId = ConversationId(id),
        order = 0,
        subject = subject,
        senders = listOf(Sender("address", "name")),
        recipients = emptyList(),
        expirationTime = expirationTime,
        numMessages = numMessages,
        numUnread = 0,
        numAttachments = numAttachments,
        attachmentCount = attachmentCount,
        isStarred = false,
        time = 0.toLong(),
        size = 0.toLong(),
        customLabels = labels,
        avatarInformation = AvatarInformationSample.avatarSample,
        exclusiveLocation = exclusiveLocation,
        attachments = listOf(AttachmentMetadataSamples.Pdf)
    )

    private fun buildLabel(labelId: String) = Label(
        labelId = LabelId(labelId),
        parentId = null,
        name = "",
        type = LabelType.MessageLabel,
        path = "",
        color = "",
        order = 0,
        isNotified = null,
        isExpanded = null,
        isSticky = null
    )
}
