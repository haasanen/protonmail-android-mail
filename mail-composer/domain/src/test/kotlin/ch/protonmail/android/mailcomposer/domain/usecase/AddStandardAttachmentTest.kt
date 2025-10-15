package ch.protonmail.android.mailcomposer.domain.usecase

import android.net.Uri
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailattachments.domain.model.AddAttachmentError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class AddStandardAttachmentTest {

    private val attachmentRepository: AttachmentRepository = mockk()

    private val addStandardAttachment = AddStandardAttachment(attachmentRepository)

    @Test
    fun `should forward success from repository`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        coEvery { attachmentRepository.addAttachment(mockUri) } returns Unit.right()

        // When
        val actual = addStandardAttachment(mockUri)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `should forward error from repository`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val error = AddAttachmentError.EncryptionError
        coEvery { attachmentRepository.addAttachment(mockUri) } returns error.left()

        // When
        val actual = addStandardAttachment(mockUri)

        // Then
        assertEquals(error.left(), actual)
    }

}
