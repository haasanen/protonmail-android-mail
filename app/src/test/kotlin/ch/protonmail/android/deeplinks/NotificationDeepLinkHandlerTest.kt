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

package ch.protonmail.android.deeplinks

import app.cash.turbine.test
import ch.protonmail.android.navigation.deeplinks.NotificationsDeepLinkData
import ch.protonmail.android.navigation.deeplinks.NotificationsDeepLinkHandler
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class NotificationDeepLinkHandlerTest {

    private lateinit var handler: NotificationsDeepLinkHandler

    @Before
    fun setup() {
        handler = NotificationsDeepLinkHandler()
    }

    @Test
    fun `initial state has no pending data`() {
        // When
        val hasPending = handler.hasPending()

        // Then
        assertFalse(hasPending)
    }

    @Test
    fun `initial state is locked`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()

        // When
        handler.setPending(testData)

        // Then
        handler.pending.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasPending returns true when data is set`() {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()

        // When
        handler.setPending(testData)

        // Then
        assertTrue(handler.hasPending())
    }

    @Test
    fun `hasPending returns false after consume is called`() {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setPending(testData)

        // When
        handler.consume()

        // Then
        assertFalse(handler.hasPending())
    }

    @Test
    fun `pending flow emits null when locked`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setLocked()

        // When
        handler.setPending(testData)

        // Then
        handler.pending.test {
            val emission = awaitItem()
            assertNull(emission)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending flow emits data when unlocked`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setUnlocked()

        // When
        handler.setPending(testData)

        // Then
        handler.pending.test {
            val emission = awaitItem()
            assertEquals(testData, emission)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending flow emits null initially when unlocked with no data`() = runTest {
        // Given
        handler.setUnlocked()

        // Then
        handler.pending.test {
            val emission = awaitItem()
            assertNull(emission)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending flow transitions from null to data when unlocked`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setUnlocked()

        handler.pending.test {
            // Initial state
            assertNull(awaitItem())

            // When
            handler.setPending(testData)

            // Then
            assertEquals(testData, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending flow transitions from data to null when locked`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setUnlocked()
        handler.setPending(testData)

        handler.pending.test {
            // Initial state with data
            assertEquals(testData, awaitItem())

            // When
            handler.setLocked()

            // Then
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending flow transitions from null to data when unlocking with existing data`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setLocked()
        handler.setPending(testData)

        handler.pending.test {
            // Initial state (locked, null)
            assertNull(awaitItem())

            // When
            handler.setUnlocked()

            // Then
            assertEquals(testData, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending flow emits null after consume is called`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setUnlocked()
        handler.setPending(testData)

        handler.pending.test {
            // Initial state with data
            assertEquals(testData, awaitItem())

            // When
            handler.consume()

            // Then
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPending replaces existing data`() = runTest {
        // Given
        val firstData = mockk<NotificationsDeepLinkData>(name = "first")
        val secondData = mockk<NotificationsDeepLinkData>(name = "second")
        handler.setUnlocked()
        handler.setPending(firstData)

        handler.pending.test {
            // Initial state with first data
            assertEquals(firstData, awaitItem())

            // When
            handler.setPending(secondData)

            // Then
            assertEquals(secondData, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple lock unlock cycles work correctly`() = runTest {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setPending(testData)

        handler.pending.test {
            // Initial state is locked (default), so first emission is null
            assertNull(awaitItem())

            // Unlock
            handler.setUnlocked()
            assertEquals(testData, awaitItem())

            // Lock
            handler.setLocked()
            assertNull(awaitItem())

            // Unlock again
            handler.setUnlocked()
            assertEquals(testData, awaitItem())

            // Lock again
            handler.setLocked()
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `consume clears data that was set while locked`() {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setLocked()
        handler.setPending(testData)

        // When
        handler.consume()

        // Then
        assertFalse(handler.hasPending())
    }

    @Test
    fun `setLocked does not clear pending data`() {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setPending(testData)

        // When
        handler.setLocked()

        // Then
        assertTrue(handler.hasPending())
    }

    @Test
    fun `setUnlocked does not clear pending data`() {
        // Given
        val testData = mockk<NotificationsDeepLinkData>()
        handler.setPending(testData)

        // When
        handler.setUnlocked()

        // Then
        assertTrue(handler.hasPending())
    }
}
