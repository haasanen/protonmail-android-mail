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

package ch.protonmail.android.mailconversation.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.mailconversation.domain.sample.ConversationSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.FindLocalSystemLabelId
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveConversationsTest {

    private val userId = UserIdSample.Primary
    private val conversationIds = listOf(ConversationIdSample.Invoices, ConversationIdSample.WeatherForecast)

    private val conversationRepository = mockk<ConversationRepository>()
    private val findLocalSystemLabelId = mockk<FindLocalSystemLabelId>()

    private val moveConversations = MoveConversations(
        conversationRepository = conversationRepository,
        findLocalSystemLabelId = findLocalSystemLabelId
    )

    @Test
    fun `when move succeeds then Unit is returned`() = runTest {
        // Given
        val destinationLabel = LabelId("labelId")
        val expectedConversations = listOf(ConversationSample.WeatherForecast, ConversationSample.AlphaAppFeedback)

        expectMoveSucceeds(destinationLabel, expectedConversations)

        // When
        val result = moveConversations(userId, conversationIds, destinationLabel)

        // Then
        assertEquals(Unit.right(), result)
    }

    @Test
    fun `when move fails then DataError is returned`() = runTest {
        // Given
        val destinationLabel = LabelId("labelId")

        expectMoveFails(destinationLabel)

        // When
        val result = moveConversations(userId, conversationIds, destinationLabel)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), result)
    }

    @Test
    fun `when move to system folder maps the static id to the local one`() = runTest {
        // Given
        val destinationLabel = SystemLabelId.Archive
        val localLabelId = LabelId("archive-local-id")
        val expectedConversations = listOf(ConversationSample.WeatherForecast, ConversationSample.AlphaAppFeedback)
        expectMoveSucceeds(localLabelId, expectedConversations)
        expectFindLocalLabel(destinationLabel, localLabelId)

        // When
        val result = moveConversations(userId, conversationIds, destinationLabel)

        // Then
        assertEquals(Unit.right(), result)
    }

    @Test
    fun `return error when move to system folder fails mapping the static id to the local one`() = runTest {
        // Given
        val destinationLabel = SystemLabelId.Archive
        expectFindLocalLabelFails(destinationLabel)

        // When
        val result = moveConversations(userId, conversationIds, destinationLabel)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), result)
    }

    private fun expectFindLocalLabelFails(systemLabelId: SystemLabelId) {
        coEvery { findLocalSystemLabelId(userId, systemLabelId) } returns null
    }

    private fun expectFindLocalLabel(systemLabelId: SystemLabelId, resolvedLabel: LabelId) {
        coEvery { findLocalSystemLabelId(userId, systemLabelId) } returns MailLabelId.System(resolvedLabel)
    }

    private fun expectMoveSucceeds(destinationLabel: LabelId, expectedList: List<Conversation>) {
        coEvery {
            conversationRepository.move(userId, conversationIds, destinationLabel)
        } returns expectedList.right()
    }

    private fun expectMoveFails(destinationLabel: LabelId) {
        coEvery {
            conversationRepository.move(userId, conversationIds, destinationLabel)
        } returns DataError.Local.NoDataCached.left()
    }

}
