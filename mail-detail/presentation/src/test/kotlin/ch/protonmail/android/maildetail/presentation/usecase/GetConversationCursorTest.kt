/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.maildetail.presentation.usecase

import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.CursorId
import ch.protonmail.android.mailcommon.domain.model.EphemeralMailboxCursor
import ch.protonmail.android.mailcommon.domain.repository.EphemeralMailboxCursorRepository
import ch.protonmail.android.mailmailbox.domain.usecase.SetEphemeralMailboxCursor
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Assert
import kotlin.test.Test

class GetConversationCursorTest {

    private val setEphemeralMailboxCursor = mockk<SetEphemeralMailboxCursor>(relaxed = true)
    private val mockEphemeralMailboxCursorRepository = mockk<EphemeralMailboxCursorRepository>()
    private val userId = UserId("userID")
    private val conversationId = ConversationId("conversationId")
    private val messageId = "messageId"

    @Test
    fun `returns cursor when returned by flow`() = runTest {
        val expected = EphemeralMailboxCursor.Data(mockk())
        val mockFlow = flowOf(expected)
        every { mockEphemeralMailboxCursorRepository.observeCursor() } returns mockFlow
        val result = GetConversationCursor(mockEphemeralMailboxCursorRepository, setEphemeralMailboxCursor)
            .invoke(userId, false, conversationId, messageId, false)
            .first()

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `given cursorflow returns state of null then setEphemeralMailboxCursor`() = runTest {
        val expected = EphemeralMailboxCursor.Data(mockk())
        val mockFlow = flowOf(null, expected)
        every { mockEphemeralMailboxCursorRepository.observeCursor() } returns mockFlow
        val result = GetConversationCursor(mockEphemeralMailboxCursorRepository, setEphemeralMailboxCursor)
            .invoke(userId, false, conversationId, messageId, false)
            .toList()

        coVerify { setEphemeralMailboxCursor.invoke(userId, false, CursorId(conversationId, messageId)) }
        Assert.assertEquals(EphemeralMailboxCursor.Initialising, result.first())
        Assert.assertEquals(expected, result[1])
    }

    @Test
    fun `given cursorflow returns state of not Initialised then setEphemeralMailboxCursor`() = runTest {
        val expected = EphemeralMailboxCursor.Data(mockk())
        val mockFlow = flowOf(EphemeralMailboxCursor.NotInitalised, expected)
        every { mockEphemeralMailboxCursorRepository.observeCursor() } returns mockFlow
        val result = GetConversationCursor(mockEphemeralMailboxCursorRepository, setEphemeralMailboxCursor)
            .invoke(userId, false, conversationId, messageId, false)
            .toList()

        coVerify { setEphemeralMailboxCursor.invoke(userId, false, CursorId(conversationId, messageId)) }
        Assert.assertEquals(EphemeralMailboxCursor.Initialising, result.first())
        Assert.assertEquals(expected, result[1])
    }
}
