package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentDeleteError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
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
        coVerifyOrder { attachmentRepository.deleteAttachment(attachmentId) }
    }

    @Test
    fun `deleteAttachment should map failed to delete file error`() = runTest {
        // Given
        val expected = AttachmentDeleteError.FailedToDeleteFile.left()
        expectComposerAttachmentDeleteFails(DataError.Local.FailedToDeleteFile.left())

        // When
        val actual = deleteAttachment(attachmentId)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `deleteAttachment should map invalid message error`() = runTest {
        // Given
        val expected = AttachmentDeleteError.InvalidDraftMessage.left()
        expectComposerAttachmentDeleteFails(DataError.Local.AttachmentError.InvalidDraftMessage.left())

        // When
        val actual = deleteAttachment(attachmentId)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `deleteAttachment should return unknown error when not relevant error is returned from the repo`() = runTest {
        // Given
        val expected = AttachmentDeleteError.Unknown.left()
        expectComposerAttachmentDeleteFails(DataError.Local.AttachmentError.TooManyAttachments.left())

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
