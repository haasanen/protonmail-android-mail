package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class DeleteAttachmentTest {

    private val attachmentId = AttachmentId("attachmentId")

    private val attachmentRepository: AttachmentRepository = mockk()

    private val deleteAttachment = DeleteAttachment(attachmentRepository)

    @Test
    fun `deleteAttachment should call attachment repository`() = runTest {
        // Given
        expectComposerDeleteAttachmentSucceeds()

        // When
        deleteAttachment(attachmentId)

        // Then
        coVerifyOrder {
            attachmentRepository.deleteAttachment(attachmentId)
        }
    }

    @Test
    fun `deleteAttachment should return failed to delete file error when file deletion failed`() = runTest {
        // Given
        val expected = AttachmentDeleteError.FailedToDeleteFile.left()
        expectComposerAttachmentDeleteFails(DataError.Local.FailedToDeleteFile.left())

        // When
        val actual = deleteAttachment(attachmentId)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `deleteAttachment should return unknown error when unknown error is returned from the repo failed`() = runTest {
        // Given
        val expected = AttachmentDeleteError.Unknown.left()
        expectComposerAttachmentDeleteFails(DataError.Local.Unknown.left())

        // When
        val actual = deleteAttachment(attachmentId)

        // Then
        assertEquals(expected, actual)
    }

    private fun expectComposerDeleteAttachmentSucceeds() {
        coEvery { attachmentRepository.deleteAttachment(attachmentId) } returns Unit.right()
    }

    private fun expectComposerAttachmentDeleteFails(error: Either<DataError.Local, Nothing>) {
        coEvery { attachmentRepository.deleteAttachment(attachmentId) } returns error
    }
}
