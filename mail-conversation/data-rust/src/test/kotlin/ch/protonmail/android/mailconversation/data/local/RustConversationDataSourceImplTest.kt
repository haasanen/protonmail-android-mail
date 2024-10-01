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

package ch.protonmail.android.mailconversation.data.local

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailconversation.data.usecase.GetRustAvailableConversationActions
import ch.protonmail.android.mailconversation.data.usecase.GetRustConversationLabelAsActions
import ch.protonmail.android.mailconversation.data.usecase.GetRustConversationMoveToActions
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationIdSample
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.ConversationAvailableActions
import uniffi.proton_mail_uniffi.CustomFolderAction
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.IsSelected
import uniffi.proton_mail_uniffi.LabelAsAction
import uniffi.proton_mail_uniffi.LabelColor
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import uniffi.proton_mail_uniffi.MoveAction
import uniffi.proton_mail_uniffi.SystemFolderAction
import uniffi.proton_mail_uniffi.SystemLabel
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RustConversationDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val sessionManager = mockk<UserSessionRepository>()
    private val mailSessionRepository = mockk<MailSessionRepository>()

    private val rustMailbox: RustMailbox = mockk()
    private val rustConversationDetailQuery: RustConversationDetailQuery = mockk()
    private val rustConversationsQuery: RustConversationsQuery = mockk()
    private val getRustAvailableConversationActions = mockk<GetRustAvailableConversationActions>()
    private val getRustConversationMoveToActions = mockk<GetRustConversationMoveToActions>()
    private val getRustConversationLabelAsActions = mockk<GetRustConversationLabelAsActions>()

    private val dataSource = RustConversationDataSourceImpl(
        sessionManager,
        rustMailbox,
        rustConversationDetailQuery,
        rustConversationsQuery,
        getRustAvailableConversationActions,
        getRustConversationMoveToActions,
        getRustConversationLabelAsActions,
        testCoroutineScope
    )

    @Test
    fun `get conversations should return list of conversations`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailSession>()
        val labelId = LocalLabelId(1uL)
        val pageKey = PageKey(labelId.toLabelId())
        val conversations = listOf(
            LocalConversationTestData.AugConversation,
            LocalConversationTestData.SepConversation,
            LocalConversationTestData.OctConversation
        )
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        coEvery { rustConversationsQuery.getConversations(userId, pageKey) } returns conversations

        // When
        val result = dataSource.getConversations(userId, pageKey)

        // Then
        coVerify { rustConversationsQuery.getConversations(userId, pageKey) }
        assertEquals(conversations, result)
    }

    @Test
    fun `observe conversation should return the conversation for the given id`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailSession>()
        val conversationId = LocalConversationIdSample.AugConversation
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        coEvery {
            rustConversationDetailQuery.observeConversation(userId, conversationId)
        } returns flowOf(LocalConversationTestData.AugConversation)

        // When
        val result = dataSource.observeConversation(userId, conversationId)?.first()

        // Then
        assertEquals(LocalConversationTestData.AugConversation, result)
    }

    @Test
    fun `observe conversation should handle mailbox exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailSession>()
        val conversationId = LocalConversationIdSample.AugConversation
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        coEvery {
            rustConversationDetailQuery.observeConversation(userId, conversationId)
        } throws MailboxException.Io("DB Exception")

        // When
        val result = dataSource.observeConversation(userId, conversationId)

        // Then
        assertNull(result)
    }

    @Test
    fun `observeConversationMessages should return conversation messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId = LocalConversationId(1uL)
        val messages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        val localConversationMessages = LocalConversationMessages(
            messageIdToOpen = LocalMessageIdSample.AugWeatherForecast,
            messages = messages
        )
        coEvery {
            rustConversationDetailQuery.observeConversationMessages(
                userId, conversationId
            )
        } returns flowOf(localConversationMessages)

        // When
        dataSource.observeConversationMessages(userId, conversationId).test {

            // Then
            val result = awaitItem()
            assertEquals(localConversationMessages, result)
            coVerify { rustConversationDetailQuery.observeConversationMessages(userId, conversationId) }

            awaitComplete()
        }
    }

    @Test
    fun `get available actions should return available conversation actions`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val conversationIds = listOf(LocalConversationIdSample.OctConversation)
        val expected = ConversationAvailableActions(emptyList(), emptyList(), emptyList(), emptyList())

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustAvailableConversationActions(mailbox, conversationIds) } returns expected

        // When
        val result = dataSource.getAvailableActions(userId, labelId, conversationIds)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `get available system move to actions should return only available actions towards system folders`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val conversationIds = listOf(LocalConversationIdSample.OctConversation)
        val archive = SystemFolderAction(Id(2uL), SystemLabel.ARCHIVE, IsSelected.UNSELECTED)
        val customFolder = CustomFolderAction(
            Id(100uL),
            "custom",
            LabelColor("#fff"),
            emptyList(),
            IsSelected.UNSELECTED
        )
        val allMoveToActions = listOf(MoveAction.SystemFolder(archive), MoveAction.CustomFolder(customFolder))
        val expected = listOf(MoveAction.SystemFolder(archive))

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustConversationMoveToActions(mailbox, conversationIds) } returns allMoveToActions

        // When
        val result = dataSource.getAvailableSystemMoveToActions(userId, labelId, conversationIds)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `get available label as actions should return available conversation actions`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1uL)
        val mailbox = mockk<Mailbox>()
        val conversationIds = listOf(LocalConversationIdSample.AugConversation)
        val expected = listOf(
            LabelAsAction(Id(1uL), "label", LabelColor("#fff"), IsSelected.UNSELECTED),
            LabelAsAction(Id(2uL), "label2", LabelColor("#000"), IsSelected.SELECTED)
        )

        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { getRustConversationLabelAsActions(mailbox, conversationIds) } returns expected

        // When
        val result = dataSource.getAvailableLabelAsActions(userId, labelId, conversationIds)

        // Then
        assertEquals(expected, result)
    }
}
