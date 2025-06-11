package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.repository.PreviousScheduleSendTimeRepository
import ch.protonmail.android.mailmessage.domain.model.PreviousScheduleSendTime
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class CancelScheduleSendMessageTest {

    private val messageRepository = mockk<MessageRepository>()
    private val previousScheduleTimeRepository = mockk<PreviousScheduleSendTimeRepository>(relaxUnitFun = true)

    private val cancelScheduleSendMessage = CancelScheduleSendMessage(
        messageRepository,
        previousScheduleTimeRepository
    )

    @Test
    fun `successful cancel schedule send caches previous send time in memory`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val previousScheduleTime = PreviousScheduleSendTime(Instant.DISTANT_FUTURE)
        coEvery { messageRepository.cancelScheduleSend(userId, messageId) } returns previousScheduleTime.right()

        // When
        cancelScheduleSendMessage(userId, messageId)

        // Then
        coVerify { previousScheduleTimeRepository.save(previousScheduleTime) }
    }
}
