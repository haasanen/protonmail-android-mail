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

import android.net.Uri
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailattachments.data.local.RustAttachmentDataSource
import ch.protonmail.android.mailattachments.data.mapper.DecryptedAttachmentMapper
import ch.protonmail.android.mailattachments.data.mapper.toAttachmentMetadata
import ch.protonmail.android.mailattachments.data.mapper.toLocalAttachmentId
import ch.protonmail.android.mailattachments.data.repository.AttachmentRepositoryImpl
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.DecryptedAttachment
import ch.protonmail.android.mailcommon.data.mapper.LocalDecryptedAttachment
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.data.sample.LocalAttachmentMetadataSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AttachmentRepositoryImplTest {

    private val rustAttachmentDataSource = mockk<RustAttachmentDataSource>()
    private val decryptedAttachmentMapper = mockk<DecryptedAttachmentMapper>()
    private val attachmentRepository = AttachmentRepositoryImpl(
        rustAttachmentDataSource, decryptedAttachmentMapper
    )

    @Test
    fun `test get attachment returns decrypted attachment for valid inputs`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val attachmentId = AttachmentId("121")
        val mockUri = mockk<Uri>()

        val localAttachment = LocalDecryptedAttachment(
            attachmentMetadata = LocalAttachmentMetadataSample.Pdf,
            dataPath = "/cache/sampleFile"
        )
        val expectedDecryptedAttachment = DecryptedAttachment(
            metadata = localAttachment.attachmentMetadata.toAttachmentMetadata(),
            fileUri = mockUri

        )
        every { decryptedAttachmentMapper.toDomainModel(localAttachment) } returns expectedDecryptedAttachment.right()
        coEvery {
            rustAttachmentDataSource.getAttachment(userId, attachmentId.toLocalAttachmentId())
        } returns localAttachment.right()

        // When
        val result = attachmentRepository.getAttachment(userId, attachmentId)

        // Then
        assertEquals(expectedDecryptedAttachment.right(), result)
        coVerify { rustAttachmentDataSource.getAttachment(userId, attachmentId.toLocalAttachmentId()) }
    }

    @Test
    fun `test get attachment returns data error for failed fetch`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val attachmentId = AttachmentId("1221")
        val expectedError = DataError.Local.Unknown

        coEvery {
            rustAttachmentDataSource.getAttachment(userId, attachmentId.toLocalAttachmentId())
        } returns expectedError.left()

        // When
        val result = attachmentRepository.getAttachment(userId, attachmentId)

        // Then
        assertEquals(expectedError.left(), result)
        coVerify { rustAttachmentDataSource.getAttachment(userId, attachmentId.toLocalAttachmentId()) }
    }
}
