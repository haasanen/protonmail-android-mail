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

package ch.protonmail.android.navigation.deeplinks

import java.util.UUID
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.network.NetworkManager
import ch.protonmail.android.mailcommon.domain.network.NetworkStatus
import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.FindLocalSystemLabelId
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.RemoteMessageId
import ch.protonmail.android.mailmessage.domain.sample.MessageSample.AlphaAppQAReport
import ch.protonmail.android.mailmessage.domain.usecase.ObserveMessage
import ch.protonmail.android.mailsession.data.mapper.toUserId
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsession.domain.usecase.SetPrimaryAccount
import ch.protonmail.android.navigation.deeplinks.NotificationsDeepLinksViewModel.State.NavigateToConversation
import ch.protonmail.android.navigation.deeplinks.NotificationsDeepLinksViewModel.State.NavigateToInbox
import ch.protonmail.android.testdata.account.AccountTestSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.core.domain.entity.UserId
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class NotificationsDeepLinksViewModelTest {

    private val networkManager: NetworkManager = mockk {
        coEvery { networkStatus } returns NetworkStatus.Unmetered
    }
    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val userSessionRepository = mockk<UserSessionRepository>()
    private val setPrimaryAccount = mockk<SetPrimaryAccount>()
    private val observeMessage = mockk<ObserveMessage>()
    private val findLocalSystemLabelId = mockk<FindLocalSystemLabelId>()

    private lateinit var viewModel: NotificationsDeepLinksViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = NotificationsDeepLinksViewModel(
            networkManager = networkManager,
            observePrimaryUserId = observePrimaryUserId,
            userSessionRepository = userSessionRepository,
            observeMessage = observeMessage,
            setPrimaryAccount = setPrimaryAccount,
            findLocalSystemLabelId = findLocalSystemLabelId
        )
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should emit navigate to inbox with active user when provided userId is already the primary`() = runTest {
        // Given
        val userId = getUserId()
        expectPrimaryId(userId)

        // When
        viewModel.navigateToInbox(userId.id)

        // Then
        viewModel.state.test {
            assertEquals(NavigateToInbox.ActiveUser, awaitItem())
        }
    }

    @Test
    fun `should emit navigate to conversation details when deeplink is resolved and user is online`() = runTest {
        // Given
        val userId = getUserId()
        val messageId = getRemoteMessageId()
        val labelId = LabelId("1")
        val expectedEvent = NavigateToConversation(
            conversationId = AlphaAppQAReport.conversationId,
            contextLabelId = labelId,
            scrollToMessageId = AlphaAppQAReport.messageId
        )

        expectPrimaryId(userId)
        expectMessage(userId, messageId, AlphaAppQAReport)

        // When
        viewModel.navigateToMessage(messageId.id, userId.id)

        // Then
        viewModel.state.test {
            assertEquals(expectedEvent, awaitItem())
        }
    }

    @Test
    fun `should emit navigate to inbox when the user is offline and taps a message deeplink`() = runTest {
        // Given
        val userId = getUserId()
        val messageId = getRemoteMessageId()

        coEvery { networkManager.networkStatus } returns NetworkStatus.Disconnected
        expectPrimaryId(userId)

        // When
        viewModel.navigateToMessage(messageId.id, userId.id)

        // Then
        viewModel.state.test {
            assertEquals(NavigateToInbox.ActiveUser, awaitItem())
        }
    }

    @Test
    fun `should navigate to the inbox if there is an error retrieving the local messages`() = runTest {
        // Given
        val userId = getUserId()
        val messageId = getRemoteMessageId()

        expectPrimaryId(userId)
        expectMessageError(userId, messageId)

        // When
        viewModel.navigateToMessage(messageId.id, userId.id)

        // Then
        viewModel.state.test {
            assertEquals(NavigateToInbox.ActiveUser, awaitItem())
        }
    }

    @Test
    fun `should switch account and emit switched for inbox notification to an active non primary account`() = runTest {
        // Given
        val activeAccount = AccountTestSample.Primary.copy(primaryAddress = "test@email.com")
        val notificationUserId = getUserId()
        val secondaryAccount = AccountTestSample.Primary.copy(userId = notificationUserId)

        expectPrimaryId(activeAccount.userId)
        expectAccounts(userId = notificationUserId, account = secondaryAccount)
        coEvery { setPrimaryAccount(notificationUserId) } just runs

        // When
        viewModel.navigateToInbox(notificationUserId.id)

        // Then
        viewModel.state.test {
            assertEquals(NavigateToInbox.ActiveUserSwitched(secondaryAccount.primaryAddress!!), awaitItem())
            coVerify { setPrimaryAccount(secondaryAccount.userId) }
        }
    }

    @Test
    fun `should switch account and emit switched for message notification to active non primary account`() = runTest {
        // Given
        val activeAccount = AccountTestSample.Primary.copy(primaryAddress = "test@email.com")
        val notificationUserId = getUserId()
        val secondaryAccount = AccountTestSample.Primary.copy(userId = notificationUserId)
        val messageId = getRemoteMessageId()
        val labelId = LabelId("1")

        val expectedEvent = NavigateToConversation(
            conversationId = AlphaAppQAReport.conversationId,
            userSwitchedEmail = AccountTestSample.Primary.primaryAddress,
            contextLabelId = labelId,
            scrollToMessageId = AlphaAppQAReport.messageId
        )

        expectPrimaryId(activeAccount.userId)
        expectAccounts(userId = notificationUserId, account = secondaryAccount)
        expectMessage(secondaryAccount.userId, messageId, AlphaAppQAReport)
        coEvery { setPrimaryAccount(notificationUserId) } just runs

        // When
        viewModel.navigateToMessage(messageId.id, secondaryAccount.userId.id)

        // Then
        viewModel.state.test {
            assertEquals(expectedEvent, awaitItem())
            coVerify { setPrimaryAccount(secondaryAccount.userId) }
        }
    }

    @Test
    fun `should fallback to all mail when message exclusive location can't be resolved`() = runTest {
        // Given
        val messageId = getRemoteMessageId()
        val userId = getUserId()
        val labelId = LabelId("resolvedAllMail")
        val message = AlphaAppQAReport.copy(exclusiveLocation = ExclusiveLocation.NoLocation)
        val expectedEvent = NavigateToConversation(
            conversationId = message.conversationId,
            userSwitchedEmail = null,
            contextLabelId = labelId,
            scrollToMessageId = message.messageId
        )

        expectPrimaryId(userId)
        expectMessage(userId, messageId, message)
        coEvery { findLocalSystemLabelId(userId, SystemLabelId.AllMail) } returns MailLabelId.System(labelId)

        // When
        viewModel.navigateToMessage(messageId.id, userId.id)

        // Then
        viewModel.state.test {
            assertEquals(expectedEvent, awaitItem())
        }
    }

    private fun getUserId() = UUID.randomUUID().toString().toUserId()
    private fun getRemoteMessageId() = RemoteMessageId(UUID.randomUUID().toString())

    private fun expectPrimaryId(userId: UserId) {
        every { observePrimaryUserId() } returns flowOf(userId)
    }

    private fun expectAccounts(userId: UserId, account: Account) {
        coEvery { userSessionRepository.getAccount(userId) } returns account
    }

    private fun expectMessage(
        userId: UserId,
        remoteMessageId: RemoteMessageId,
        message: Message
    ) {
        coEvery {
            observeMessage(userId, remoteMessageId)
        } returns flowOf(message.right())
    }

    private fun expectMessageError(userId: UserId, remoteMessageId: RemoteMessageId) {
        coEvery {
            observeMessage(userId, remoteMessageId)
        } returns flowOf(
            DataError.Local.Unknown.left()
        )
    }
}
