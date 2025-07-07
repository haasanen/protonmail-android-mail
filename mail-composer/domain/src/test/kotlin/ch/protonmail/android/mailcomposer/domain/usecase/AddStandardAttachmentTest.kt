package ch.protonmail.android.mailcomposer.domain.usecase

import android.net.Uri
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailattachments.domain.model.AddAttachmentError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class AddStandardAttachmentTest(private val testInput: TestInput) {

    private val attachmentRepository: AttachmentRepository = mockk()

    private val addStandardAttachment = AddStandardAttachment(attachmentRepository)

    @Test
    fun `should map error correctly`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        coEvery { attachmentRepository.addAttachment(mockUri) } returns testInput.error.left()

        // When
        val actual = addStandardAttachment(mockUri)

        // Then
        assertEquals(testInput.expected.left(), actual)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = arrayOf(
            TestInput(
                error = AddAttachmentError.TooManyAttachments,
                expected = AttachmentAddError.TooManyAttachments
            ),
            TestInput(
                error = AddAttachmentError.AttachmentTooLarge,
                expected = AttachmentAddError.AttachmentTooLarge
            ),
            TestInput(
                error = AddAttachmentError.EncryptionError,
                expected = AttachmentAddError.EncryptionError
            ),
            TestInput(
                error = AddAttachmentError.InvalidDraftMessage,
                expected = AttachmentAddError.InvalidDraftMessage
            ),
            TestInput(
                error = AddAttachmentError.InvalidState,
                expected = AttachmentAddError.RetryUpload
            ),
            TestInput(
                error = AddAttachmentError.Other(DataError.Local.NoDataCached),
                expected = AttachmentAddError.Unknown
            )
        )
    }

    data class TestInput(
        val error: AddAttachmentError,
        val expected: AttachmentAddError
    )
}
