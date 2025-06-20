package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailattachments.domain.model.AttachmentState
import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ObserveMessageAttachmentsTest {

    private val repository = mockk<AttachmentRepository>()

    private val observeMessageAttachments = ObserveMessageAttachments(repository)

    @Test
    fun `returns attachments from repository filtering out inline ones`() = runTest {
        // Given
        val attachment = AttachmentMetadataWithState(AttachmentMetadataSamples.Invoice, AttachmentState.Uploaded)
        val attachment1 = AttachmentMetadataWithState(AttachmentMetadataSamples.Pdf, AttachmentState.Pending)
        val inline = AttachmentMetadataWithState(
            AttachmentMetadataSamples.EmbeddedImageAttachment, AttachmentState.Uploading
        )
        val attachments = listOf(attachment, inline, attachment1)
        coEvery { repository.observeAttachments() } returns flowOf(attachments.right())

        // When
        val actual = observeMessageAttachments()

        // Then
        val expected = listOf(attachment, attachment1)
        assertEquals(expected.right(), actual.first())
    }
}
