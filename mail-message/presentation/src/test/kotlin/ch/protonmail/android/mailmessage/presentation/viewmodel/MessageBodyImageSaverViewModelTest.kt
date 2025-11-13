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

package ch.protonmail.android.mailmessage.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailattachments.presentation.ExternalAttachmentErrorResult
import ch.protonmail.android.mailattachments.presentation.ExternalAttachmentsHandler
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.model.AttachmentDataError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.usecase.LoadMessageBodyImage
import ch.protonmail.android.mailmessage.presentation.model.BodyImageSaveState
import ch.protonmail.android.mailmessage.presentation.model.BodyImageUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MessageBodyImageSaverViewModelTest {

    private val attachmentsHandler = mockk<ExternalAttachmentsHandler>()
    private val loadMessageBodyImage = mockk<LoadMessageBodyImage>()
    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        coEvery { this@mockk.invoke() } returns flowOf(UserIdSample.Primary)
    }

    private val testImageUrl = "cid:test_image"
    private val testBodyImageUiModel = BodyImageUiModel(
        imageUrl = testImageUrl,
        messageId = MessageIdSample.MessageWithAttachments
    )
    private val testBodyImage = MessageBodyImage(data = byteArrayOf(1, 2, 3), mimeType = "image/png")

    private val viewModel = MessageBodyImageSaverViewModel(
        attachmentsHandler,
        loadMessageBodyImage,
        observePrimaryUserId
    )

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())
        }
    }

    @Test
    fun `requestSave should emit RequestingSave state with content when image loads successfully`() = runTest {
        // Given
        val expected = BodyImageSaveState.RequestingSave(testBodyImageUiModel, testBodyImage)
        coEvery {
            loadMessageBodyImage(
                UserIdSample.Primary,
                MessageIdSample.MessageWithAttachments,
                testImageUrl
            )
        } returns testBodyImage.right()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.requestSave(testBodyImageUiModel)

            assertEquals(expected, awaitItem())
        }

        coVerify(exactly = 1) {
            loadMessageBodyImage(
                UserIdSample.Primary,
                MessageIdSample.MessageWithAttachments, testImageUrl
            )
        }
    }

    @Test
    fun `requestSave should emit Error state when image fails to load`() = runTest {
        // Given
        val expected = BodyImageSaveState.Error(ExternalAttachmentErrorResult.UnableToLoadImage)
        coEvery {
            loadMessageBodyImage(
                UserIdSample.Primary,
                MessageIdSample.MessageWithAttachments,
                testImageUrl
            )
        } returns mockk<AttachmentDataError>().left()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.requestSave(testBodyImageUiModel)

            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `requestSave should emit Error state when user not found`() = runTest {
        // Given
        val expected = BodyImageSaveState.Error(ExternalAttachmentErrorResult.UserNotFound)
        coEvery { observePrimaryUserId() } returns flowOf(null)

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.requestSave(testBodyImageUiModel)

            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `markLaunchAsConsumed should transition RequestingSave to WaitingForUser`() = runTest {
        // Given
        val expected = BodyImageSaveState.WaitingForUser(testBodyImageUiModel, testBodyImage)
        coEvery {
            loadMessageBodyImage(
                UserIdSample.Primary,
                MessageIdSample.MessageWithAttachments,
                testImageUrl
            )
        } returns testBodyImage.right()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.requestSave(testBodyImageUiModel)
            val requestingState = awaitItem()
            assertTrue(requestingState is BodyImageSaveState.RequestingSave)

            viewModel.markLaunchAsConsumed()

            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `markLaunchAsConsumed should not change state if not RequestingSave`() = runTest {
        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.markLaunchAsConsumed()

            // State should remain Idle, no new emission
            expectNoEvents()
        }
    }

    @Test
    fun `performSave should successfully save image and emit Saved state`() = runTest {
        // Given
        val destinationUri = mockk<Uri>()

        coEvery {
            attachmentsHandler.saveDataToDestination(destinationUri, testBodyImage.mimeType, testBodyImage.data)
        } returns Unit.right()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, testBodyImage)

            assertEquals(BodyImageSaveState.Saving, awaitItem())
            assertEquals(BodyImageSaveState.Saved.UserPicked, awaitItem())
        }

        coVerify(exactly = 1) {
            attachmentsHandler.saveDataToDestination(destinationUri, testBodyImage.mimeType, testBodyImage.data)
        }
        confirmVerified(attachmentsHandler)
    }

    @Test
    fun `performSave should emit Error state when unable to create uri`() = runTest {
        // Given
        val destinationUri = mockk<Uri>()

        coEvery {
            attachmentsHandler.saveDataToDestination(destinationUri, testBodyImage.mimeType, testBodyImage.data)
        } returns ExternalAttachmentErrorResult.UnableToCreateUri.left()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, testBodyImage)
            assertEquals(BodyImageSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertEquals(errorState, BodyImageSaveState.Error(ExternalAttachmentErrorResult.UnableToCreateUri))
        }
    }

    @Test
    fun `performSave should emit Error state when an error occurs during save`() = runTest {
        // Given
        val destinationUri = mockk<Uri>()

        coEvery {
            attachmentsHandler.saveDataToDestination(destinationUri, testBodyImage.mimeType, testBodyImage.data)
        } returns ExternalAttachmentErrorResult.UnableToCopy.left()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.performSave(destinationUri, testBodyImage)

            assertEquals(BodyImageSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is BodyImageSaveState.Error)
            assertEquals(errorState, BodyImageSaveState.Error(ExternalAttachmentErrorResult.UnableToCopy))
        }
    }

    @Test
    fun `performSaveToDownloadFolder should successfully save image and emit Saved state`() = runTest {
        // Given
        coEvery {
            attachmentsHandler.saveDataToDownloads(testImageUrl, testBodyImage.mimeType, testBodyImage.data)
        } returns Unit.right()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.performSaveToDownloadFolder(testBodyImageUiModel, testBodyImage)

            assertEquals(BodyImageSaveState.Saving, awaitItem())
            assertEquals(BodyImageSaveState.Saved.FallbackLocation, awaitItem())
        }

        coVerify(exactly = 1) {
            attachmentsHandler.saveDataToDownloads(testImageUrl, testBodyImage.mimeType, testBodyImage.data)
        }
        confirmVerified(attachmentsHandler)
    }

    @Test
    fun `performSaveToDownloadFolder should emit Error state when save fails`() = runTest {
        // Given
        coEvery {
            attachmentsHandler.saveDataToDownloads(testImageUrl, testBodyImage.mimeType, testBodyImage.data)
        } returns ExternalAttachmentErrorResult.UnableToCopy.left()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.performSaveToDownloadFolder(testBodyImageUiModel, testBodyImage)

            assertEquals(BodyImageSaveState.Saving, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is BodyImageSaveState.Error)
            assertEquals(errorState, BodyImageSaveState.Error(ExternalAttachmentErrorResult.UnableToCopy))
        }
    }

    @Test
    fun `resetState should return state to Idle`() = runTest {
        // Given
        coEvery {
            loadMessageBodyImage(
                UserIdSample.Primary,
                MessageIdSample.MessageWithAttachments,
                testImageUrl
            )
        } returns testBodyImage.right()

        // When + Then
        viewModel.saveState.test {
            assertEquals(BodyImageSaveState.Idle, awaitItem())

            viewModel.requestSave(testBodyImageUiModel)
            assertTrue(awaitItem() is BodyImageSaveState.RequestingSave)

            viewModel.resetState()
            assertEquals(BodyImageSaveState.Idle, awaitItem())
        }
    }
}
