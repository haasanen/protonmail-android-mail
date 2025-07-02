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

package ch.protonmail.android.mailmessage.presentation.mapper

import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailmessage.domain.model.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.presentation.model.attachment.isExpandable
import ch.protonmail.android.mailmessage.presentation.sample.AttachmentMetadataUiModelSamples
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AttachmentGroupUiModelMapperTest {

    private val attachmentMetadataUiModelMapper = mockk<AttachmentMetadataUiModelMapper> {
        every {
            toUiModel(any())
        } returns AttachmentMetadataUiModelSamples.Document
    }

    private val mapper = AttachmentGroupUiModelMapper(attachmentMetadataUiModelMapper)

    @Test
    fun `returns NotApplicable when there is one attachment`() {
        // Given
        val attachments = listOf(AttachmentMetadataSamples.Document)

        // When
        val result = mapper.toUiModel(attachments, null)

        // Then
        assertEquals(1, result.attachments.size)
        assertEquals(
            AttachmentListExpandCollapseMode.NotApplicable,
            result.expandCollapseMode
        )
        assertFalse(result.isExpandable())
    }

    @Test
    fun `returns NotApplicable expandCollapseMode when 3 attachments`() {
        // Given
        val attachments = listOf(
            AttachmentMetadataSamples.Document,
            AttachmentMetadataSamples.Image,
            AttachmentMetadataSamples.Audio
        )

        // When
        val result = mapper.toUiModel(attachments, null)

        // Then
        assertEquals(3, result.attachments.size)
        assertEquals(
            AttachmentListExpandCollapseMode.NotApplicable,
            result.expandCollapseMode
        )
        assertFalse(result.isExpandable())

    }

    @Test
    fun `returns Collapsed as default mode when 4 attachments`() {
        // Given
        val attachments = listOf(
            AttachmentMetadataSamples.Document,
            AttachmentMetadataSamples.Image,
            AttachmentMetadataSamples.Audio,
            AttachmentMetadataSamples.Video
        )

        // When
        val result = mapper.toUiModel(attachments, null)

        // Then
        assertEquals(4, result.attachments.size)
        assertEquals(
            AttachmentListExpandCollapseMode.Collapsed,
            result.expandCollapseMode
        )
        assertTrue(result.isExpandable())
    }


    @Test
    fun `returns the given mode when provided`() {
        // Given
        val attachments = listOf(
            AttachmentMetadataSamples.Document,
            AttachmentMetadataSamples.Image,
            AttachmentMetadataSamples.Audio,
            AttachmentMetadataSamples.Video
        )

        // When
        val result = mapper.toUiModel(attachments, AttachmentListExpandCollapseMode.Expanded)

        // Then
        assertEquals(4, result.attachments.size)
        assertEquals(
            AttachmentListExpandCollapseMode.Expanded,
            result.expandCollapseMode
        )
        assertTrue(result.isExpandable())
    }
}
