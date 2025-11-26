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

package ch.protonmail.android.mailcrashrecord.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcrashrecord.domain.model.MessageBodyWebViewCrash
import ch.protonmail.android.mailcrashrecord.domain.repository.CrashRecordRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue

class HasMessageBodyWebViewCrashedTest {

    private val crashRecordRepository = mockk<CrashRecordRepository>()

    private val hasMessageBodyWebViewCrashed = HasMessageBodyWebViewCrashed(crashRecordRepository)

    @Test
    fun `should call repository method when checking if message body web view has crashed`() = runTest {
        // Given
        coEvery { crashRecordRepository.get() } returns MessageBodyWebViewCrash(hasCrashed = true).right()

        // When
        val actual = hasMessageBodyWebViewCrashed()

        // Then
        coVerify { crashRecordRepository.get() }
        assertTrue(actual)
    }
}
