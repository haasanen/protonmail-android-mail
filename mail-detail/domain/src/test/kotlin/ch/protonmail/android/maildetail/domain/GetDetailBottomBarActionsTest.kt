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

package ch.protonmail.android.maildetail.domain

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetAllConversationBottomBarActions
import ch.protonmail.android.maildetail.domain.usecase.GetDetailBottomBarActions
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.domain.model.MessageTheme
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageDetailBottomBarActions
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class GetDetailBottomBarActionsTest {

    private val getAllConversationBottomBarActions = mockk<GetAllConversationBottomBarActions>()
    private val getAllMessageBottomBarActions = mockk<GetMessageDetailBottomBarActions>()
    private val getDetailActions by lazy {
        GetDetailBottomBarActions(
            getAllConversationBottomBarActions,
            getAllMessageBottomBarActions
        )
    }

    @Test
    fun `returns visible bottom bar actions when use case succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Invoices
        val allActions = AllBottomBarActions(
            hiddenActions = listOf(Action.Star, Action.Label),
            visibleActions = listOf(Action.Spam, Action.Archive)
        )
        val expected = listOf(Action.Spam, Action.Archive)
        coEvery {
            getAllConversationBottomBarActions(userId, labelId, conversationId)
        } returns allActions.right()

        // When
        val result = getDetailActions(userId, labelId, conversationId)

        // Then
        assertEquals(expected.right(), result)
        verify { getAllMessageBottomBarActions wasNot Called }
    }

    @Test
    fun `returns visible bottom bar actions for message when single message mode is enabled`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageId = MessageIdSample.PlainTextMessage
        val allActions = AllBottomBarActions(
            hiddenActions = listOf(Action.Star, Action.Label),
            visibleActions = listOf(Action.Spam, Action.Archive)
        )
        val expected = listOf(Action.Spam, Action.Archive)
        val themeOptions = MessageThemeOptions(MessageTheme.Dark)
        coEvery {
            getAllMessageBottomBarActions(userId, labelId, messageId, themeOptions)
        } returns allActions.right()

        // When
        val result = getDetailActions(userId, labelId, messageId, themeOptions)

        // Then
        assertEquals(expected.right(), result)
        verify { getAllConversationBottomBarActions wasNot Called }
    }

    @Test
    fun `returns error when failing to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Invoices
        val error = DataError.Local.CryptoError.left()
        coEvery { getDetailActions(userId, labelId, conversationId) } returns error

        // When
        val result = getDetailActions(userId, labelId, conversationId)

        // Then
        assertEquals(error, result)
    }
}
