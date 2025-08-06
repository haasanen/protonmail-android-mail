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

import java.util.UUID
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MessageViewStateCacheTest {

    private val repo = mockk<InMemoryConversationStateRepository>(relaxUnitFun = true)

    @Test
    fun `Should expand message on setExpanded`() = runTest {
        // Given
        val useCase = buildUseCase()
        val messageId = MessageId(UUID.randomUUID().toString())
        val decryptedMessageBody = DecryptedMessageBody(
            messageId = messageId,
            value = UUID.randomUUID().toString(),
            mimeType = MimeType.Html,
            hasQuotedText = false,
            isUnread = true,
            hasCalendarInvite = false,
            banners = emptyList(),
            attachments = emptyList(),
            transformations = MessageBodyTransformations(false, false, false, null)
        )

        // When
        useCase.setExpanded(messageId, decryptedMessageBody)

        // Then
        coVerify { repo.expandMessage(messageId, decryptedMessageBody) }
    }

    @Test
    fun `Should collapse message on setCollapsed`() = runTest {
        // Given
        val useCase = buildUseCase()
        val messageId = MessageId(UUID.randomUUID().toString())

        // When
        useCase.setCollapsed(messageId)

        // Then
        coVerify { repo.collapseMessage(messageId) }
    }

    @Test
    fun `Should call setExpanding on expanding message`() = runTest {
        // Given
        val useCase = buildUseCase()
        val messageId = MessageId(UUID.randomUUID().toString())

        // When
        useCase.setExpanding(messageId)

        // Then
        coVerify { repo.expandingMessage(messageId) }
    }

    @Test
    fun `should switch filter on switch filter`() = runTest {
        // Given
        val useCase = buildUseCase()

        // When
        useCase.switchTrashedMessagesFilter()

        // Then
        coVerify { repo.switchTrashedMessagesFilter() }
    }

    @Test
    fun `should update rsvp event shown`() = runTest {
        // Given
        val messageId = MessageIdSample.CalendarInvite
        val event = mockk<RsvpEvent>()
        val useCase = buildUseCase()

        // When
        useCase.updateRsvpEventShown(messageId, event)

        // Then
        coVerify { repo.updateRsvpEventShown(messageId, event) }
    }

    @Test
    fun `should update rsvp event answering`() = runTest {
        // Given
        val messageId = MessageIdSample.CalendarInvite
        val answer = RsvpAnswer.Yes
        val useCase = buildUseCase()

        // When
        useCase.updateRsvpEventAnswering(messageId, answer)

        // Then
        coVerify { repo.updateRsvpEventAnswering(messageId, answer) }
    }

    @Test
    fun `should update rsvp event loading`() = runTest {
        // Given
        val messageId = MessageIdSample.CalendarInvite
        val useCase = buildUseCase()

        // When
        useCase.updateRsvpEventLoading(messageId, refresh = true)

        // Then
        coVerify { repo.updateRsvpEventLoading(messageId, refresh = true) }
    }

    @Test
    fun `should update rsvp event error`() = runTest {
        // Given
        val messageId = MessageIdSample.CalendarInvite
        val useCase = buildUseCase()

        // When
        useCase.updateRsvpEventError(messageId)

        // Then
        coVerify { repo.updateRsvpEventError(messageId) }
    }

    private fun buildUseCase() = MessageViewStateCache(repo)
}
