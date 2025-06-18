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

package ch.protonmail.android.mailpinlock.domain.autolock.usecase

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

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPending
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.domain.usecase.ShouldPresentPinInsertionScreen
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ShouldPresentPinInsertionScreenTest {

    private val autoLockRepository = mockk<AutoLockRepository>()

    private val autoLockCheckPendingState = spyk(AutoLockCheckPendingState())

    private fun useCase() = ShouldPresentPinInsertionScreen(
        autoLockRepository,
        autoLockCheckPendingState = autoLockCheckPendingState
    )

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should not trigger pin request when shouldShowPin is false`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns false.right()

        // When
        val result = useCase().invoke().first()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should not trigger pin request when it is already completed`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns true.right()
        autoLockCheckPendingState.emitCheckPendingState(AutoLockCheckPending(false))

        // When
        val result = useCase().invoke().first()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should trigger pin request when it's not already completed`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns true.right()
        autoLockCheckPendingState.emitCheckPendingState(AutoLockCheckPending(true))

        // When
        val result = useCase().invoke().first()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should not trigger pin request when it's not necessary`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns false.right()
        autoLockCheckPendingState.emitCheckPendingState(AutoLockCheckPending(true))

        // When
        val result = useCase().invoke().first()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should not trigger pin request on pending attempt but shouldAutoLock fails`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns DataError.Local.NoDataCached.left()
        autoLockCheckPendingState.emitCheckPendingState(AutoLockCheckPending(true))

        // When
        val result = useCase().invoke().first()

        // Then
        assertFalse(result)
    }
}
