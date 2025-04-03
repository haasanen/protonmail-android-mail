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

import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.composer.data.wrapper.AttachmentListWrapper
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
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
    private lateinit var dataSource: RustAttachmentDataSourceImpl

    @Before
    fun setUp() {
        dataSource = RustAttachmentDataSourceImpl(rustDraftDataSource)
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

}
