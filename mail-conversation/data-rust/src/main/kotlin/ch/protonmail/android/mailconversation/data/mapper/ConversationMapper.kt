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

package ch.protonmail.android.mailconversation.data.mapper

import ch.protonmail.android.mailcommon.domain.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.mailconversation.domain.entity.ConversationLabel
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.mailmessage.data.mapper.toParticipant
import ch.protonmail.android.mailmessage.domain.model.AttachmentCount
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelType
import uniffi.proton_mail_uniffi.InlineCustomLabel

fun LocalConversation.toConversation(): Conversation {

    val labels = emptyList<ConversationLabel>()

    return Conversation(
        conversationId = this.id.toConversationId(),
        userId = FAKE_USER_ID,
        order = this.displayOrder.toLong(),
        subject = this.subject,
        senders = this.senders.map { it.toParticipant() },
        recipients = this.recipients.map { it.toParticipant() },
        numMessages = this.numMessages.toInt(),
        numUnread = this.numUnread.toInt(),
        numAttachments = this.numAttachments.toInt(),
        expirationTime = this.expirationTime.toLong(),
        labels = labels,
        attachmentCount = AttachmentCount(this.numAttachments.toInt()),
        starred = this.isStarred,
        time = time.toLong(),
        size = size.toLong(),
        customLabels = this.customLabels.map { it.toLabel() }
    )
}

fun InlineCustomLabel.toLabel() = Label(
    userId = FAKE_USER_ID,
    labelId = this.id.toLabelId(),
    parentId = null,
    name = this.name,
    type = LabelType.MessageLabel,
    path = "",
    color = this.color.value,
    order = 0,
    isNotified = null,
    isExpanded = null,
    isSticky = null
)

private fun LocalConversationId.toConversationId(): ConversationId = ConversationId(this.value.toString())

