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

package ch.protonmail.android.mailcommon.data.worker

import androidx.work.Operation
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

internal class CancelWorkManagerWorkTest {

    private val workManager = mockk<WorkManager>()
    private val cancelWorkManagerWork = CancelWorkManagerWork(workManager)

    @Test
    fun `should proxy the cancellation call to WorkManager`() {
        // Given
        val tag = "tag"
        val operation = mockk<Operation>()
        every { workManager.cancelAllWorkByTag(tag) } returns operation

        // When
        cancelWorkManagerWork.cancelAllWorkByTag(tag)

        // Then
        verify(exactly = 1) { workManager.cancelAllWorkByTag(tag) }
    }
}
