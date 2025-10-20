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

package ch.protonmail.android.mailattachments.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailattachments.presentation.ExternalAttachmentErrorResult
import ch.protonmail.android.mailattachments.presentation.ExternalAttachmentsHandler
import ch.protonmail.android.mailattachments.presentation.model.FileContent
import ch.protonmail.android.mailattachments.presentation.model.FileSaveState
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FileSaverViewModelTest {

    private lateinit var viewModel: FileSaverViewModel
    private val attachmentsHandler = mockk<ExternalAttachmentsHandler>()

    @BeforeTest
    fun setup() {
        viewModel = FileSaverViewModel(attachmentsHandler)
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

        coEvery { attachmentsHandler.copyUriToDestination(sourceUri, destinationUri) } returns Unit.right()

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, sourceUri)

            assertEquals(FileSaveState.Saving, awaitItem())
            assertEquals(FileSaveState.Saved.UserPicked, awaitItem())
        }

        coVerify(exactly = 1) { attachmentsHandler.copyUriToDestination(sourceUri, destinationUri) }
        confirmVerified(attachmentsHandler)
    }

    @Test
    fun `performSave should emit Error state when unable to create uri`() = runTest {
        // Given
        val sourceUri = mockk<Uri>()
        val destinationUri = mockk<Uri>()

        coEvery {
            attachmentsHandler.copyUriToDestination(sourceUri, destinationUri)
        } returns ExternalAttachmentErrorResult.UnableToCreateUri.left()

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, sourceUri)
            assertEquals(FileSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertEquals(errorState, FileSaveState.Error(ExternalAttachmentErrorResult.UnableToCreateUri))
        }
    }

    @Test
    fun `performSave should emit Error state when an error occurs during copy`() = runTest {
        // Given
        val sourceUri = mockk<Uri>()
        val destinationUri = mockk<Uri>()

        coEvery {
            attachmentsHandler.copyUriToDestination(sourceUri, destinationUri)
        } returns ExternalAttachmentErrorResult.UnableToCopy.left()

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, sourceUri)

            assertEquals(FileSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is FileSaveState.Error)
            assertEquals(errorState, FileSaveState.Error(ExternalAttachmentErrorResult.UnableToCopy))
        }
    }

    @Test
    fun `performSaveToDownloadFolder should successfully save file and emit Saved state`() = runTest {
        // Given
        val sourceUri = mockk<Uri>()
        val fileContent = FileContent("fileName.pdf", sourceUri, "image/png")

        coEvery { attachmentsHandler.saveFileToDownloadsFolder(fileContent) } returns Unit.right()

        // When + Then
        viewModel.saveState.test {
            assertEquals(FileSaveState.Idle, awaitItem())

            viewModel.performSaveToDownloadFolder(fileContent)

            assertEquals(FileSaveState.Saving, awaitItem())
            assertEquals(FileSaveState.Saved.FallbackLocation, awaitItem())
        }

        coVerify(exactly = 1) { attachmentsHandler.saveFileToDownloadsFolder(fileContent) }
        confirmVerified(attachmentsHandler)
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
