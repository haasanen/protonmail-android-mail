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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetEmbeddedImageTest {

    private val userId = UserIdSample.Primary
    private val messageId = MessageIdSample.Invoice
    private val contentId = "embeddedImageContentId"

    private val messageBodyRepository = mockk<MessageBodyRepository>()

    private val getEmbeddedImage = GetEmbeddedImage(messageBodyRepository)

    @Test
    fun `returns embedded image when getting it is successful`() = runTest {
        // Given
        val expectedByteArray = "I'm a bytearray".toByteArray()
        val expected = EmbeddedImage(expectedByteArray, "image/png").right()
        coEvery { messageBodyRepository.getEmbeddedImage(userId, messageId, contentId) } returns expected

        // When
        val actual = getEmbeddedImage(userId, messageId, contentId)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `returns error when get embedded image fails`() = runTest {
        // Given
        coEvery { messageBodyRepository.getEmbeddedImage(userId, messageId, contentId) } returns
            DataError.Local.NoDataCached.left()

        // When
        val actual = getEmbeddedImage(userId, messageId, contentId)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }
}
