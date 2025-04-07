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

package ch.protonmail.android.composer.data.local

import android.net.Uri
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.wrapper.AttachmentListWrapper
import ch.protonmail.android.mailcommon.data.file.FileInformation
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.local.AttachmentFileStorage
import ch.protonmail.android.mailmessage.data.mapper.toAttachmentMetadata
import ch.protonmail.android.mailmessage.data.sample.LocalAttachmentMetadataSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.slot
import org.junit.Before
import uniffi.proton_mail_uniffi.AsyncLiveQueryCallback
import uniffi.proton_mail_uniffi.AttachmentListAddResult
import uniffi.proton_mail_uniffi.AttachmentListAttachmentsResult
import uniffi.proton_mail_uniffi.AttachmentListWatcherResult
import uniffi.proton_mail_uniffi.DraftAttachment
import uniffi.proton_mail_uniffi.DraftAttachmentState
import uniffi.proton_mail_uniffi.DraftAttachmentWatcher
import uniffi.proton_mail_uniffi.OtherErrorReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import uniffi.proton_mail_uniffi.ProtonError as LocalProtonError

class RustAttachmentDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val rustDraftDataSource = mockk<RustDraftDataSource>()
    private val attachmentFileStorage = mockk<AttachmentFileStorage>()
    private lateinit var dataSource: RustAttachmentDataSourceImpl

    @Before
    fun setUp() {
        dataSource = RustAttachmentDataSourceImpl(rustDraftDataSource, attachmentFileStorage)
    }

    @Test
    fun `observe attachments return flow of attachments successfully`() = runTest {
        // Given
        val attachment = LocalAttachmentMetadataSample.Pdf
        val draftAttachment = DraftAttachment(
            state = DraftAttachmentState.Uploaded,
            attachment = attachment,
            stateModifiedTimestamp = 0L
        )

        val expectedMetadata = attachment.toAttachmentMetadata()

        val wrapper = mockk<AttachmentListWrapper>()
        val watcher = mockk<DraftAttachmentWatcher>()
        val callbackSlot = slot<AsyncLiveQueryCallback>()

        coEvery { rustDraftDataSource.attachmentList() } returns wrapper.right()
        coEvery { wrapper.attachments() } returns AttachmentListAttachmentsResult.Ok(listOf(draftAttachment))
        coEvery { wrapper.createWatcher(capture(callbackSlot)) } returns AttachmentListWatcherResult.Ok(watcher)

        // When
        dataSource.observeAttachments().test {
            callbackSlot.captured.onUpdate()

            // Then
            val emission = awaitItem()
            assertTrue(emission.isRight())
            assertEquals(listOf(expectedMetadata), emission.orNull())
            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `observe attachments fail when rust draft attachment access fails`() = runTest {
        // Given
        val protonError = LocalProtonError.OtherReason(
            OtherErrorReason.Other("some internal error")
        )
        val wrapper = mockk<AttachmentListWrapper>()
        val watcher = mockk<DraftAttachmentWatcher>()
        val callbackSlot = slot<AsyncLiveQueryCallback>()

        coEvery { rustDraftDataSource.attachmentList() } returns wrapper.right()
        coEvery {
            wrapper.attachments()
        } returns AttachmentListAttachmentsResult.Error(
            uniffi.proton_mail_uniffi.DraftAttachmentError.Other(protonError)
        )
        coEvery { wrapper.createWatcher(capture(callbackSlot)) } returns AttachmentListWatcherResult.Ok(watcher)

        val expected = protonError.toDataError()

        // When
        dataSource.observeAttachments().test {
            callbackSlot.captured.onUpdate()

            // Then
            val emission = awaitItem()
            assertTrue(emission.isLeft())
            assertEquals(expected, emission.swap().orNull())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add attachment successfully when storage and Rust add succeeds`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val fileInfo = FileInformation(
            name = "test.pdf",
            path = "/fake/path/test.pdf",
            size = 1024L,
            mimeType = "application/pdf"
        )
        val wrapper = mockk<AttachmentListWrapper>()

        coEvery { rustDraftDataSource.attachmentList() } returns wrapper.right()
        coEvery { wrapper.attachmentUploadDirectory() } returns "/fake/path"
        coEvery { attachmentFileStorage.saveAttachment(any(), eq(uri)) } returns fileInfo
        coEvery { wrapper.addAttachment(fileInfo.path) } returns AttachmentListAddResult.Ok

        // When
        val result = dataSource.addAttachment(uri)

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `addAttachment fails when accessing attachmentList fails`() = runTest {
        // Given
        val error = DataError.Local.Unknown
        coEvery { rustDraftDataSource.attachmentList() } returns error.left()

        // When
        val result = dataSource.addAttachment(mockk())

        // Then
        assertTrue(result.isLeft())
        assertEquals(error, result.swap().getOrNull())
    }

    @Test
    fun `addAttachment fails when saving attachment fails`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val wrapper = mockk<AttachmentListWrapper>()

        coEvery { rustDraftDataSource.attachmentList() } returns wrapper.right()
        coEvery { wrapper.attachmentUploadDirectory() } returns "/fake/path"
        coEvery { attachmentFileStorage.saveAttachment(any(), eq(uri)) } returns null

        // When
        val result = dataSource.addAttachment(uri)

        // Then
        assertTrue(result.isLeft())
        assertEquals(DataError.Local.FailedToStoreFile, result.swap().getOrNull())
    }

    @Test
    fun `addAttachment returns left when addAttachment fails`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val fileInfo = FileInformation(
            name = "error.pdf",
            path = "/fake/path/error.pdf",
            size = 2048L,
            mimeType = "application/pdf"
        )
        val wrapper = mockk<AttachmentListWrapper>()
        val rustError = uniffi.proton_mail_uniffi.DraftAttachmentError.Other(
            LocalProtonError.OtherReason(OtherErrorReason.Other("internal"))
        )

        coEvery { rustDraftDataSource.attachmentList() } returns wrapper.right()
        coEvery { wrapper.attachmentUploadDirectory() } returns "/fake/path"
        coEvery { attachmentFileStorage.saveAttachment(any(), eq(uri)) } returns fileInfo
        coEvery { wrapper.addAttachment(fileInfo.path) } returns AttachmentListAddResult.Error(rustError)

        // When
        val result = dataSource.addAttachment(uri)

        // Then
        assertTrue(result.isLeft())
        assertEquals(rustError.toDataError(), result.swap().orNull())
    }
}
