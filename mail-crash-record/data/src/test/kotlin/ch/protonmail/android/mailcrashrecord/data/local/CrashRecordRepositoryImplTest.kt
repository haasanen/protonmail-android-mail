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

package ch.protonmail.android.mailcrashrecord.data.local

import arrow.core.right
import ch.protonmail.android.mailcrashrecord.domain.model.MessageBodyWebViewCrash
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CrashRecordRepositoryImplTest {

    private val crashRecordLocalDataSource = mockk<CrashRecordLocalDataSource>()

    private val crashRecordRepository = CrashRecordRepositoryImpl(crashRecordLocalDataSource)

    @Test
    fun `should call data source method when getting message body web view crash`() = runTest {
        // Given
        val expected = MessageBodyWebViewCrash(hasCrashed = true).right()
        coEvery { crashRecordLocalDataSource.get() } returns expected

        // When
        val actual = crashRecordRepository.get()

        // Then
        coVerify { crashRecordLocalDataSource.get() }
        assertEquals(expected, actual)
    }

    @Test
    fun `should call data source method when saving message body web view crash`() = runTest {
        // Given
        val crash = MessageBodyWebViewCrash(hasCrashed = true)
        coEvery { crashRecordLocalDataSource.save(crash) } returns Unit.right()

        // When
        val actual = crashRecordRepository.save(crash)

        // Then
        coVerify { crashRecordLocalDataSource.save(crash) }
        assertEquals(Unit.right(), actual)
    }
}
