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

package ch.protonmail.android.mailconversation.data.repository

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.local.RustConversationDataSource
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.mapper.toLocalConversationId
import ch.protonmail.android.testdata.label.rust.LabelAsActionsTestData
import ch.protonmail.android.testdata.label.rust.LocalLabelAsActionTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.proton_mail_uniffi.ConversationAction
import uniffi.proton_mail_uniffi.ConversationAvailableActions
import uniffi.proton_mail_uniffi.GeneralActions
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.IsSelected
import uniffi.proton_mail_uniffi.MovableSystemFolder
import uniffi.proton_mail_uniffi.MovableSystemFolderAction
import uniffi.proton_mail_uniffi.MoveAction
import uniffi.proton_mail_uniffi.ReplyAction
import kotlin.test.assertEquals

class RustConversationActionRepositoryTest {

    private val rustConversationDataSource: RustConversationDataSource = mockk()

    private val rustConversationRepository = RustConversationActionRepository(
        rustConversationDataSource
    )

    @Test
    fun `get available actions should return available actions when data source exposes them`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = SystemLabelId.Inbox.labelId
        val conversationIds = listOf(ConversationId("1"))
        val rustAvailableActions = ConversationAvailableActions(
            listOf(ReplyAction.REPLY, ReplyAction.FORWARD),
            listOf(ConversationAction.STAR, ConversationAction.LABEL_AS),
            listOf(
                MovableSystemFolderAction(Id(5uL), MovableSystemFolder.SPAM, IsSelected.UNSELECTED),
                MovableSystemFolderAction(Id(10uL), MovableSystemFolder.ARCHIVE, IsSelected.UNSELECTED)
            ),
            listOf(GeneralActions.VIEW_HEADERS)
        )

        coEvery {
            rustConversationDataSource.getAvailableActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns rustAvailableActions

        // When
        val result = rustConversationRepository.getAvailableActions(userId, labelId, conversationIds)

        // Then
        val expected = AvailableActions(
            listOf(Action.Reply, Action.Forward),
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive),
            listOf(Action.ViewHeaders)
        )
        assertEquals(expected.right(), result)
    }

    @Test
    fun `get available actions should return error when data source fails`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = SystemLabelId.Inbox.labelId
        val conversationIds = listOf(ConversationId("1"))

        coEvery {
            rustConversationDataSource.getAvailableActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns null

        // When
        val result = rustConversationRepository.getAvailableActions(userId, labelId, conversationIds)

        // Then
        assertEquals(DataError.Local.Unknown.left(), result)
    }

    @Test
    fun `get available actions skips any unhandled actions returned by the data source`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = SystemLabelId.Inbox.labelId
        val conversationIds = listOf(ConversationId("1"))
        val rustAvailableActions = ConversationAvailableActions(
            listOf(ReplyAction.REPLY_ALL),
            listOf(ConversationAction.PIN),
            listOf(MovableSystemFolderAction(Id(10uL), MovableSystemFolder.INBOX, IsSelected.UNSELECTED)),
            emptyList()
        )

        coEvery {
            rustConversationDataSource.getAvailableActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns rustAvailableActions

        // When
        val result = rustConversationRepository.getAvailableActions(userId, labelId, conversationIds)

        // Then
        val expected = AvailableActions(
            listOf(Action.ReplyAll),
            emptyList(),
            emptyList(),
            emptyList()
        )
        assertEquals(expected.right(), result)
    }

    @Test
    fun `get available system move to actions should return actions when data source exposes them`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = SystemLabelId.Inbox.labelId
        val conversationIds = listOf(ConversationId("1"))
        val rustMoveToActions = listOf(
            MoveAction.SystemFolder(
                MovableSystemFolderAction(Id(2uL), MovableSystemFolder.ARCHIVE, IsSelected.UNSELECTED)
            ),
            MoveAction.SystemFolder(
                MovableSystemFolderAction(Id(3uL), MovableSystemFolder.TRASH, IsSelected.UNSELECTED)
            )
        )

        coEvery {
            rustConversationDataSource.getAvailableSystemMoveToActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns rustMoveToActions

        // When
        val result = rustConversationRepository.getSystemMoveToLocations(userId, labelId, conversationIds)

        // Then
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        assertEquals(expected.right(), result)
    }

    @Test
    fun `get available system move to actions should return error when data source fails`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = SystemLabelId.Inbox.labelId
        val conversationIds = listOf(ConversationId("1"))

        coEvery {
            rustConversationDataSource.getAvailableSystemMoveToActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns null

        // When
        val result = rustConversationRepository.getSystemMoveToLocations(userId, labelId, conversationIds)

        // Then
        assertEquals(DataError.Local.Unknown.left(), result)
    }

    @Test
    fun `get available label as actions should return actions when data source exposes them`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = SystemLabelId.Inbox.labelId
        val conversationIds = listOf(ConversationId("1"))
        val rustLabelAsActions = listOf(
            LocalLabelAsActionTestData.selectedAction,
            LocalLabelAsActionTestData.unselectedAction,
            LocalLabelAsActionTestData.partiallySelectedAction
        )

        coEvery {
            rustConversationDataSource.getAvailableLabelAsActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns rustLabelAsActions

        // When
        val result = rustConversationRepository.getAvailableLabelAsActions(userId, labelId, conversationIds)

        // Then
        val expected = LabelAsActionsTestData.actions
        assertEquals(expected.right(), result)
    }

    @Test
    fun `get available label as actions should return error when data source fails`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = SystemLabelId.Inbox.labelId
        val conversationIds = listOf(ConversationId("1"))

        coEvery {
            rustConversationDataSource.getAvailableLabelAsActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns null

        // When
        val result = rustConversationRepository.getAvailableLabelAsActions(userId, labelId, conversationIds)

        // Then
        assertEquals(DataError.Local.Unknown.left(), result)
    }
}
