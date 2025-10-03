package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcomposer.domain.mod.RecipientsNotSupportingExpiration
import ch.protonmail.android.mailcomposer.domain.model.SendWithExpirationTimeResult.CanSend
import ch.protonmail.android.mailcomposer.domain.model.SendWithExpirationTimeResult.ExpirationSupportUnknown
import ch.protonmail.android.mailcomposer.domain.model.SendWithExpirationTimeResult.ExpirationUnsupportedForSome
import ch.protonmail.android.mailcomposer.domain.repository.MessageExpirationTimeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CanSendWithExpirationTimeTest {

    private val repository = mockk<MessageExpirationTimeRepository>()

    private val canSendWithExpirationTime = CanSendWithExpirationTime(repository)

    @Test
    fun `returns can send when all recipients support expiration time`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns AllRecipientsSupportExpiration.right()

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(CanSend.right(), actual)
    }

    @Test
    fun `returns expiration won't apply when any recipients are unsupported`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns UnsupportedRecipients.right()
        val expected = ExpirationUnsupportedForSome(UnsupportedRecipients.unsupported)

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns expiration may not apply when any recipients are unknown`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns UnknownRecipients.right()
        val expected = ExpirationSupportUnknown(UnknownRecipients.unknown)

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns expiration won't apply when there are both unsupported and unknown recipients`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns UnsupportedAndUnknownRecipients.right()
        val expected = ExpirationUnsupportedForSome(UnsupportedRecipients.unsupported)

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(expected.right(), actual)
    }

    companion object {

        private val unsupportedRecipients = listOf("unsupported")
        private val unknownRecipients = listOf("unknown")
        val AllRecipientsSupportExpiration = RecipientsNotSupportingExpiration(emptyList(), emptyList())
        val UnsupportedRecipients = RecipientsNotSupportingExpiration(unsupportedRecipients, emptyList())
        val UnknownRecipients = RecipientsNotSupportingExpiration(emptyList(), unknownRecipients)
        val UnsupportedAndUnknownRecipients =
            RecipientsNotSupportingExpiration(unsupportedRecipients, unknownRecipients)
    }
}
