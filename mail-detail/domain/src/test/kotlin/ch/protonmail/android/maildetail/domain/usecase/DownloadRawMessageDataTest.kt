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

package ch.protonmail.android.maildetail.domain.usecase

import android.net.Uri
import arrow.core.right
import ch.protonmail.android.maildetail.domain.repository.RawMessageDataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DownloadRawMessageDataTest {

    private val rawMessageDataRepository = mockk<RawMessageDataRepository>()

    private val downloadRawMessageData = DownloadRawMessageData(rawMessageDataRepository)

    @Test
    fun `should call repository method when use case is called`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val data = "raw headers"
        coEvery { rawMessageDataRepository.downloadRawData(uri, data) } returns Unit.right()

        // When
        val actual = downloadRawMessageData(uri, data)

        // Then
        coVerify { rawMessageDataRepository.downloadRawData(uri, data) }
        assertEquals(Unit.right(), actual)
    }
}
