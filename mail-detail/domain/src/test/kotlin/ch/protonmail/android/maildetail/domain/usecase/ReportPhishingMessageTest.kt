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

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportPhishingMessageTest {

    private val messageRepository = mockk<MessageRepository>()

    private val reportPhishingMessage = ReportPhishingMessage(messageRepository)

    @Test
    fun `returns success when report phishing succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.AugWeatherForecast
        coEvery { messageRepository.reportPhishing(userId, messageId) } returns Unit.right()

        // When
        val actual = reportPhishingMessage(userId, messageId)

        // Then
        coVerify { messageRepository.reportPhishing(userId, messageId) }
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when report phishing fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.AugWeatherForecast
        val expected = DataError.Local.CryptoError
        coEvery { messageRepository.reportPhishing(userId, messageId) } returns expected.left()

        // When
        val actual = reportPhishingMessage(userId, messageId)

        // Then
        assertEquals(expected.left(), actual)
    }

}
