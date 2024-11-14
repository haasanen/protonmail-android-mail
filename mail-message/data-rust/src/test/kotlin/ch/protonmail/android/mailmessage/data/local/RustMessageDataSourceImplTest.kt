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

package ch.protonmail.android.mailmessage.data.local

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.search.RustMessageSearchQuery
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.usecase.GetRustAllMessageBottomBarActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustAvailableMessageActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustMessageLabelAsActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustMessageMoveToActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustSenderImage
import ch.protonmail.android.mailmessage.data.usecase.RustDeleteMessages
import ch.protonmail.android.mailmessage.data.usecase.RustLabelMessages
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesRead
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesUnread
import ch.protonmail.android.mailmessage.data.usecase.RustMoveMessages
import ch.protonmail.android.mailmessage.data.usecase.RustStarMessages
import ch.protonmail.android.mailmessage.data.usecase.RustUnstarMessages
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.proton_mail_uniffi.AllBottomBarMessageActions
import uniffi.proton_mail_uniffi.CustomFolderAction
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.IsSelected
import uniffi.proton_mail_uniffi.LabelAsAction
import uniffi.proton_mail_uniffi.LabelColor
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import uniffi.proton_mail_uniffi.MessageAvailableActions
import uniffi.proton_mail_uniffi.MovableSystemFolder
import uniffi.proton_mail_uniffi.MovableSystemFolderAction
import uniffi.proton_mail_uniffi.MoveAction
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RustMessageDataSourceImplTest {

    private val userSessionRepository = mockk<UserSessionRepository>()

    private val rustMailbox: RustMailbox = mockk()
    private val rustMessageQuery: RustMessageQuery = mockk()
    private val rustMessageSearchQuery: RustMessageSearchQuery = mockk()
    private val createRustMessageAccessor = mockk<CreateRustMessageAccessor>()
    private val createRustMessageBodyAccessor = mockk<CreateRustMessageBodyAccessor>()
    private val getRustSenderImage = mockk<GetRustSenderImage>()
    private val rustMarkMessagesRead = mockk<RustMarkMessagesRead>()
    private val rustMarkMessagesUnread = mockk<RustMarkMessagesUnread>()
    private val rustStarMessages = mockk<RustStarMessages>()
    private val rustUnstarMessages = mockk<RustUnstarMessages>()
    private val getRustAllBottomBarActions = mockk<GetRustAllMessageBottomBarActions>()
    private val rustDeleteMessages = mockk<RustDeleteMessages>()
    private val rustMoveMessages = mockk<RustMoveMessages>()
    private val rustLabelMessages = mockk<RustLabelMessages>()
    private val getRustAvailableMessageActions = mockk<GetRustAvailableMessageActions>()
    private val getRustMessageMoveToActions = mockk<GetRustMessageMoveToActions>()
    private val getRustMessageLabelAsActions = mockk<GetRustMessageLabelAsActions>()

    private val dataSource = RustMessageDataSourceImpl(
        userSessionRepository,
        rustMailbox,
        rustMessageQuery,
        rustMessageSearchQuery,
        createRustMessageAccessor,
        createRustMessageBodyAccessor,
        getRustSenderImage,
        rustMarkMessagesRead,
        rustMarkMessagesUnread,
        rustStarMessages,
        rustUnstarMessages,
        getRustAllBottomBarActions,
        rustDeleteMessages,
        rustMoveMessages,
        rustLabelMessages,
        getRustAvailableMessageActions,
        getRustMessageMoveToActions,
        getRustMessageLabelAsActions
    )

    @Test
    fun `get message should return message metadata`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val messageId = LocalMessageIdSample.AugWeatherForecast
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { createRustMessageAccessor(mailSession, messageId) } returns LocalMessageTestData.AugWeatherForecast

        // When
        val result = dataSource.getMessage(userId, messageId)

        // Then
        assertEquals(LocalMessageTestData.AugWeatherForecast, result)
    }

    @Test
    fun `get message should handle session exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        val messageId = LocalMessageIdSample.AugWeatherForecast
        coEvery {
            createRustMessageAccessor.invoke(mailSession, messageId)
        } throws MailSessionException.Io("Db failure")

        // When
        val result = dataSource.getMessage(userId, messageId)

        // Then
        assertNull(result)
    }

    @Test
    fun `get message body should return decrypted message body`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val labelId = LocalLabelId(1uL)
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<Mailbox>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } returns mockk()

        // When
        val result = dataSource.getMessageBody(userId, messageId, labelId)

        // Then
        verify { rustMailbox.observeMailbox(labelId) }
        coVerify { createRustMessageBodyAccessor(mailbox, messageId) }
        assert(result != null)
    }

    @Test
    fun `get message body should handle mailbox exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<Mailbox>()
        every { rustMailbox.observeMailbox() } returns flowOf(mailbox)
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } throws MailboxException.Io("DB Exception")
        // When
        val result = dataSource.getMessageBody(userId, messageId, null)

        // Then
        verify { rustMailbox.observeMailbox() }
        assertNull(result)
    }

    @Test
    fun `get messages should return list of message metadata`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        val pageKey = PageKey.DefaultPageKey()
        val messages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        coEvery { rustMessageQuery.getMessages(userId, pageKey) } returns messages

        // When
        val result = dataSource.getMessages(userId, pageKey)

        // Then
        coVerify { rustMessageQuery.getMessages(userId, pageKey) }
        assertEquals(messages, result)
    }

    @Test
    fun `get messages should return search results when pagekey contains search query`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        val pageKey = PageKey.PageKeyForSearch("search query")
        val messages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        coEvery { rustMessageSearchQuery.getMessages(userId, pageKey) } returns messages

        // When
        val result = dataSource.getMessages(userId, pageKey)

        // Then
        coVerify { rustMessageSearchQuery.getMessages(userId, pageKey) }
        assertEquals(messages, result)
    }

    @Test
    fun `getSenderImage should return sender image when session is available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val address = "test@example.com"
        val bimi = "bimiSelector"
        val expectedImage = "image.png"

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { getRustSenderImage(mailSession, address, bimi) } returns expectedImage

        // When
        val result = dataSource.getSenderImage(userId, address, bimi)

        // Then
        coVerify { getRustSenderImage(mailSession, address, bimi) }
        assertEquals(expectedImage, result)
    }

    @Test
    fun `getSenderImage should return null when session is not available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val address = "test@example.com"
        val bimi = "bimiSelector"

        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.getSenderImage(userId, address, bimi)

        // Then
        coVerify(exactly = 0) { getRustSenderImage(any(), any(), any()) }
        assertNull(result)
    }

    @Test
    fun `getSenderImage should return null when exception occurs`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val address = "test@example.com"
        val bimi = "bimiSelector"

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery {
            getRustSenderImage(
                mailSession,
                address,
                bimi
            )
        } throws MailSessionException.Other("Some error")

        // When
        val result = dataSource.getSenderImage(userId, address, bimi)

        // Then
        assertNull(result)
    }

    @Test
    fun `should mark messages as read when session and labelId are available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)
        coEvery { rustMarkMessagesRead(mailbox, messageIds) } just Runs

        // When
        val result = dataSource.markRead(userId, messageIds)

        // Then
        assertTrue(result.isRight())
        coVerify { rustMarkMessagesRead(mailbox, messageIds) }
    }

    @Test
    fun `should not mark messages as read when mailbox is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMailbox.observeMailbox() } returns flowOf()

        // When
        val result = dataSource.markRead(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        verify { rustMarkMessagesRead wasNot Called }
    }

    @Test
    fun `should handle exception when marking messages as read`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMarkMessagesRead(mailbox, messageIds) } throws MailSessionException.Other("Error")
        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)

        // When
        val result = dataSource.markRead(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
    }

    @Test
    fun `should mark messages as unread when session and labelId are available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)
        coEvery { rustMarkMessagesUnread(mailbox, messageIds) } just Runs

        // When
        val result = dataSource.markUnread(userId, messageIds)

        // Then
        assertTrue(result.isRight())
        coVerify { rustMarkMessagesUnread(mailbox, messageIds) }
    }

    @Test
    fun `should star messages when session is available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustStarMessages(mailSession, messageIds) } just Runs

        // When
        val result = dataSource.starMessages(userId, messageIds)

        // Then
        coVerify { rustStarMessages(mailSession, messageIds) }
        assert(result.isRight())
    }

    @Test
    fun `should not mark messages as unread when mailbox is not available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMailbox.observeMailbox() } returns flowOf()

        // When
        val result = dataSource.markUnread(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        verify { rustMarkMessagesUnread wasNot Called }
    }

    @Test
    fun `should handle exception when marking messages as unread`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMarkMessagesUnread(mailbox, messageIds) } throws MailSessionException.Other("Error")
        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)

        // When
        val result = dataSource.markUnread(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
    }

    @Test
    fun `should unstar messages when session is available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustUnstarMessages(mailSession, messageIds) } just Runs

        // When
        val result = dataSource.unStarMessages(userId, messageIds)

        // Then
        coVerify { rustUnstarMessages(mailSession, messageIds) }
        assert(result.isRight())
    }

    @Test
    fun `should not unstar messages when session is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.unStarMessages(userId, messageIds)

        // Then
        coVerify(exactly = 0) { rustUnstarMessages(any(), any()) }
        assert(result.isLeft())
    }

    @Test
    fun `should handle exception when unstarring messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustUnstarMessages(mailSession, messageIds) } throws MailSessionException.Other("Error")

        // When
        val result = dataSource.unStarMessages(userId, messageIds)

        // Then
        coVerify { rustUnstarMessages(mailSession, messageIds) }
        assert(result.isLeft())
    }

    @Test
    fun `get available actions should return available message actions`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageIdSample.AugWeatherForecast)
        val expected = MessageAvailableActions(emptyList(), emptyList(), emptyList(), emptyList())

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustAvailableMessageActions(mailbox, messageIds) } returns expected

        // When
        val result = dataSource.getAvailableActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `get available system move to actions should return only available actions towards system folders`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageIdSample.AugWeatherForecast)
        val archive = MovableSystemFolderAction(Id(2uL), MovableSystemFolder.ARCHIVE)
        val customFolder = CustomFolderAction(
            Id(100uL),
            "custom",
            LabelColor("#fff"),
            emptyList()
        )
        val allMoveToActions = listOf(MoveAction.SystemFolder(archive), MoveAction.CustomFolder(customFolder))
        val expected = listOf(MoveAction.SystemFolder(archive))

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustMessageMoveToActions(mailbox, messageIds) } returns allMoveToActions

        // When
        val result = dataSource.getAvailableSystemMoveToActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `get available label as actions should return available message actions`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageIdSample.AugWeatherForecast)
        val expected = listOf(
            LabelAsAction(Id(1uL), "label", LabelColor("#fff"), IsSelected.UNSELECTED),
            LabelAsAction(Id(2uL), "label2", LabelColor("#000"), IsSelected.SELECTED)
        )

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustMessageLabelAsActions(mailbox, messageIds) } returns expected

        // When
        val result = dataSource.getAvailableLabelAsActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `get all available bottom bar actions should return all available bottom bar actions`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageIdSample.AugWeatherForecast)
        val expected = AllBottomBarMessageActions(emptyList(), emptyList())

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustAllBottomBarActions(mailbox, messageIds) } returns expected

        // When
        val result = dataSource.getAllAvailableBottomBarActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected.right(), result)
    }

    @Test
    fun `get all available bottom bar actions should return error when rust throws exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageIdSample.AugWeatherForecast)
        val expected = DataError.Local.Unknown

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustAllBottomBarActions(mailbox, messageIds) } throws MailboxException.Other("fail")

        // When
        val result = dataSource.getAllAvailableBottomBarActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected.left(), result)
    }

    @Test
    fun `should not delete messages when mailbox is not available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMailbox.observeMailbox() } returns flowOf()

        // When
        val result = dataSource.deleteMessages(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        verify { rustDeleteMessages wasNot Called }
    }

    @Test
    fun `should handle exception when deleting messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustDeleteMessages(mailbox, messageIds) } throws MailSessionException.Other("Error")
        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)

        // When
        val result = dataSource.deleteMessages(userId, messageIds)

        // Then
        assertEquals(DataError.Local.Unknown.left(), result)
    }

    @Test
    fun `should not move messages when mailbox is not available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMailbox.observeMailbox() } returns flowOf()

        // When
        val result = dataSource.moveMessages(userId, messageIds, labelId)

        // Then
        assertTrue(result.isLeft())
        verify { rustMoveMessages wasNot Called }
    }

    @Test
    fun `should handle exception when moving messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailbox = mockk<Mailbox>()
        val labelId = LocalLabelId(1uL)
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { rustMoveMessages(mailbox, labelId, messageIds) } throws MailSessionException.Other("Error")
        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)

        // When
        val result = dataSource.moveMessages(userId, messageIds, labelId)

        // Then
        assertEquals(DataError.Local.Unknown.left(), result)
    }

    @Test
    fun `should label messages when mailbox is available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))
        val selectedLabelIds = listOf(LocalLabelId(3uL), LocalLabelId(4uL))
        val partiallySelectedLabelIds = listOf(LocalLabelId(5uL))
        val shouldArchive = false
        val mailbox = mockk<Mailbox>()

        coEvery {
            rustLabelMessages(mailbox, messageIds, selectedLabelIds, partiallySelectedLabelIds, shouldArchive)
        } returns Unit
        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)

        // When
        val result = dataSource.labelMessages(
            userId,
            messageIds,
            selectedLabelIds,
            partiallySelectedLabelIds,
            shouldArchive
        )

        // Then
        assertTrue(result.isRight())
        coVerify { rustLabelMessages(mailbox, messageIds, selectedLabelIds, partiallySelectedLabelIds, shouldArchive) }
    }

    @Test
    fun `should not label messages when mailbox is not available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))
        val selectedLabelIds = listOf(LocalLabelId(3uL), LocalLabelId(4uL))
        val partiallySelectedLabelIds = listOf(LocalLabelId(5uL))
        val shouldArchive = false

        coEvery { rustMailbox.observeMailbox() } returns flowOf()

        // When
        val result = dataSource.labelMessages(
            userId,
            messageIds,
            selectedLabelIds,
            partiallySelectedLabelIds,
            shouldArchive
        )

        // Then
        assertTrue(result.isLeft())
        verify { rustLabelMessages wasNot Called }
    }

    @Test
    fun `should handle exception when labelling messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailbox = mockk<Mailbox>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))
        val selectedLabelIds = listOf(LocalLabelId(3uL), LocalLabelId(4uL))
        val partiallySelectedLabelIds = listOf(LocalLabelId(5uL))
        val shouldArchive = false

        coEvery {
            rustLabelMessages(mailbox, messageIds, selectedLabelIds, partiallySelectedLabelIds, shouldArchive)
        } throws MailSessionException.Other("Error")
        coEvery { rustMailbox.observeMailbox() } returns flowOf(mailbox)

        // When
        val result = dataSource.labelMessages(
            userId,
            messageIds,
            selectedLabelIds,
            partiallySelectedLabelIds,
            shouldArchive
        )

        // Then
        assertEquals(DataError.Local.Unknown.left(), result)
    }
}
