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
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.label.domain.entity.LabelId
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveConversationsTest {

    private val userId = UserIdSample.Primary
    private val conversationIds = listOf(ConversationIdSample.Invoices, ConversationIdSample.WeatherForecast)

    private val conversationRepository = mockk<ConversationRepository>()

    private val moveConversations = MoveConversations(conversationRepository = conversationRepository)

    @Test
    fun `when move succeeds then Unit is returned`() = runTest {
        // Given
        val destinationLabel = LabelId("labelId")
        val expectedConversations = listOf(ConversationSample.WeatherForecast, ConversationSample.AlphaAppFeedback)

        expectMoveSucceeds(destinationLabel, expectedConversations)
        coEvery { conversationRepository.observeCachedConversations(userId, conversationIds) } returns flowOf()

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
        coEvery { conversationRepository.observeCachedConversations(userId, conversationIds) } returns flowOf()

        // When
        val result = moveConversations(userId, conversationIds, destinationLabel)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), result)
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
