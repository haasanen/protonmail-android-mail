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
import ch.protonmail.android.mailmessage.domain.model.AttachmentDataError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadMessageBodyImageTest {

    private val userId = UserIdSample.Primary
    private val messageId = MessageIdSample.Invoice
    private val url = "imageUrl"

    private val messageBodyRepository = mockk<MessageBodyRepository>()

    private val loadMessageBodyImage = LoadMessageBodyImage(messageBodyRepository)

    @Test
    fun `returns image when loading it is successful`() = runTest {
        // Given
        val expectedByteArray = "I'm a bytearray".toByteArray()
        val expected = MessageBodyImage(expectedByteArray, "image/png").right()
        coEvery {
            messageBodyRepository.loadImage(userId, messageId, url, shouldLoadImagesSafely = true)
        } returns expected

        // When
        val actual = loadMessageBodyImage(userId, messageId, url, shouldLoadImagesSafely = true)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `returns error when loading image fails`() = runTest {
        // Given
        coEvery { messageBodyRepository.loadImage(userId, messageId, url, shouldLoadImagesSafely = true) } returns
            AttachmentDataError.Other(DataError.Local.NoDataCached).left()

        // When
        val actual = loadMessageBodyImage(userId, messageId, url, shouldLoadImagesSafely = true)

        // Then
        assertEquals(AttachmentDataError.Other(DataError.Local.NoDataCached).left(), actual)
    }
}
