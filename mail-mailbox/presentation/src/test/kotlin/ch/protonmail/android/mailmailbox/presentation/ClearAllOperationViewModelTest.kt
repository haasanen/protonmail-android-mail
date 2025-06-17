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

package ch.protonmail.android.mailmailbox.presentation

import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabels
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveLoadedMailLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveMailLabels
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteBanner
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteState
import ch.protonmail.android.mailmailbox.domain.model.SpamOrTrash
import ch.protonmail.android.mailmailbox.domain.usecase.GetAutoDeleteBanner
import ch.protonmail.android.mailmailbox.presentation.mailbox.ClearAllOperationViewModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ClearAllOperationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk.invoke() } returns flowOf(userId)
    }

    private val flowOfLabels = MutableSharedFlow<MailLabels>()
    private val selectedMailLabelIdFlow = MutableStateFlow<MailLabelId>(MailLabelId.System(SystemLabelId.Inbox.labelId))
    private val observeMailLabels = mockk<ObserveMailLabels> {
        every { this@mockk.invoke(userId) } returns flowOfLabels
    }
    private val observeLoadedMailLabelId = mockk<ObserveLoadedMailLabelId> {
        every { this@mockk.invoke() } returns selectedMailLabelIdFlow
    }
    private val getAutoDeleteBanner = mockk<GetAutoDeleteBanner>()

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should emit upselling state when the current label is spam or trash and the feature is disabled`() = runTest {
        // Given
        every { observeMailLabels.invoke(userId) } returns flowOf(spamMailLabels)
        every { observeLoadedMailLabelId() } returns
            MutableStateFlow(MailLabelId.System(spamSystemLabelId.labelId))
        coEvery {
            getAutoDeleteBanner(userId, spamSystemLabelId.labelId)
        } returns AutoDeleteBanner(AutoDeleteState.AutoDeleteUpsell, SpamOrTrash.Spam).right()

        // When + Then
        viewModel().state.test {
            assertTrue(awaitItem() is ClearAllStateUiModel.Visible.UpsellBannerWithLink)
        }
    }

    @Test
    fun `should emit clear all state when the current label is spam or trash and the feature is enabled`() = runTest {
        // Given
        every { observeMailLabels.invoke(userId) } returns flowOf(spamMailLabels)
        every { observeLoadedMailLabelId() } returns
            MutableStateFlow(MailLabelId.System(spamSystemLabelId.labelId))
        coEvery {
            getAutoDeleteBanner(userId, spamSystemLabelId.labelId)
        } returns AutoDeleteBanner(AutoDeleteState.AutoDeleteEnabled, SpamOrTrash.Spam).right()

        // When + Then
        viewModel().state.test {
            assertTrue(awaitItem() is ClearAllStateUiModel.Visible.ClearAllBannerWithButton)
        }
    }

    private fun viewModel() = ClearAllOperationViewModel(
        observePrimaryUserId,
        observeLoadedMailLabelId,
        getAutoDeleteBanner
    )

    private companion object {

        val userId = UserId("user-id")
        val spamSystemLabelId = SystemLabelId.Spam

        val spamMailLabels = MailLabels(
            system = listOf(
                MailLabel.System(
                    id = MailLabelId.System(spamSystemLabelId.labelId),
                    systemLabelId = spamSystemLabelId,
                    order = 1
                )
            ),
            folders = emptyList(),
            labels = emptyList()
        )
    }
}
