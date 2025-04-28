package ch.protonmail.android.mailcomposer.domain.usecase

import android.net.Uri
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class AddAttachmentTest(private val testInput: TestInput) {

    private val attachmentRepository: AttachmentRepository = mockk()

    private val addAttachment = AddAttachment(attachmentRepository)

    @Test
    fun `should map error correctly`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        coEvery { attachmentRepository.addAttachment(mockUri) } returns testInput.error.left()

        // When
        val actual = addAttachment(mockUri)

        // Then
        assertEquals(testInput.expected.left(), actual)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = arrayOf(
            TestInput(
                error = DataError.Local.AttachmentError.TooManyAttachments,
                expected = AttachmentAddError.TooManyAttachments
            ),
            TestInput(
                error = DataError.Local.AttachmentError.AttachmentTooLarge,
                expected = AttachmentAddError.AttachmentTooLarge
            ),
            TestInput(
                error = DataError.Local.AttachmentError.EncryptionError,
                expected = AttachmentAddError.EncryptionError
            ),
            TestInput(
                error = DataError.Local.AttachmentError.InvalidDraftMessage,
                expected = AttachmentAddError.InvalidDraftMessage
            ),
            TestInput(
                error = DataError.Local.NoDataCached,
                expected = AttachmentAddError.Unknown
            )
        )
    }

    data class TestInput(
        val error: DataError.Local,
        val expected: AttachmentAddError
    )
}
