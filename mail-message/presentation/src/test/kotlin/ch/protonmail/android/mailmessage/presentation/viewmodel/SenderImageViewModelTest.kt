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

package ch.protonmail.android.mailmessage.presentation.viewmodel

import app.cash.turbine.test
import ch.protonmail.android.mailmessage.domain.model.SenderImage
import ch.protonmail.android.mailmessage.domain.usecase.GetSenderImage
import ch.protonmail.android.mailmessage.presentation.model.SenderImageState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SenderImageViewModelTest {

    private val getSenderImage: GetSenderImage = mockk()
    private val dispatcherProvider = TestDispatcherProvider()
    private lateinit var viewModel: SenderImageViewModel

    @Before
    fun setup() {
        viewModel = SenderImageViewModel(getSenderImage, dispatcherProvider)
    }

    @Test
    fun `should return NotLoaded state for new address`() = runTest {
        // When
        val state = viewModel.stateForAddress("new@example.com").first()

        // Then
        assertEquals(SenderImageState.NotLoaded, state)
    }

    @Test
    fun `should return Loading and Data state when image is available`() = runTest(context = dispatcherProvider.Io) {
        // Given
        val address = "/image/path/example.png"
        val senderImage = mockk<SenderImage> {
            every { imageFile.exists() } returns true
            every { imageFile.length() } returns 1024L
        }
        coEvery { getSenderImage(address, null) } returns senderImage

        viewModel.stateForAddress(address).test {

            // When
            viewModel.loadSenderImage(address, null)

            // Then
            assertEquals(SenderImageState.NotLoaded, awaitItem())
            assertEquals(SenderImageState.Loading, awaitItem())
            assertEquals(SenderImageState.Data(senderImage.imageFile), awaitItem())
        }
    }

    @Test
    fun `should return Loading and NoImageAvailable state when image is null or invalid`() =
        runTest(context = dispatcherProvider.Io) {
            // Given
            val address = "/image/path/test@example.com"
            coEvery { getSenderImage(address, null) } returns null

            viewModel.stateForAddress(address).test {

                // When
                viewModel.loadSenderImage(address, null)

                // Then
                assertEquals(SenderImageState.NotLoaded, awaitItem())
                assertEquals(SenderImageState.Loading, awaitItem())
                assertEquals(SenderImageState.NoImageAvailable, awaitItem())
            }
        }

    @Test
    fun `should not reload sender image if already loaded`() = runTest(context = dispatcherProvider.Io) {
        // Given
        val address = "/image/path/loaded@example.com"
        val senderImage = mockk<SenderImage> {
            every { imageFile.exists() } returns true
            every { imageFile.length() } returns 1024L
        }
        coEvery { getSenderImage(address, null) } returns senderImage
        // First load the image
        viewModel.loadSenderImage(address, null)
        advanceUntilIdle()

        // When
        viewModel.loadSenderImage(address, null)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getSenderImage(address, null) }

    }

}
