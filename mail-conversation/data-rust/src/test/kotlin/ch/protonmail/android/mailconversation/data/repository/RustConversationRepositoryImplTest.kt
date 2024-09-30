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

import app.cash.turbine.test
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.local.RustConversationDataSource
import ch.protonmail.android.mailconversation.data.mapper.toConversation
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.data.mapper.toConversationId
import ch.protonmail.android.mailmessage.data.mapper.toConversationMessagesWithMessageToOpen
import ch.protonmail.android.mailmessage.data.mapper.toLocalConversationId
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.testdata.conversation.rust.LocalConversationIdSample
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.label.LabelTestData
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import uniffi.proton_mail_uniffi.ConversationAction
import uniffi.proton_mail_uniffi.ConversationAvailableActions
import uniffi.proton_mail_uniffi.GeneralActions
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.IsSelected
import uniffi.proton_mail_uniffi.MoveAction
import uniffi.proton_mail_uniffi.ReplyAction
import uniffi.proton_mail_uniffi.SystemFolderAction
import uniffi.proton_mail_uniffi.SystemLabel
import kotlin.test.assertEquals

class RustConversationRepositoryImplTest {

    private val rustConversationDataSource: RustConversationDataSource = mockk()

    private val userId = UserId("test_user")
    private val labelWithSystemLabelId = LabelWithSystemLabelId(
        LabelTestData.systemLabel, systemLabelId = SystemLabelId.Archive
    )
    private val rustConversationRepository = RustConversationRepositoryImpl(
        rustConversationDataSource
    )

    @Test
    fun `getLocalConversations should return conversations`() = runTest {
        // Given
        val pageKey = PageKey(labelId = labelWithSystemLabelId.label.labelId)
        val localConversations = listOf(
            LocalConversationTestData.AugConversation, LocalConversationTestData.SepConversation
        )
        val expectedConversations = localConversations.map { it.toConversation() }
        coEvery { rustConversationDataSource.getConversations(userId, any()) } returns localConversations

        // When
        val result = rustConversationRepository.getLocalConversations(userId, pageKey)

        // Then
        coVerify { rustConversationDataSource.getConversations(userId, any()) }
        assertEquals(expectedConversations, result)
    }

    @Test
    fun `observeConversation should return the conversation for the given id`() = runTest {
        // Given
        val conversationId = LocalConversationIdSample.AugConversation.toConversationId()
        val localConversation = LocalConversationTestData.AugConversation
        val expected = localConversation.toConversation()
        coEvery { rustConversationDataSource.observeConversation(userId, any()) } returns flowOf(localConversation)

        // When
        rustConversationRepository.observeConversation(userId, conversationId, refreshData = false).test {
            val result = awaitItem().getOrNull()

            // Then
            assertEquals(expected, result)
            coVerify { rustConversationDataSource.observeConversation(userId, any()) }

            awaitComplete()
        }

    }

    @Test
    fun `observeConversation should return error when rust provides no conversation `() = runTest {
        // Given
        val conversationId = LocalConversationIdSample.AugConversation.toConversationId()

        coEvery { rustConversationDataSource.observeConversation(userId, any()) } returns null

        // When
        rustConversationRepository.observeConversation(userId, conversationId, refreshData = false).test {
            val result = awaitItem().getOrNull()

            // Then
            assertEquals(null, result)
            coVerify { rustConversationDataSource.observeConversation(userId, any()) }

            awaitComplete()
        }
    }

    @Test
    fun `observeConversationMessages should return list of messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId = LocalConversationIdSample.AugConversation.toConversationId()
        val localMessages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        val localConversationMessages = LocalConversationMessages(
            messageIdToOpen = LocalMessageIdSample.AugWeatherForecast,
            messages = localMessages
        )
        val expectedConversationMessages = localConversationMessages.toConversationMessagesWithMessageToOpen()

        coEvery {
            rustConversationDataSource.observeConversationMessages(userId, conversationId.toLocalConversationId())
        } returns flowOf(localConversationMessages)

        // When
        rustConversationRepository.observeConversationMessages(userId, conversationId).test {
            val result = awaitItem().getOrElse { null }

            // Then
            assertEquals(expectedConversationMessages, result)
            coVerify {
                rustConversationDataSource.observeConversationMessages(
                    userId,
                    conversationId.toLocalConversationId()
                )
            }
            awaitComplete()
        }
    }

    @Test
    fun `observeConversationMessages should return DataError when no messages found`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId = LocalConversationIdSample.AugConversation.toConversationId()
        val expectedError = DataError.Local.NoDataCached.left()

        coEvery {
            rustConversationDataSource.observeConversationMessages(userId, conversationId.toLocalConversationId())
        } returns flowOf(LocalConversationMessages(LocalMessageIdSample.AugWeatherForecast, emptyList()))

        // When
        rustConversationRepository.observeConversationMessages(userId, conversationId).test {
            val result = awaitItem()

            // Then
            assertEquals(expectedError, result)
            coVerify {
                rustConversationDataSource.observeConversationMessages(
                    userId,
                    conversationId.toLocalConversationId()
                )
            }
            awaitComplete()
        }
    }

    @Test
    fun `markRead should mark conversations as read`() = runTest {
        // Given
        val conversationIds = listOf(LocalConversationIdSample.AugConversation.toConversationId())
        coEvery { rustConversationDataSource.markRead(userId, any()) } just Runs

        // When
        val result = rustConversationRepository.markRead(userId, conversationIds)

        // Then
        coVerify { rustConversationDataSource.markRead(userId, conversationIds.map { it.toLocalConversationId() }) }
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun `markUnread should mark conversations as unread`() = runTest {
        // Given
        val conversationIds = listOf(LocalConversationIdSample.AugConversation.toConversationId())
        coEvery { rustConversationDataSource.markUnread(userId, any()) } just Runs

        // When
        val result = rustConversationRepository.markUnread(
            userId, conversationIds,
            labelWithSystemLabelId.label.labelId
        )

        // Then
        coVerify { rustConversationDataSource.markUnread(userId, conversationIds.map { it.toLocalConversationId() }) }
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `should star conversations`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation.toConversationId(),
            LocalConversationIdSample.SepConversation.toConversationId()
        )
        coEvery { rustConversationDataSource.starConversations(userId, any()) } returns Unit

        // When
        val result = rustConversationRepository.star(userId, conversationIds)

        // Then
        coVerify {
            rustConversationDataSource.starConversations(
                userId,
                conversationIds.map {
                    it.toLocalConversationId()
                }
            )
        }
        assertEquals(emptyList<Conversation>().right(), result)
    }

    @Test
    fun `should unStar conversations`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation.toConversationId(),
            LocalConversationIdSample.SepConversation.toConversationId()
        )
        coEvery { rustConversationDataSource.unStarConversations(userId, any()) } returns Unit

        // When
        val result = rustConversationRepository.unStar(userId, conversationIds)

        // Then
        coVerify {
            rustConversationDataSource.unStarConversations(
                userId,
                conversationIds.map {
                    it.toLocalConversationId()
                }
            )
        }
        assertEquals(emptyList<Conversation>().right(), result)
    }

    @Test
    fun `move should call rust data source function and return empty list when successful`() = runTest {
        // Given
        val conversationIds = listOf(ConversationId("1"), ConversationId("2"))
        val toLabelId = LabelIdSample.Trash

        coEvery {
            rustConversationDataSource.moveConversations(
                userId,
                conversationIds.map { it.toLocalConversationId() },
                toLabelId.toLocalLabelId()
            )
        } just Runs

        // When
        val result = rustConversationRepository.move(userId, conversationIds, toLabelId)

        // Then
        coVerify {
            rustConversationDataSource.moveConversations(
                userId,
                conversationIds.map { it.toLocalConversationId() },
                toLabelId.toLocalLabelId()
            )
        }
        assertEquals(emptyList<Conversation>().right(), result)
    }

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
                SystemFolderAction(Id(5uL), SystemLabel.SPAM, IsSelected.UNSELECTED),
                SystemFolderAction(Id(10uL), SystemLabel.ARCHIVE, IsSelected.UNSELECTED)
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
            listOf(
                SystemFolderAction(Id(5uL), SystemLabel.ALL_DRAFTS, IsSelected.UNSELECTED),
                SystemFolderAction(Id(10uL), SystemLabel.INBOX, IsSelected.UNSELECTED)
            ),
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
            MoveAction.SystemFolder(SystemFolderAction(Id(2uL), SystemLabel.ARCHIVE, IsSelected.UNSELECTED)),
            MoveAction.SystemFolder(SystemFolderAction(Id(3uL), SystemLabel.TRASH, IsSelected.UNSELECTED))
        )

        coEvery {
            rustConversationDataSource.getAvailableSystemMoveToActions(
                userId,
                labelId.toLocalLabelId(),
                conversationIds.map { it.toLocalConversationId() }
            )
        } returns rustMoveToActions

        // When
        val result = rustConversationRepository.getAvailableSystemMoveToActions(userId, labelId, conversationIds)

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
        val result = rustConversationRepository.getAvailableSystemMoveToActions(userId, labelId, conversationIds)

        // Then
        assertEquals(DataError.Local.Unknown.left(), result)
    }
}
