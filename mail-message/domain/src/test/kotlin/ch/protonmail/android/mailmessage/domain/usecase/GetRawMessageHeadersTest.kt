/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailmessage.domain.model.RawMessageData
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRawMessageHeadersTest {

    private val messageBodyRepository = mockk<MessageBodyRepository>()

    private val getRawMessageHeaders = GetRawMessageHeaders(messageBodyRepository)

    @Test
    fun `should call repository method when use case is invoked`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = MessageIdSample.AugWeatherForecast
        val rawHeaders = RawMessageData("raw headers")
        coEvery { messageBodyRepository.getRawHeaders(userId, messageId) } returns rawHeaders.right()

        // When
        val actual = getRawMessageHeaders(userId, messageId)

        // Then
        coVerify { messageBodyRepository.getRawHeaders(userId, messageId) }
        assertEquals(rawHeaders.right(), actual)
    }
}
