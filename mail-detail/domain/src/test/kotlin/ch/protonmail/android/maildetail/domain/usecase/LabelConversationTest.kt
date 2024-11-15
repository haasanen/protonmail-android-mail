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

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.LabelConversations
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class LabelConversationTest {

    private val labelConversations: LabelConversations = mockk()
    private val labelConversation = LabelConversation(labelConversations)

    @Test
    fun `when repository fails then error is returned`() = runTest {
        // Given
        val error = DataError.Local.NoDataCached.left()
        val userId = UserIdSample.Primary
        val labelSelectionList = LabelSelectionList(listOf(LabelId("labelId")), listOf(LabelId("labelId2")))
        val conversationIds = listOf(ConversationIdSample.Invoices)
        val shouldArchive = false
        coEvery {
            labelConversations(
                userId = userId,
                conversationIds = conversationIds,
                updatedSelections = labelSelectionList,
                shouldArchive = shouldArchive
            )
        } returns error

        // When
        val result = labelConversation(
            userId = UserIdSample.Primary,
            conversationId = ConversationIdSample.Invoices,
            updatedSelections = labelSelectionList,
            shouldArchive = shouldArchive
        )

        // Then
        assertEquals(error, result)
    }

    @Test
    fun `when repository succeeds then Unit is returned`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelSelectionList = LabelSelectionList(listOf(LabelId("labelId")), listOf(LabelId("labelId2")))
        val conversationIds = listOf(ConversationIdSample.Invoices)
        val shouldArchive = true
        coEvery {
            labelConversations(
                userId = userId,
                conversationIds = conversationIds,
                updatedSelections = labelSelectionList,
                shouldArchive = shouldArchive
            )
        } returns Unit.right()

        // When
        val result = labelConversation(
            userId = UserIdSample.Primary,
            conversationId = ConversationIdSample.Invoices,
            updatedSelections = labelSelectionList,
            shouldArchive = shouldArchive
        )

        // Then
        assertEquals(Unit.right(), result)
    }
}
