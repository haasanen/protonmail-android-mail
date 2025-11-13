package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.AttachmentDataError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadMessageBodyImageTest {

    private val url = "imageUrl"

    private val draftRepository = mockk<DraftRepository>()

    private val loadMessageBodyImage = LoadMessageBodyImage(draftRepository)

    @Test
    fun `returns image when loading it is successful`() = runTest {
        // Given
        val expectedByteArray = "I'm a bytearray".toByteArray()
        val expected = MessageBodyImage(expectedByteArray, "image/png").right()
        coEvery { draftRepository.loadImage(url) } returns expected

        // When
        val actual = loadMessageBodyImage(url)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `returns error when loading image fails`() = runTest {
        // Given
        coEvery {
            draftRepository.loadImage(url)
        } returns AttachmentDataError.Other(DataError.Local.NoDataCached).left()

        // When
        val actual = loadMessageBodyImage(url)

        // Then
        assertEquals(AttachmentDataError.Other(DataError.Local.NoDataCached).left(), actual)
    }
}
