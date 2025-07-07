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

package ch.protonmail.android.mailcomposer.presentation.mapper

import ch.protonmail.android.mailattachments.domain.model.AddAttachmentError
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailattachments.domain.model.AttachmentState
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import kotlin.test.Test
import kotlin.test.assertEquals

class AttachmentListErrorMapperTest {

    private fun createAttachmentWithError(reason: AddAttachmentError): AttachmentMetadataWithState {
        val item = mockk<AttachmentMetadataWithState>()
        every { item.attachmentState } returns AttachmentState.Error(reason)
        return item
    }

    @Test
    fun `returns null when there are no errors`() {
        // Given
        val attachment = mockk<AttachmentMetadataWithState>()
        every { attachment.attachmentState } returns AttachmentState.Uploaded

        // When
        val result = AttachmentListErrorMapper.toAttachmentAddErrorWithList(listOf(attachment))

        // Then
        assertNull(result)
    }

    @Test
    fun `returns TooManyAttachments error when present`() {
        // Given
        val attachment = createAttachmentWithError(AddAttachmentError.TooManyAttachments)

        // When
        val result = AttachmentListErrorMapper.toAttachmentAddErrorWithList(listOf(attachment))

        // Then
        assertEquals(AttachmentAddError.TooManyAttachments, result?.error)
        assertEquals(listOf(attachment), result?.failedAttachments)
    }

    @Test
    fun `returns AttachmentTooLarge when no higher-priority error exists`() {
        // Given
        val attachment = createAttachmentWithError(AddAttachmentError.AttachmentTooLarge)

        // When
        val result = AttachmentListErrorMapper.toAttachmentAddErrorWithList(listOf(attachment))

        // Then
        assertEquals(AttachmentAddError.AttachmentTooLarge, result?.error)
        assertEquals(listOf(attachment), result?.failedAttachments)
    }

    @Test
    fun `returns InvalidDraftMessage when no higher-priority error exists`() {
        // Given
        val attachment = createAttachmentWithError(AddAttachmentError.InvalidDraftMessage)

        // When
        val result = AttachmentListErrorMapper.toAttachmentAddErrorWithList(listOf(attachment))

        // Then
        assertEquals(AttachmentAddError.InvalidDraftMessage, result?.error)
        assertEquals(listOf(attachment), result?.failedAttachments)
    }

    @Test
    fun `returns EncryptionError when no higher-priority error exists`() {
        // Given
        val attachment = createAttachmentWithError(AddAttachmentError.EncryptionError)

        // When
        val result = AttachmentListErrorMapper.toAttachmentAddErrorWithList(listOf(attachment))

        // Then
        assertEquals(AttachmentAddError.EncryptionError, result?.error)
        assertEquals(listOf(attachment), result?.failedAttachments)
    }

    @Test
    fun `returns highest priority error when multiple errors exist`() {
        // Given
        val tooManyAttachment = createAttachmentWithError(AddAttachmentError.TooManyAttachments)
        val encryptionError = createAttachmentWithError(AddAttachmentError.EncryptionError)

        // When
        val result = AttachmentListErrorMapper.toAttachmentAddErrorWithList(listOf(tooManyAttachment, encryptionError))

        // Then
        assertEquals(AttachmentAddError.TooManyAttachments, result?.error)
        assertEquals(listOf(tooManyAttachment), result?.failedAttachments)
    }
}

