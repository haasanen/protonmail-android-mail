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
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.FindLocalSystemLabelId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveMessagesTest {

    private val userId = UserIdSample.Primary
    private val messageIds = listOf(MessageIdSample.AugWeatherForecast, MessageIdSample.Invoice)

    private val messageRepository = mockk<MessageRepository>()
    private val findLocalSystemLabelId = mockk<FindLocalSystemLabelId>()

    private val moveMessages = MoveMessages(messageRepository, findLocalSystemLabelId)

    @Test
    fun `when move succeeds then Unit is returned`() = runTest {
        // Given
        expectMoveSucceeds(SystemLabelId.Spam.labelId)

        // When
        val actual = moveMessages(userId, messageIds, SystemLabelId.Spam.labelId)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `when move fails then DataError is returned`() = runTest {
        // Given
        expectMoveFails(SystemLabelId.Spam.labelId)

        // When
        val actual = moveMessages(userId, messageIds, SystemLabelId.Spam.labelId)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }

    @Test
    fun `when move to system folder maps the static id to the local one`() = runTest {
        // Given
        val destinationLabel = SystemLabelId.Archive
        val localLabelId = LabelId("archive-local-id")
        expectMoveSucceeds(localLabelId)
        expectFindLocalLabel(destinationLabel, localLabelId)

        // When
        val result = moveMessages(userId, messageIds, destinationLabel)

        // Then
        coVerify { messageRepository.moveTo(userId, messageIds, localLabelId) }
        assertEquals(Unit.right(), result)
    }

    @Test
    fun `return error when move to system folder fails mapping the static id to the local one`() = runTest {
        // Given
        val destinationLabel = SystemLabelId.Archive
        expectFindLocalLabelFails(destinationLabel)

        // When
        val result = moveMessages(userId, messageIds, destinationLabel)

        // Then
        assertEquals(DataError.Local.IllegalStateError.left(), result)
    }

    private fun expectFindLocalLabelFails(systemLabelId: SystemLabelId) {
        coEvery { findLocalSystemLabelId(userId, systemLabelId) } returns null
    }

    private fun expectFindLocalLabel(systemLabelId: SystemLabelId, resolvedLabel: LabelId) {
        coEvery { findLocalSystemLabelId(userId, systemLabelId) } returns MailLabelId.System(resolvedLabel)
    }

    private fun expectMoveSucceeds(destinationLabel: LabelId, expectedMap: List<MessageId> = expectedMessageIds()) {
        coEvery {
            messageRepository.moveTo(userId, expectedMap, destinationLabel)
        } returns Unit.right()
    }

    private fun expectMoveFails(destinationLabel: LabelId) {
        coEvery {
            messageRepository.moveTo(userId, expectedMessageIds(), destinationLabel)
        } returns DataError.Local.NoDataCached.left()
    }

    private fun expectedMessageIds() = listOf(MessageIdSample.AugWeatherForecast, MessageIdSample.Invoice)
}
