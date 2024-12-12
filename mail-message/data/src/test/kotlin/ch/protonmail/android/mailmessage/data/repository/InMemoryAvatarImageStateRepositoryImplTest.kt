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

package ch.protonmail.android.mailmessage.data.repository

import java.io.File
import ch.protonmail.android.mailmessage.domain.model.AvatarImageState
import ch.protonmail.android.mailmessage.domain.model.SenderImage

import ch.protonmail.android.mailmessage.domain.usecase.GetSenderImage
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test

class InMemoryAvatarImageStateRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val getSenderImage: GetSenderImage = mockk()
    private lateinit var repository: InMemoryAvatarImageStateRepositoryImpl

    @Before
    fun setup() {
        repository = InMemoryAvatarImageStateRepositoryImpl(getSenderImage, testCoroutineScope)
    }

    @Test
    fun `should return NotLoaded state for new address`() = runTest {
        // When
        val state = repository.getAvatarImageState("new@example.com")

        // Then
        assertEquals(AvatarImageState.NotLoaded, state)
    }

    @Test
    fun `should load and return Data state when image is valid`() = runTest {
        // Given
        val address = "user1@example.com"
        val mockFile = mockk<File> {
            every { exists() } returns true
            every { length() } returns 1024L
        }
        val senderImage = mockk<SenderImage> {
            every { imageFile } returns mockFile
        }
        coEvery { getSenderImage(address, null) } returns senderImage

        // When
        repository.loadImage(address, null)
        advanceUntilIdle()

        // Then
        val state = repository.getAvatarImageState(address)
        assertEquals(AvatarImageState.Data(mockFile), state)
    }

    @Test
    fun `should load and return NoImageAvailable state when image is invalid`() = runTest {
        // Given
        val address = "user2@example.com"
        coEvery { getSenderImage(address, null) } returns null

        // When
        repository.loadImage(address, null)
        advanceUntilIdle()

        // Then
        val state = repository.getAvatarImageState(address)
        assertEquals(AvatarImageState.NoImageAvailable, state)
    }

    @Test
    fun `should not reload image if already loaded`() = runTest {
        // Given
        val address = "user3@example.com"
        val mockFile = mockk<File> {
            every { exists() } returns true
            every { length() } returns 1024L
        }
        val senderImage = mockk<SenderImage> {
            every { imageFile } returns mockFile
        }
        coEvery { getSenderImage(address, null) } returns senderImage

        // First load
        repository.loadImage(address, null)
        advanceUntilIdle()

        // When: Try loading again
        repository.loadImage(address, null)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getSenderImage(address, null) }
    }

    @Test
    fun `should update allStatesFlow when image is loaded`() = runTest {
        // Given
        val address1 = "user4@example.com"
        val address2 = "user5@example.com"
        val mockFile = mockk<File> {
            every { exists() } returns true
            every { length() } returns 1024L
        }
        val senderImage1 = mockk<SenderImage> {
            every { imageFile } returns mockFile
        }
        val senderImage2 = mockk<SenderImage> {
            every { imageFile } returns mockFile
        }
        coEvery { getSenderImage(address1, null) } returns senderImage1
        coEvery { getSenderImage(address2, null) } returns senderImage2

        // When
        repository.loadImage(address1, null)
        repository.loadImage(address2, null)

        // Then
        val allStates = repository.observeAvatarImageStates().first()
        assertEquals(2, allStates.states.size)
        assertEquals(AvatarImageState.Data(mockFile), allStates.states[address1])
        assertEquals(AvatarImageState.Data(mockFile), allStates.states[address2])
    }

    @Test
    fun `should return the correct state after updating`() = runTest {
        // Given
        val address = "user6@example.com"
        val mockFile = mockk<File> {
            every { exists() } returns true
            every { length() } returns 1024L
        }
        val senderImage = mockk<SenderImage> {
            every { imageFile } returns mockFile
        }
        coEvery { getSenderImage(address, null) } returns senderImage

        // When
        repository.loadImage(address, null)

        // Then
        val state = repository.getAvatarImageState(address)
        assertEquals(AvatarImageState.Data(mockFile), state)
    }
}
