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

package ch.protonmail.android.maillabel.presentation.bottomsheet.moveto

import android.graphics.Color
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationMoveToLocations
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageMoveToLocations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import me.proton.core.mailsettings.domain.entity.ViewMode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetMoveToLocationsTest {

    private val getConversationMoveToLocations = mockk<GetConversationMoveToLocations>()
    private val getMessageMoveToLocations = mockk<GetMessageMoveToLocations>()


    private val getMoveToLocations = GetMoveToLocations(
        getMessageMoveToLocations,
        getConversationMoveToLocations
    )

    @Before
    fun setUp() {
        mockkStatic(Color::class)
        every { Color.parseColor(any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Color::class)
    }

    @Test
    fun `gets available move to actions for messages when view mode for selected items is message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageId = MessageId("1")
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery { getMessageMoveToLocations(userId, labelId, listOf(messageId)) } returns expected.right()

        // When
        val actual = getMoveToLocations.forMessage(userId, labelId, messageId)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available move to actions for conversation when view mode for selected items is convo`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationId("1")
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery {
            getConversationMoveToLocations(userId, labelId, listOf(conversationId))
        } returns expected.right()


        // When
        val actual = getMoveToLocations.forConversation(userId, labelId, conversationId)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available move to actions for conversation when view mode for selected items is convo (mailbox)`() =
        runTest {
            // Given
            val userId = UserIdSample.Primary
            val labelId = LabelIdSample.Trash
            val viewMode = ViewMode.ConversationGrouping
            val conversationId = ConversationId("1")
            val itemIds = listOf(MoveToItemId(conversationId.id))
            val expected = listOf(
                MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
                MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
            )
            coEvery {
                getConversationMoveToLocations(userId, labelId, listOf(conversationId))
            } returns expected.right()


            // When
            val actual = getMoveToLocations.forMailbox(userId, labelId, itemIds, viewMode)

            // Then
            assertEquals(expected.right(), actual)
        }

    @Test
    fun `returns error when getting system actions fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationId("1")
        val error = DataError.Local.Unknown.left()
        coEvery { getConversationMoveToLocations(userId, labelId, listOf(conversationId)) } returns error

        // When
        val actual = getMoveToLocations.forConversation(userId, labelId, conversationId)

        // Then
        assertEquals(error, actual)
    }
}
