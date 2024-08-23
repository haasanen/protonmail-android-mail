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
import ch.protonmail.android.mailconversation.domain.entity.ConversationWithContext
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.mailmessage.data.mapper.toParticipant
import ch.protonmail.android.mailmessage.domain.model.AttachmentCount
import me.proton.core.label.domain.entity.LabelId
import uniffi.proton_mail_uniffi.CustomLabel

fun ConversationId.toLocalConversationId(): LocalConversationId = LocalConversationId(this.id.toULong())

fun LocalConversation.toConversation(): Conversation {
    val numMessages = this.numMessages.toInt()
    val numUnread = this.numUnread.toInt()
    val numAttachments = this.numAttachments.toInt()
    val contextTime = this.time.toLong()
    val contextSize = this.size.toLong()

    val labels = this.customLabels.map {
        it.toConversationLabel(
            this.id.toConversationId(), numMessages, numUnread, numAttachments, contextTime, contextSize
        )
    }

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
        starred = this.isStarred
    )
}

fun LocalConversation.toConversationWithContext(contextLabelId: LabelId): ConversationWithContext =
    ConversationWithContext(this.toConversation(), contextLabelId)

@Suppress("LongParameterList")
fun CustomLabel.toConversationLabel(
    conversationId: ConversationId,
    contextNumMessages: Int,
    contextNumUnread: Int,
    contextNumAttachments: Int,
    contextTime: Long,
    contextSize: Long
): ConversationLabel {
    return ConversationLabel(
        conversationId = conversationId,
        labelId = this.id.toLabelId(),
        contextNumMessages = contextNumMessages,
        contextNumUnread = contextNumUnread,
        contextNumAttachments = contextNumAttachments,
        contextTime = contextTime,
        contextSize = contextSize
    )
}

private fun LocalConversationId.toConversationId(): ConversationId = ConversationId(this.toString())

