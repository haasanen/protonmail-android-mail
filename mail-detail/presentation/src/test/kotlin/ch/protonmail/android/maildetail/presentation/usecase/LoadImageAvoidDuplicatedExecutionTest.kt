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

package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.model.AttachmentDataError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.usecase.LoadMessageBodyImage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoadImageAvoidDuplicatedExecutionTest {

    private val userId = UserIdSample.Primary
    private val messageId = MessageIdSample.Invoice
    private val url = "url"
    private val loadMessageBodyImage = mockk<LoadMessageBodyImage>()

    private val loadImageAvoidDuplicatedExecution = LoadImageAvoidDuplicatedExecution(loadMessageBodyImage)


    @Test
    fun `returns image result when loading image was successful`() = runTest {
        // Given
        val expectedByteArray = "I'm an image".toByteArray()
        val expectedImageResult = MessageBodyImage(data = expectedByteArray, mimeType = "")
        coEvery {
            loadMessageBodyImage(userId, messageId, url)
        } returns expectedImageResult.right()

        // When
        val result = loadImageAvoidDuplicatedExecution(userId, messageId, url, coroutineContext)

        // Then
        assertEquals(expectedImageResult, result)
    }

    @Test
    fun `returns null when loading image failed`() = runTest {
        // Given
        coEvery {
            loadMessageBodyImage(userId, messageId, url)
        } returns AttachmentDataError.Other(DataError.Local.NoDataCached).left()

        // When
        val result = loadImageAvoidDuplicatedExecution(userId, messageId, url, coroutineContext)

        // Then
        assertNull(result)
    }

    @Test
    fun `verify load image is called only once while it is running`() = runTest {
        // Given
        val expectedByteArray = "I'm an image".toByteArray()
        val expectedImageResult = MessageBodyImage(data = expectedByteArray, mimeType = "")
        coEvery {
            loadMessageBodyImage(userId, messageId, url)
        } coAnswers {
            expectedImageResult.right()
        }

        // When
        launch { loadImageAvoidDuplicatedExecution(userId, messageId, url, coroutineContext) }
        launch { loadImageAvoidDuplicatedExecution(userId, messageId, url, coroutineContext) }
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { loadMessageBodyImage(userId, messageId, url) }
    }
}
