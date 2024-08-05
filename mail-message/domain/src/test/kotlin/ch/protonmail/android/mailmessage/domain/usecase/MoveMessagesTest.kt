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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.UndoableOperation
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcommon.domain.usecase.RegisterUndoableOperation
import ch.protonmail.android.maillabel.domain.model.MailLabels
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveExclusiveMailLabels
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.MessageSample
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.label.domain.entity.LabelId
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveMessagesTest {

    private val userId = UserIdSample.Primary
    private val messageIds = listOf(MessageIdSample.AugWeatherForecast, MessageIdSample.Invoice)
    private val exclusiveMailLabels = listOf(MailLabelTestData.inboxSystemLabel, MailLabelTestData.archiveSystemLabel)

    private val messageRepository = mockk<MessageRepository>()
    private val observeExclusiveMailLabels = mockk<ObserveExclusiveMailLabels>()
    private val registerUndoableOperation = mockk<RegisterUndoableOperation>()

    private val moveMessages = MoveMessages(
        messageRepository,
        observeExclusiveMailLabels,
        registerUndoableOperation
    )

    @Test
    fun `when move succeeds then Unit is returned`() = runTest {
        // Given
        expectObserveExclusiveMailLabelSucceeds()
        expectGetLocalMessagesSucceeds()
        expectMoveSucceeds(SystemLabelId.Spam.labelId, listOf(MessageSample.AugWeatherForecast))
        expectRegisterUndoOperationSucceeds()

        // When
        val actual = moveMessages(userId, messageIds, SystemLabelId.Spam.labelId)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `when move fails then DataError is returned`() = runTest {
        // Given
        expectObserveExclusiveMailLabelSucceeds()
        expectGetLocalMessagesSucceeds()
        expectMoveFails(SystemLabelId.Spam.labelId)

        // When
        val actual = moveMessages(userId, messageIds, SystemLabelId.Spam.labelId)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }

    @Test
    fun `store undoable operation when moving messages locally succeeds`() = runTest {
        // given
        val toLabel = SystemLabelId.Trash.labelId
        val messages = listOf(MessageSample.Invoice, MessageSample.HtmlInvoice)
        val expectedMap = messages.associate { it.messageId to it.labelIds.first() }
        expectObserveExclusiveMailLabelSucceeds()
        expectGetLocalMessagesSucceeds(messages)
        expectMoveSucceeds(toLabel, messages, expectedMap)
        expectRegisterUndoOperationSucceeds()

        // when
        moveMessages(userId, messageIds, toLabel)

        // then
        coVerify { registerUndoableOperation(any<UndoableOperation.UndoMoveMessages>()) }
    }

    private fun expectRegisterUndoOperationSucceeds() {
        coEvery { registerUndoableOperation(any<UndoableOperation.UndoMoveMessages>()) } just Runs
    }

    private fun expectObserveExclusiveMailLabelSucceeds() {
        every { observeExclusiveMailLabels(userId) } returns flowOf(
            MailLabels(
                system = exclusiveMailLabels,
                folders = emptyList(),
                labels = emptyList()
            )
        )
    }

    private fun expectGetLocalMessagesSucceeds(withMessages: List<Message>? = null) {
        val returnedMessages = withMessages ?: listOf(
            MessageSample.AugWeatherForecast.copy(labelIds = listOf(SystemLabelId.Archive.labelId)),
            MessageSample.Invoice.copy(labelIds = listOf(SystemLabelId.Inbox.labelId))
        )
        coEvery { messageRepository.getLocalMessages(userId, messageIds) } returns returnedMessages
    }

    private fun expectMoveSucceeds(
        destinationLabel: LabelId,
        expectedMessages: List<Message>,
        expectedMap: Map<MessageId, LabelId?> = buildExpectedMap()
    ) {
        coEvery {
            messageRepository.moveTo(userId, expectedMap, destinationLabel)
        } returns expectedMessages.right()
    }

    private fun expectMoveFails(destinationLabel: LabelId) {
        coEvery {
            messageRepository.moveTo(userId, buildExpectedMap(), destinationLabel)
        } returns DataError.Local.NoDataCached.left()
    }

    private fun buildExpectedMap() = mapOf(
        MessageIdSample.AugWeatherForecast to SystemLabelId.Archive.labelId,
        MessageIdSample.Invoice to SystemLabelId.Inbox.labelId
    )
}
