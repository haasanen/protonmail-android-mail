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

package ch.protonmail.android.mailconversation.domain.entity

import ch.protonmail.android.mailattachments.domain.model.AttachmentCount
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailmessage.domain.model.Sender

data class Conversation(
    val conversationId: ConversationId,
    val order: Long,
    val subject: String,
    val senders: List<Sender>,
    val recipients: List<Recipient>,
    val expirationTime: Long,
    val numMessages: Int,
    val numUnread: Int,
    val numAttachments: Int,
    val attachmentCount: AttachmentCount,
    val attachments: List<AttachmentMetadata>,
    val isStarred: Boolean,
    val time: Long,
    val size: Long,
    val customLabels: List<Label>,
    val avatarInformation: AvatarInformation,
    val exclusiveLocation: ExclusiveLocation
)
