/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailconversation.data.local

import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ConversationScrollerRegistryTest {

    private lateinit var registry: ConversationScrollerRegistry

    private val paginator1 = mockk<ConversationPaginatorWrapper> {
        every { disconnect() } just runs
    }
    private val paginator2 = mockk<ConversationPaginatorWrapper> {
        every { disconnect() } just runs
    }

    @Before
    fun setUp() {
        registry = ConversationScrollerRegistry()
    }

    @Test
    fun `disconnect all registered scrollers`() = runTest {
        // Given
        registry.register(paginator1)
        registry.register(paginator2)

        // When
        registry.disconnectAll()

        // Then
        verify(exactly = 1) { paginator1.disconnect() }
        verify(exactly = 1) { paginator2.disconnect() }

        // And when called again, nothing else happens
        registry.disconnectAll()
        verify(exactly = 1) { paginator1.disconnect() }
        verify(exactly = 1) { paginator2.disconnect() }
    }

    @Test
    fun `disconnect will not be called for an unregistered scroller`() = runTest {
        // Given
        registry.register(paginator1)
        registry.register(paginator2)
        registry.unregister(paginator2)

        // When
        registry.disconnectAll()

        // Then
        verify(exactly = 1) { paginator1.disconnect() }
        verify(exactly = 0) { paginator2.disconnect() }
    }

    @Test
    fun `disconnect will be called once when the same scroller registered twice`() = runTest {
        // Given
        registry.register(paginator1)
        registry.register(paginator1)

        // When
        registry.disconnectAll()

        // Then
        verify(exactly = 1) { paginator1.disconnect() }
    }

    @Test
    fun `unregister runs silently when scroller is not in registry`() = runTest {
        // Given
        registry.register(paginator1)

        // When
        registry.unregister(paginator2)

        // Then
        verify(exactly = 0) { paginator2.disconnect() }
        // No assertion needed for "no crash" — test would fail if it threw.
    }
}
