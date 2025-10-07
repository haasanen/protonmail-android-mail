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

package ch.protonmail.android.mailattachments.presentation

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import ch.protonmail.android.mailattachments.presentation.model.FileContent
import ch.protonmail.android.mailattachments.presentation.model.FileSaveState
import ch.protonmail.android.mailattachments.presentation.viewmodel.FileSaverViewModel
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FileSaverViewModelTest {

    private val context = mockk<Context>()
    private lateinit var contentResolver: ContentResolver

    private lateinit var viewModel: FileSaverViewModel

    @BeforeTest
    fun setup() {
        viewModel = FileSaverViewModel(context)
        contentResolver = mockk(relaxed = true)
        every { context.contentResolver } returns contentResolver
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())
        }
    }

    @Test
    fun `requestSave should emit RequestingSave state with content`() = runTest {
        // Given
        val fileContent = FileContent(name = "test.txt", mimeType = "text/plain", uri = mockk<Uri>())

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.requestSave(fileContent)

            val state = awaitItem()
            assertEquals(fileContent, (state as FileSaveState.RequestingSave).content)
        }
    }

    @Test
    fun `markLaunchAsConsumed should transition RequestingSave to WaitingForUser`() = runTest {
        // Given
        val fileContent = FileContent(name = "test.txt", mimeType = "text/plain", uri = mockk<Uri>())

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.requestSave(fileContent)
            val requestingState = awaitItem()
            assertTrue(requestingState is FileSaveState.RequestingSave)

            viewModel.markLaunchAsConsumed()

            val waitingState = awaitItem()
            assertEquals(fileContent, (waitingState as FileSaveState.WaitingForUser).content)
        }
    }

    @Test
    fun `markLaunchAsConsumed should not change state if not RequestingSave`() = runTest {
        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.markLaunchAsConsumed()

            // State should remain Idle, no new emission
            expectNoEvents()
        }
    }

    @Test
    fun `performSave should successfully save file and emit Saved state`() = runTest {
        // Given
        val sourceUri = mockk<Uri>()
        val destinationUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream("test content".toByteArray())
        val outputStream = ByteArrayOutputStream()

        every { contentResolver.openInputStream(sourceUri) } returns inputStream
        every { contentResolver.openOutputStream(destinationUri) } returns outputStream

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, sourceUri)

            assertEquals(FileSaveState.Saving, awaitItem())
            assertEquals(FileSaveState.Saved, awaitItem())
        }

        verify(exactly = 1) { contentResolver.openInputStream(sourceUri) }
        verify(exactly = 1) { contentResolver.openOutputStream(destinationUri) }
        confirmVerified(contentResolver)
    }

    @Test
    fun `performSave should emit Error state when input stream is null`() = runTest {
        // Given
        val sourceUri = mockk<Uri>()
        val destinationUri = mockk<Uri>()

        every { contentResolver.openInputStream(sourceUri) } returns null

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, sourceUri)

            assertEquals(FileSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is FileSaveState.Error)
            assertTrue((errorState as FileSaveState.Error).exception is IOException)
            assertEquals("Failed to open streams for file copy", errorState.exception.message)
        }
    }

    @Test
    fun `performSave should emit Error state when output stream is null`() = runTest {
        // Given
        val sourceUri = mockk<Uri>()
        val destinationUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream("test content".toByteArray())

        every { contentResolver.openInputStream(sourceUri) } returns inputStream
        every { contentResolver.openOutputStream(destinationUri) } returns null

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, sourceUri)

            assertEquals(FileSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is FileSaveState.Error)
            assertTrue((errorState as FileSaveState.Error).exception is IOException)
        }
    }

    @Test
    fun `performSave should emit Error state when IOException occurs during copy`() = runTest {
        // Given
        val sourceUri = mockk<Uri>()
        val destinationUri = mockk<Uri>()
        val exception = IOException("Copy failed")

        // Create a real InputStream that throws an exception when read
        val inputStream = object : InputStream() {
            override fun read(): Int {
                throw exception
            }
        }
        val outputStream = ByteArrayOutputStream()

        every { contentResolver.openInputStream(sourceUri) } returns inputStream
        every { contentResolver.openOutputStream(destinationUri) } returns outputStream

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, sourceUri)

            assertEquals(FileSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is FileSaveState.Error)
            assertTrue((errorState as FileSaveState.Error).exception is IOException)
        }
    }

    @Test
    fun `resetState should return state to Idle`() = runTest {
        // Given
        val fileContent = FileContent(name = "test.txt", mimeType = "text/plain", uri = mockk<Uri>())

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.requestSave(fileContent)
            assertTrue(awaitItem() is FileSaveState.RequestingSave)

            viewModel.resetState()
            assertEquals(FileSaveState.Idle, awaitItem())
        }
    }
}
