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

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.domain.usecase.ShouldPresentPinInsertionScreen
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.BeforeTest

internal class ShouldPresentPinInsertionScreenTest {

    private val autoLockRepository: AutoLockRepository = mockk()
    private val autoLockCheckPendingState: AutoLockCheckPendingState = mockk()

    private val lockCheckEvents = MutableSharedFlow<Unit>()

    private lateinit var shouldPresentPinInsertionScreen: ShouldPresentPinInsertionScreen

    @BeforeTest
    fun setUp() {
        every { autoLockCheckPendingState.autoLockCheckEvents } returns lockCheckEvents

        shouldPresentPinInsertionScreen = ShouldPresentPinInsertionScreen(
            autoLockRepository,
            autoLockCheckPendingState
        )
    }

    @Test
    fun `invoke() should emit true immediately on collection due to onStart`() = runTest {
        // When + Then
        shouldPresentPinInsertionScreen().test {
            assertTrue("Expected initial emission to be true", awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `should emit true when check event is received and repository returns true`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns true.right()

        // When + Then
        shouldPresentPinInsertionScreen().test {
            awaitItem() // onStart (true)

            lockCheckEvents.emit(Unit)

            assertTrue("Expected emission after event to be true", awaitItem())
        }
    }

    @Test
    fun `should emit false when check event is received and repository returns false`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns false.right()

        // When + Then
        shouldPresentPinInsertionScreen().test {
            awaitItem() // onStart (true)

            lockCheckEvents.emit(Unit)

            assertFalse("Expected emission after event to be false", awaitItem())
        }
    }

    @Test
    fun `should emit false when check event is received and repository call fails`() = runTest {
        // Given
        coEvery { autoLockRepository.shouldAutoLock() } returns DataError.Local.NoDataCached.left()

        // When + Then
        shouldPresentPinInsertionScreen().test {
            awaitItem() // onStart (true)

            lockCheckEvents.emit(Unit)

            assertFalse("Expected fallback emission to be false on repository error", awaitItem())
        }
    }
}
