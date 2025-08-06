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

package ch.protonmail.android.maildetail.presentation.viewmodel

import java.util.concurrent.ConcurrentHashMap
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository.MessageState
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository.MessagesState
import ch.protonmail.android.mailmessage.domain.model.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeInMemoryConversationStateRepository : InMemoryConversationStateRepository {

    private val transformationCache = ConcurrentHashMap<MessageId, MessageBodyTransformations>()
    private val conversationCache = ConcurrentHashMap<MessageId, MessageState>()
    private var shouldHideMessagesBasedOnTrashFilter = true
    private val conversationStateFlow = MutableSharedFlow<MessagesState>(1)
    private val attachmentsListExpandCollapseMode = ConcurrentHashMap<MessageId, AttachmentListExpandCollapseMode>()
    private val rsvpEventCache = ConcurrentHashMap<MessageId, InMemoryConversationStateRepository.RsvpEventState>()

    init {
        conversationStateFlow.tryEmit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override val conversationState: Flow<MessagesState> =
        conversationStateFlow

    override suspend fun expandMessage(messageId: MessageId, decryptedBody: DecryptedMessageBody) {
        conversationCache[messageId] = MessageState.Expanded(decryptedBody)
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override suspend fun expandingMessage(messageId: MessageId) {
        conversationCache[messageId] = MessageState.Expanding
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override suspend fun collapseMessage(messageId: MessageId) {
        conversationCache[messageId] = MessageState.Collapsed
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override suspend fun updateAttachmentsExpandCollapseMode(
        messageId: MessageId,
        attachmentListExpandCollapseMode: AttachmentListExpandCollapseMode
    ) {
        attachmentsListExpandCollapseMode[messageId] = attachmentListExpandCollapseMode
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override suspend fun updateRsvpEventShown(messageId: MessageId, rsvpEvent: RsvpEvent) {
        rsvpEventCache[messageId] = InMemoryConversationStateRepository.RsvpEventState.Shown(rsvpEvent)
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override suspend fun updateRsvpEventAnswering(messageId: MessageId, answer: RsvpAnswer) {
        rsvpEventCache[messageId] = when (val cache = rsvpEventCache[messageId]) {
            is InMemoryConversationStateRepository.RsvpEventState.Shown ->
                InMemoryConversationStateRepository.RsvpEventState.Answering(cache.rsvpEvent, answer)
            else -> InMemoryConversationStateRepository.RsvpEventState.Error
        }
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override suspend fun updateRsvpEventLoading(messageId: MessageId, refresh: Boolean) {
        if (rsvpEventCache[messageId] == null || refresh) {
            rsvpEventCache[messageId] = InMemoryConversationStateRepository.RsvpEventState.Loading
            conversationStateFlow.emit(
                MessagesState(
                    transformationCache,
                    attachmentsListExpandCollapseMode,
                    conversationCache,
                    shouldHideMessagesBasedOnTrashFilter,
                    rsvpEventCache
                )
            )
        }
    }

    override suspend fun updateRsvpEventError(messageId: MessageId) {
        rsvpEventCache[messageId] = InMemoryConversationStateRepository.RsvpEventState.Error
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override suspend fun switchTrashedMessagesFilter() {
        shouldHideMessagesBasedOnTrashFilter = shouldHideMessagesBasedOnTrashFilter.not()
        conversationStateFlow.emit(
            MessagesState(
                transformationCache,
                attachmentsListExpandCollapseMode,
                conversationCache,
                shouldHideMessagesBasedOnTrashFilter,
                rsvpEventCache
            )
        )
    }

    override fun getTransformationsForMessage(messageId: MessageId) = transformationCache[messageId]

    override fun setTransformationsForMessage(messageId: MessageId, transformations: MessageBodyTransformations) {
        transformationCache[messageId] = transformations
    }
}
