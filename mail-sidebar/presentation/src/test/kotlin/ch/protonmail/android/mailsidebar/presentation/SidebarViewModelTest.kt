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

package ch.protonmail.android.mailsidebar.presentation

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.AppInformation
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabels
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveMailLabels
import ch.protonmail.android.maillabel.domain.usecase.UpdateLabelExpandedState
import ch.protonmail.android.maillabel.presentation.MailLabelsUiModel
import ch.protonmail.android.mailmailbox.domain.usecase.ObserveUnreadCounters
import ch.protonmail.android.mailmessage.domain.model.UnreadCounter
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsidebar.presentation.SidebarViewModel.Action.LabelAction
import ch.protonmail.android.mailsidebar.presentation.SidebarViewModel.State.Disabled
import ch.protonmail.android.mailsidebar.presentation.SidebarViewModel.State.Enabled
import ch.protonmail.android.mailsidebar.presentation.label.SidebarLabelAction.Collapse
import ch.protonmail.android.mailsidebar.presentation.label.SidebarLabelAction.Expand
import ch.protonmail.android.mailsidebar.presentation.label.SidebarLabelAction.Select
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import ch.protonmail.android.testdata.user.UserTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.PaymentManager
import org.junit.Before
import org.junit.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals

class SidebarViewModelTest {

    private val appInformation = mockk<AppInformation>()

    private val selectedMailLabelId = mockk<SelectedMailLabelId> {
        every { this@mockk.flow } returns MutableStateFlow<MailLabelId>(MailLabelId.System(SystemLabelId.Inbox.labelId))
        every { this@mockk.set(any()) } returns Unit
    }

    private val primaryUserId = MutableStateFlow<UserId?>(null)
    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk() } returns primaryUserId
    }

    private val mailboxLabels = MutableStateFlow(MailLabels.Initial)
    private val observeMailboxLabels = mockk<ObserveMailLabels> {
        every { this@mockk(any<UserId>()) } returns mailboxLabels
    }

    private val updateLabelExpandedState = mockk<UpdateLabelExpandedState>(relaxUnitFun = true)

    private val observeUnreadCounters = mockk<ObserveUnreadCounters> {
        coEvery { this@mockk.invoke(any()) } returns flowOf(emptyList<UnreadCounter>())
    }
    private val paymentManager = mockk<PaymentManager> {
        coEvery { this@mockk.isSubscriptionAvailable(userId = any()) } returns false
    }

    private lateinit var sidebarViewModel: SidebarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        sidebarViewModel = SidebarViewModel(
            appInformation = appInformation,
            selectedMailLabelId = selectedMailLabelId,
            updateLabelExpandedState = updateLabelExpandedState,
            observePrimaryUserId = observePrimaryUserId,
            observeMailLabels = observeMailboxLabels,
            observeUnreadCounters = observeUnreadCounters,
            reportAProblemEnabled = flowOf(false)
        )
    }

    @Test
    fun `emits initial sidebar state when data is being loaded`() = runTest {
        // When
        sidebarViewModel.state.test {
            // Initial state is Disabled.
            assertEquals(Disabled, awaitItem())

            // Given
            primaryUserId.emit(UserIdTestData.Primary)

            // Then
            val actual = awaitItem() as Enabled
            val expected = Enabled(
                selectedMailLabelId = MailLabelId.System(SystemLabelId.Inbox.labelId),
                canChangeSubscription = false,
                mailLabels = MailLabelsUiModel.Loading
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    @Ignore("can change subscription is hardcoded awaiting for account to expose it through rust lib")
    fun `state is can change subscriptions when payment manager subscription available is true`() = runTest {
        sidebarViewModel.state.test {
            // Initial state is Disabled.
            assertEquals(Disabled, awaitItem())

            // Given
            coEvery { paymentManager.isSubscriptionAvailable(UserIdTestData.userId) } returns true
            primaryUserId.emit(UserIdTestData.Primary)

            // Then
            val actual = awaitItem() as Enabled
            val expected = Enabled(
                selectedMailLabelId = MailLabelId.System(SystemLabelId.Inbox.labelId),
                canChangeSubscription = true,
                mailLabels = MailLabelsUiModel.Loading
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `state is can't change subscription when payment manager subscription available is false`() = runTest {
        sidebarViewModel.state.test {
            // Initial state is Disabled.
            assertEquals(Disabled, awaitItem())

            // Given
            coEvery { paymentManager.isSubscriptionAvailable(UserIdTestData.adminUserId) } returns false
            primaryUserId.emit(UserIdTestData.adminUserId)

            // Then
            val actual = awaitItem() as Enabled
            val expected = Enabled(
                selectedMailLabelId = MailLabelId.System(SystemLabelId.Inbox.labelId),
                canChangeSubscription = false,
                mailLabels = MailLabelsUiModel.Loading
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `onSidebarLabelAction Select Archive, set selectedMailLabelId`() = runTest {
        // When
        sidebarViewModel.submit(LabelAction(Select(MailLabelTestData.archiveSystemLabel.id)))

        // Then
        verify { selectedMailLabelId.set(MailLabelTestData.archiveSystemLabel.id) }
    }

    @Test
    fun `onSidebarLabelAction Collapse, call updateLabelExpandedState`() = runTest {
        // Given
        val mailLabelId = MailLabelId.Custom.Folder(LabelId("folder"))
        primaryUserId.emit(UserIdTestData.Primary)

        // When
        sidebarViewModel.submit(LabelAction(Collapse(mailLabelId)))

        // Then
        coVerify { updateLabelExpandedState.invoke(UserTestData.Primary.userId, mailLabelId, false) }
    }

    @Test
    fun `onSidebarLabelAction Expand, call updateLabelExpandedState`() = runTest {
        // Given
        val mailLabelId = MailLabelId.Custom.Folder(LabelId("folder"))
        primaryUserId.emit(UserIdTestData.Primary)

        // When
        sidebarViewModel.submit(LabelAction(Expand(mailLabelId)))

        // Then
        coVerify { updateLabelExpandedState.invoke(UserTestData.Primary.userId, mailLabelId, true) }
    }
}
