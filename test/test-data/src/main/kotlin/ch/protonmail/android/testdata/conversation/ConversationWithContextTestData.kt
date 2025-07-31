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
import ch.protonmail.android.testdata.user.UserIdTestData.userId
import ch.protonmail.android.testdata.user.UserIdTestData.userId1
import me.proton.core.domain.entity.UserId

object ConversationWithContextTestData {

    val conversation1 = buildConversation(userId = userId, id = "1", time = 1000)
    val conversation2 = buildConversation(userId = userId, id = "2", time = 2000)
    val conversation3 = buildConversation(userId = userId, id = "3", time = 3000)
    val conversation4 = buildConversation(userId = userId, id = "4", time = 4000)

    val conversation1NoLabels = buildConversation(
        userId = userId, id = "1", time = 1000, labelIds = emptyList()
    )
    val conversation1Labeled = buildConversation(
        userId = userId, id = "1", time = 1000, labelIds = listOf("0")
    )
    val conversation2Labeled = buildConversation(
        userId = userId, id = "2", time = 2000, labelIds = listOf("4")
    )
    val conversation3Labeled = buildConversation(
        userId = userId, id = "3", time = 3000, labelIds = listOf("0", "1")
    )

    val conversation1Ordered = buildConversation(userId = userId, id = "1", order = 1000)
    val conversation2Ordered = buildConversation(userId = userId, id = "2", order = 2000)

    object User2 {

        val conversation1Labeled = buildConversation(
            userId = userId1, id = "1", time = 1000, labelIds = listOf("0")
        )
        val conversation2Labeled = buildConversation(
            userId = userId1, id = "2", time = 2000, labelIds = listOf("4")
        )
        val conversation3Labeled = buildConversation(
            userId = userId1, id = "3", time = 3000, labelIds = listOf("0", "1")
        )
    }

    fun getConversation(
        userId: UserId,
        id: String,
        order: Long = 0,
        time: Long = 0,
        labelIds: List<String> = listOf("0"),
        contextLabelId: LabelId = LabelId("0"),
        numAttachments: Int = 0,
        expirationTime: Long = 0,
        attachmentCount: AttachmentCount = AttachmentCount(0)
    ) = buildConversation(
        userId = userId,
        id = id,
        order = order,
        labelIds = labelIds,
        expirationTime = expirationTime,
        numAttachments = numAttachments,
        attachmentCount = attachmentCount,
        time = 0.toLong()
    )

    private fun buildConversation(
        userId: UserId,
        id: String,
        order: Long = 1000,
        time: Long = 1000,
        labelIds: List<String> = listOf(id),
        numAttachments: Int = 2,
        expirationTime: Long = 0,
        attachmentCount: AttachmentCount = AttachmentCount(0),
        exclusiveLocation: ExclusiveLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
    ) = Conversation(
        conversationId = ConversationId(id),
        order = order,
        subject = "subject",
        senders = emptyList(),
        recipients = emptyList(),
        expirationTime = expirationTime,
        numMessages = 1,
        numUnread = 0,
        numAttachments = numAttachments,
        attachmentCount = attachmentCount,
        isStarred = false,
        time = time,
        size = 0.toLong(),
        customLabels = labelIds.map { buildLabel(it) },
        avatarInformation = AvatarInformationSample.avatarSample,
        exclusiveLocation = exclusiveLocation,
        attachments = listOf(AttachmentMetadataSamples.Pdf),
        displaySnoozeReminder = false
    )

    private fun buildLabel(labelId: String) = Label(
        labelId = LabelId(labelId),
        parentId = null,
        name = labelId,
        type = LabelType.MessageLabel,
        path = labelId,
        color = "",
        order = labelId.hashCode(),
        isNotified = null,
        isExpanded = null,
        isSticky = null
    )

}
