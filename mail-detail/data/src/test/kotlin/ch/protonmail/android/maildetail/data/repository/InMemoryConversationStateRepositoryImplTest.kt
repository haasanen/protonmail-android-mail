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

package ch.protonmail.android.maildetail.data.repository

import java.util.Random
import java.util.UUID
import app.cash.turbine.test
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository.MessageState.Collapsed
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository.MessageState.Expanded
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository.MessageState.Expanding
import ch.protonmail.android.mailmessage.domain.model.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InMemoryConversationStateRepositoryImplTest {

    @Test
    fun `Should emit empty cache on start`() = runTest {
        // Given
        val repo = buildRepository()

        repo.conversationState.test {
            val initialState = awaitItem()
            // Then
            assertEquals(emptyMap(), initialState.messagesState)
            assertEquals(emptyMap(), initialState.attachmentsListExpandCollapseMode)
            assertEquals(emptyMap(), initialState.messagesTransformations)

        }
    }

    @Test
    fun `Should emit collapsed when putting a collapsed message id`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.Invoice

        // When
        repo.collapseMessage(messageId)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            assertEquals(Collapsed, conversationState.messagesState[messageId])
            assertEquals(1, conversationState.messagesState.size)
            assertFalse(conversationState.attachmentsListExpandCollapseMode.containsKey(messageId))
        }
    }

    @Test
    fun `Should emit expanding when putting a expanding message`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.Invoice

        // When
        repo.expandingMessage(messageId)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            assertEquals(conversationState.messagesState[messageId], Expanding)
            assertEquals(1, conversationState.messagesState.size)
        }
    }

    @Test
    fun `Should emit expanded when putting a expanded message`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.Invoice
        val decryptedBody = DecryptedMessageBody(
            messageId = messageId,
            value = UUID.randomUUID().toString(),
            mimeType = MimeType.Html,
            hasQuotedText = false,
            hasCalendarInvite = false,
            isUnread = false,
            banners = emptyList(),
            transformations = MessageBodyTransformations(false, false, false, null)
        )

        // When
        repo.expandMessage(messageId, decryptedBody)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            assertEquals(conversationState.messagesState[messageId], Expanded(decryptedBody))
            assertEquals(1, conversationState.messagesState.size)
        }
    }

    @Test
    fun `Should overwrite content when putting a message id`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.Invoice
        val decryptedBody = DecryptedMessageBody(
            messageId = messageId,
            value = UUID.randomUUID().toString(),
            mimeType = MimeType.Html,
            hasQuotedText = false,
            hasCalendarInvite = false,
            isUnread = false,
            banners = emptyList(),
            transformations = MessageBodyTransformations(false, false, false, null)
        )

        // When
        repo.expandMessage(messageId, decryptedBody)
        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            assertEquals(conversationState.messagesState[messageId], Expanded(decryptedBody))
            assertEquals(1, conversationState.messagesState.size)

            repo.collapseMessage(messageId)
            val conversationState2 = awaitItem()
            assertEquals(conversationState2.messagesState[messageId], Collapsed)
            assertEquals(1, conversationState2.messagesState.size)
        }
    }

    @Test
    fun `Should emit all the messages put in the cache`() = runTest {
        // Given
        val repo = buildRepository()
        val itemCount = Random().nextInt(100)

        // When
        @Suppress("ForEachOnRange")
        (0 until itemCount).forEach {
            repo.collapseMessage(MessageId(it.toString()))
        }

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            assertEquals(itemCount, conversationState.messagesState.size)
        }
    }

    @Test
    fun `should emit the opposite filter value when switching the trashed messages filter`() = runTest {
        // Given
        val repository = buildRepository()

        repository.conversationState.test {
            val illegal = awaitItem().shouldHideMessagesBasedOnTrashFilter

            // When
            repository.switchTrashedMessagesFilter()

            // Then
            assertNotEquals(illegal, awaitItem().shouldHideMessagesBasedOnTrashFilter)
        }
    }

    @Test
    fun `Should update attachments expand collapse mode`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.Invoice
        val mode = AttachmentListExpandCollapseMode.Expanded

        // When
        repo.updateAttachmentsExpandCollapseMode(messageId, mode)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            assertEquals(mode, conversationState.attachmentsListExpandCollapseMode[messageId])
            assertEquals(1, conversationState.attachmentsListExpandCollapseMode.size)
        }
    }

    @Test
    fun `should emit rsvp event shown when putting an rsvp event shown state`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.CalendarInvite
        val event = mockk<RsvpEvent>()

        // When
        repo.updateRsvpEventShown(messageId, event)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            val expected = InMemoryConversationStateRepository.RsvpEventState.Shown(event)
            assertEquals(expected, conversationState.rsvpEvents[messageId])
        }
    }

    @Test
    fun `should emit rsvp event answering when putting an rsvp event answering state`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.CalendarInvite
        val event = mockk<RsvpEvent>()
        val answer = RsvpAnswer.Yes

        // When
        repo.updateRsvpEventShown(messageId, event)
        repo.updateRsvpEventAnswering(messageId, answer)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            val expected = InMemoryConversationStateRepository.RsvpEventState.Answering(event, answer)
            assertEquals(expected, conversationState.rsvpEvents[messageId])
        }
    }

    @Test
    fun `should emit rsvp event loading when putting an rsvp event loading state`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.CalendarInvite

        // When
        repo.updateRsvpEventLoading(messageId, refresh = true)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            val expected = InMemoryConversationStateRepository.RsvpEventState.Loading
            assertEquals(expected, conversationState.rsvpEvents[messageId])
        }
    }

    @Test
    fun `should emit rsvp event error when putting an rsvp event error state`() = runTest {
        // Given
        val repo = buildRepository()
        val messageId = MessageIdSample.CalendarInvite

        // When
        repo.updateRsvpEventError(messageId)

        repo.conversationState.test {
            val conversationState = awaitItem()

            // Then
            val expected = InMemoryConversationStateRepository.RsvpEventState.Error
            assertEquals(expected, conversationState.rsvpEvents[messageId])
        }
    }

    private fun buildRepository() = InMemoryConversationStateRepositoryImpl()
}
