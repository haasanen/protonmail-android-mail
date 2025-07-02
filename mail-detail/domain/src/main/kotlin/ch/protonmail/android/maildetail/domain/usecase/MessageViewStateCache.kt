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

package ch.protonmail.android.maildetail.domain.usecase

import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.mailmessage.domain.model.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import javax.inject.Inject

class MessageViewStateCache @Inject constructor(
    private val inMemoryConversationStateRepository: InMemoryConversationStateRepository
) {

    fun getTransformations(messageId: MessageId) =
        inMemoryConversationStateRepository.getTransformationsForMessage(messageId)

    fun setTransformations(messageId: MessageId, transformations: MessageBodyTransformations) =
        inMemoryConversationStateRepository.setTransformationsForMessage(messageId, transformations)

    suspend fun setExpanded(messageId: MessageId, decryptedBody: DecryptedMessageBody) {
        inMemoryConversationStateRepository.expandMessage(messageId, decryptedBody)
    }

    suspend fun setExpanding(messageId: MessageId) {
        inMemoryConversationStateRepository.expandingMessage(messageId)
    }

    suspend fun setCollapsed(messageId: MessageId) {
        inMemoryConversationStateRepository.collapseMessage(messageId)
    }

    suspend fun switchTrashedMessagesFilter() {
        inMemoryConversationStateRepository.switchTrashedMessagesFilter()
    }

    suspend fun updateAttachmentsExpandCollapseMode(
        messageId: MessageId,
        attachmentListExpandCollapseMode: AttachmentListExpandCollapseMode
    ) {
        inMemoryConversationStateRepository.updateAttachmentsExpandCollapseMode(
            messageId, attachmentListExpandCollapseMode
        )
    }
}
