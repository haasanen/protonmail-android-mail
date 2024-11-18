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
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.usecase.LabelMessages
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class LabelMessageTest {

    private val labelMessages: LabelMessages = mockk()
    private val labelMessage = LabelMessage(labelMessages)

    @Test
    fun `when repository fails then error is returned`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val error = DataError.Local.NoDataCached.left()
        val expectedMessageIds = listOf(MessageIdSample.Invoice)
        val updatedSelection = LabelSelectionList(listOf(LabelId("labelId")), listOf(LabelId("labelId2")))
        coEvery {
            labelMessages(
                userId = userId,
                messageIds = expectedMessageIds,
                updatedSelections = updatedSelection,
                shouldArchive = false
            )
        } returns error

        // When
        val result = labelMessage(
            userId = UserIdSample.Primary,
            messageId = MessageIdSample.Invoice,
            updatedSelection = updatedSelection,
            shouldArchive = false
        )

        // Then
        assertEquals(error, result)
    }

    @Test
    fun `when repository succeeds then Unit is returned`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expectedMessageIds = listOf(MessageIdSample.Invoice)
        val updatedSelection = LabelSelectionList(listOf(LabelId("labelId")), listOf(LabelId("labelId2")))
        coEvery {
            labelMessages(
                userId = userId,
                messageIds = expectedMessageIds,
                updatedSelections = updatedSelection,
                shouldArchive = true
            )
        } returns Unit.right()

        // When
        val result = labelMessage(
            userId = UserIdSample.Primary,
            messageId = MessageIdSample.Invoice,
            updatedSelection = updatedSelection,
            shouldArchive = true
        )

        // Then
        assertEquals(Unit.right(), result)
    }

}
