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

package ch.protonmail.android.maildetail.domain.usecase

import app.cash.turbine.test
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.LabelIdSample
import ch.protonmail.android.mailcommon.domain.sample.LabelSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageSample
import ch.protonmail.android.mailmessage.domain.sample.MessageWithLabelsSample
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveConversationMessagesWithLabelsTest {

    private val messageLabelsFlow = flowOf(listOf(LabelSample.Archive))
    private val messageFoldersFlow = flowOf(listOf(LabelSample.Document))
    private val labelRepository: LabelRepository = mockk {
        every { observeCustomLabels(userId = UserIdSample.Primary) } returns messageLabelsFlow
        every { observeCustomFolders(userId = UserIdSample.Primary) } returns messageFoldersFlow
    }
    private val messageRepository: MessageRepository = mockk {
        every { observeConversationMessages(UserIdSample.Primary, ConversationIdSample.WeatherForecast) } returns
            flowOf(nonEmptyListOf(MessageSample.AugWeatherForecast).right())
    }
    private val observeConversationMessagesWithLabels = ObserveConversationMessagesWithLabels(
        labelRepository = labelRepository,
        messageRepository = messageRepository
    )

    @Test
    fun `when messages and labels are emitted, right model is emitted`() = runTest {
        // given
        val expected = nonEmptyListOf(MessageWithLabelsSample.AugWeatherForecast).right()
        every { labelRepository.observeCustomLabels(UserIdSample.Primary) } returns flowOf(emptyList())
        every { labelRepository.observeCustomFolders(UserIdSample.Primary) } returns flowOf(emptyList())

        // when
        observeConversationMessagesWithLabels(
            UserIdSample.Primary,
            ConversationIdSample.WeatherForecast
        ).test {

            // then
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `model contains only the correct labels and folders`() = runTest {
        // given
        val message = MessageSample.Invoice.copy(
            labelIds = listOf(LabelIdSample.Archive, LabelIdSample.Document)
        )
        val messageWithLabels = MessageWithLabelsSample.build(
            message = message,
            labels = listOf(LabelSample.Archive, LabelSample.Document).sortedBy { it.order }
        )
        val allLabels = listOf(LabelSample.Document, LabelSample.News)
        val allFolders = listOf(LabelSample.Archive, LabelSample.Inbox)
        every { labelRepository.observeCustomLabels(UserIdSample.Primary) } returns flowOf(allLabels)
        every { labelRepository.observeCustomFolders(UserIdSample.Primary) } returns flowOf(allFolders)
        val expected = nonEmptyListOf(messageWithLabels).right()

        every {
            messageRepository.observeConversationMessages(
                UserIdSample.Primary, ConversationIdSample.Invoices
            )
        } returns
            flowOf(nonEmptyListOf(message).right())

        // when
        observeConversationMessagesWithLabels(
            UserIdSample.Primary,
            ConversationIdSample.Invoices
        ).test {

            // then
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when messages emits an error, the error is emitted`() = runTest {
        // given
        val error = DataError.Local.NoDataCached.left()
        every {
            messageRepository.observeConversationMessages(
                UserIdSample.Primary,
                ConversationIdSample.WeatherForecast
            )
        } returns flowOf(error)

        // when
        observeConversationMessagesWithLabels(
            UserIdSample.Primary,
            ConversationIdSample.WeatherForecast
        ).test {

            // then
            assertEquals(error, awaitItem())
            awaitComplete()
        }
    }
}
