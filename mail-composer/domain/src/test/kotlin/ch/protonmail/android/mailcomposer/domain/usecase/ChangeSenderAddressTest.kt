package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.ChangeSenderError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ChangeSenderAddressTest {

    private val draftRepository = mockk<DraftRepository>()

    private val changeSenderAddress = ChangeSenderAddress(draftRepository)

    @Test
    fun `when change sender succeeds get and return the updated draft body`() = runTest {
        // Given
        val sender = SenderEmail("sender@pm.me")
        val updatedBody = DraftBody("body with new sender signature")
        coEvery { draftRepository.changeSender(sender) } returns Unit.right()
        coEvery { draftRepository.getBody() } returns updatedBody.right()

        // When
        val actual = changeSenderAddress(sender)

        // Then
        assertEquals(updatedBody.right(), actual)
    }

    @Test
    fun `return error when getting the refresh body fails`() = runTest {
        // Given
        val sender = SenderEmail("sender@pm.me")
        coEvery { draftRepository.changeSender(sender) } returns Unit.right()
        coEvery { draftRepository.getBody() } returns DataError.Local.Unknown.left()

        // When
        val actual = changeSenderAddress(sender)

        // Then
        assertEquals(ChangeSenderError.RefreshBodyError.left(), actual)
    }
}
