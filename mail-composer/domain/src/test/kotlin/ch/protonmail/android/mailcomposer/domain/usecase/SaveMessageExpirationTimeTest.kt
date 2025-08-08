package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationError
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.repository.MessageExpirationTimeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveMessageExpirationTimeTest {

    private val expirationTime = MessageExpirationTime.OneDay

    private val messageExpirationTimeRepository = mockk<MessageExpirationTimeRepository>()

    private val saveMessageExpirationTime = SaveMessageExpirationTime(messageExpirationTimeRepository)

    @Test
    fun `should return unit when draft exists and message expiration time stored successfully`() = runTest {
        // Given
        coEvery {
            messageExpirationTimeRepository.saveMessageExpirationTime(expirationTime)
        } returns Unit.right()

        // When
        val actual = saveMessageExpirationTime(expirationTime)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `should return unit when draft is saved and message expiration time is stored successfully`() = runTest {
        // Given
        coEvery {
            messageExpirationTimeRepository.saveMessageExpirationTime(expirationTime)
        } returns Unit.right()

        // When
        val actual = saveMessageExpirationTime(expirationTime)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `should return error when saving of message expiration time fails`() = runTest {
        // Given
        coEvery {
            messageExpirationTimeRepository.saveMessageExpirationTime(expirationTime)
        } returns MessageExpirationError.ExpirationTimeInThePast.left()

        // When
        val actual = saveMessageExpirationTime(expirationTime)

        // Then
        assertEquals(MessageExpirationError.ExpirationTimeInThePast.left(), actual)
    }
}
