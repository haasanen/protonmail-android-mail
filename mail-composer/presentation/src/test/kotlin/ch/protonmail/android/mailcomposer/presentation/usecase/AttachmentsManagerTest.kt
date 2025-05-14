package ch.protonmail.android.mailcomposer.presentation.usecase

import android.content.Context
import android.net.Uri
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.domain.usecase.AddAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.AddInlineAttachment
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class AttachmentsManagerTest {

    private val context = mockk<Context>()
    private val addStandardAttachment = mockk<AddAttachment>()
    private val addInlineAttachment = mockk<AddInlineAttachment>()

    private val attachmentsManager = AttachmentsManager(
        context,
        addStandardAttachment,
        addInlineAttachment
    )

    @Test
    fun `returns rust defined content id when successfully adding inline attachment`() = runTest {
        // Given
        val fileUri = mockk<Uri>()
        val expectedCid = "rust-defined-cid"
        val expected = AttachmentsManager.AddAttachmentResult.InlineAttachmentAdded(expectedCid)
        coEvery { context.contentResolver.getType(fileUri) } returns "image/jpeg"
        coEvery { addInlineAttachment(fileUri) } returns expectedCid.right()

        // When
        val actual = attachmentsManager.addAttachment(fileUri)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns rust standard attachment added result when successfully adding attachment`() = runTest {
        // Given
        val fileUri = mockk<Uri>()
        val expected = AttachmentsManager.AddAttachmentResult.StandardAttachmentAdded
        coEvery { context.contentResolver.getType(fileUri) } returns "application/pdf"
        coEvery { addStandardAttachment(fileUri) } returns Unit.right()

        // When
        val actual = attachmentsManager.addAttachment(fileUri)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when adding attachment fails`() = runTest {
        // Given
        val fileUri = mockk<Uri>()
        val expected = AttachmentAddError.AttachmentTooLarge
        coEvery { context.contentResolver.getType(fileUri) } returns "application/pdf"
        coEvery { addStandardAttachment(fileUri) } returns expected.left()

        // When
        val actual = attachmentsManager.addAttachment(fileUri)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns error when adding inline attachment fails`() = runTest {
        // Given
        val fileUri = mockk<Uri>()
        val expected = AttachmentAddError.TooManyAttachments
        coEvery { context.contentResolver.getType(fileUri) } returns "image/png"
        coEvery { addInlineAttachment(fileUri) } returns expected.left()

        // When
        val actual = attachmentsManager.addAttachment(fileUri)

        // Then
        assertEquals(expected.left(), actual)
    }

}
