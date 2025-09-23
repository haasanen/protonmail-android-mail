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

package ch.protonmail.android.maillabel.presentation

import android.graphics.Color
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationLabelAsActions
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.maillabel.presentation.bottomsheet.GetLabelAsBottomSheetContent
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageLabelAsActions
import ch.protonmail.android.testdata.label.rust.LabelAsActionsTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.maillabel.domain.model.ViewMode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

import kotlin.test.assertEquals

class GetLabelAsBottomSheetContentTest {

    private val getConversationLabelAsActions = mockk<GetConversationLabelAsActions>()
    private val getMessageLabelAsActions = mockk<GetMessageLabelAsActions>()

    private val getLabelAsBottomSheetContent = GetLabelAsBottomSheetContent(
        getMessageLabelAsActions,
        getConversationLabelAsActions
    )

    @BeforeTest
    fun setUp() {
        mockkStatic(Color::parseColor)
        every { Color.parseColor(any()) } returns 0
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(Color::parseColor)
    }


    @Test
    fun `gets available label as actions for messages when view mode for selected items is message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(LabelAsItemId("1"))
        val messageIds = items.map { MessageId(it.value) }
        val viewMode = ViewMode.NoConversationGrouping
        val expected = LabelAsActionsTestData.onlySelectedActions
        coEvery { getMessageLabelAsActions(userId, labelId, messageIds) } returns expected.right()

        // When
        val actual = getLabelAsBottomSheetContent.forMailbox(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available label actions for conversations when view mode for selected items is conversation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(LabelAsItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = LabelAsActionsTestData.onlySelectedActions
        coEvery { getConversationLabelAsActions(userId, labelId, convoIds) } returns expected.right()

        // When
        val actual = getLabelAsBottomSheetContent.forMailbox(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when failing to get available label as actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(LabelAsItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = DataError.Local.CryptoError.left()
        coEvery { getConversationLabelAsActions(userId, labelId, convoIds) } returns expected

        // When
        val actual = getLabelAsBottomSheetContent.forMailbox(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return bottom sheet action data when all operations succeeded for message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Trash.labelId
        val messageId = MessageIdSample.PlainTextMessage
        val expectedLabelAsActions = LabelAsActionsTestData.unselectedActions
        coEvery {
            getMessageLabelAsActions(userId, labelId, listOf(messageId))
        } returns expectedLabelAsActions.right()

        // When
        val actual = getLabelAsBottomSheetContent.forMessage(userId, labelId, messageId).getOrNull()

        // Then
        assertEquals(expectedLabelAsActions, actual)
    }


    @Test
    fun `should return an error when observing labels failed for message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Trash.labelId
        val messageId = MessageIdSample.PlainTextMessage
        coEvery {
            getMessageLabelAsActions(userId, labelId, listOf(messageId))
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = getLabelAsBottomSheetContent.forMessage(userId, labelId, messageId)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }

    @Test
    fun `should return bottom sheet action data when all operations succeeded for conversation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Trash.labelId
        val conversationId = ConversationIdSample.Invoices
        val expectedLabelAsActions = LabelAsActionsTestData.unselectedActions
        coEvery {
            getConversationLabelAsActions(userId, labelId, listOf(conversationId))
        } returns expectedLabelAsActions.right()

        // When
        val actual = getLabelAsBottomSheetContent.forConversation(userId, labelId, conversationId).getOrNull()

        // Then
        assertEquals(expectedLabelAsActions, actual)
    }

    @Test
    fun `should return empty bottom sheet action data when observing labels failed for conversation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Trash.labelId
        val conversationId = ConversationIdSample.Invoices
        coEvery {
            getConversationLabelAsActions(userId, labelId, listOf(conversationId))
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = getLabelAsBottomSheetContent.forConversation(userId, labelId, conversationId)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }
}
