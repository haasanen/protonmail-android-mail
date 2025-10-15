package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcomposer.domain.model.RecipientsExpirationSupport
import ch.protonmail.android.mailcomposer.domain.model.SendWithExpirationTimeResult
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
    fun `returns expiration unsupported for some when some recipients are unsupported and some supported`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns SupportedAndUnsupportedRecipients.right()
        val expected = ExpirationUnsupportedForSome(SupportedAndUnsupportedRecipients.unsupported)

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns expiration support unknown when there are no supported or unsupported and some unknown`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns UnknownRecipients.right()
        val expected = ExpirationSupportUnknown(UnknownRecipients.unknown)

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns expiration unsupported for some there are both unsupported and unknown recipients`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns UnsupportedAndUnknownRecipients.right()
        val expected = ExpirationUnsupportedForSome(SupportedAndUnsupportedRecipients.unsupported)

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns expiration unsupported for all when there are only unsupported`() = runTest {
        // Given
        coEvery { repository.validateSendWithExpirationTime() } returns UnsupportedRecipients.right()
        val expected = SendWithExpirationTimeResult.ExpirationUnsupportedForAll

        // When
        val actual = canSendWithExpirationTime()

        // Then
        assertEquals(expected.right(), actual)
    }

    companion object {

        private val supportedRecipients = listOf("supported")
        private val unsupportedRecipients = listOf("unsupported")
        private val unknownRecipients = listOf("unknown")

        val AllRecipientsSupportExpiration = RecipientsExpirationSupport(
            supportedRecipients,
            emptyList(),
            emptyList()
        )
        val SupportedAndUnsupportedRecipients = RecipientsExpirationSupport(
            supportedRecipients,
            unsupportedRecipients,
            emptyList()
        )
        val UnknownRecipients = RecipientsExpirationSupport(
            emptyList(),
            emptyList(),
            unknownRecipients
        )
        val UnsupportedAndUnknownRecipients = RecipientsExpirationSupport(
            emptyList(),
            unsupportedRecipients,
            unknownRecipients
        )
        val UnsupportedRecipients = RecipientsExpirationSupport(
            emptyList(),
            unsupportedRecipients,
            emptyList()
        )
    }
}
