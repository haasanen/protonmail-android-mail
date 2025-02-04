package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.repository.MessageExpirationTimeRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class SaveMessageExpirationTimeTest {

    private val userId = UserIdTestData.userId
    private val messageId = MessageIdSample.NewDraftWithSubjectAndBody
    private val expiresIn = 1.days

    private val messageExpirationTimeRepository = mockk<MessageExpirationTimeRepository>()

    private val saveMessageExpirationTime = SaveMessageExpirationTime(messageExpirationTimeRepository)

    @Test
    fun `should return unit when draft exists and message expiration time stored successfully`() = runTest {
        // Given
        coEvery {
            messageExpirationTimeRepository.saveMessageExpirationTime(
                MessageExpirationTime(
                    userId,
                    messageId,
                    expiresIn
                )
            )
        } returns Unit.right()

        // When
        val actual = saveMessageExpirationTime(userId, messageId, expiresIn)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `should return unit when draft is saved and message expiration time is stored successfully`() = runTest {
        // Given
        coEvery {
            messageExpirationTimeRepository.saveMessageExpirationTime(
                MessageExpirationTime(
                    userId,
                    messageId,
                    expiresIn
                )
            )
        } returns Unit.right()

        // When
        val actual = saveMessageExpirationTime(userId, messageId, expiresIn)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `should return error when saving of message expiration time fails`() = runTest {
        // Given
        coEvery {
            messageExpirationTimeRepository.saveMessageExpirationTime(
                MessageExpirationTime(
                    userId,
                    messageId,
                    expiresIn
                )
            )
        } returns DataError.Local.Unknown.left()

        // When
        val actual = saveMessageExpirationTime(userId, messageId, expiresIn)

        // Then
        assertEquals(DataError.Local.Unknown.left(), actual)
    }
}
