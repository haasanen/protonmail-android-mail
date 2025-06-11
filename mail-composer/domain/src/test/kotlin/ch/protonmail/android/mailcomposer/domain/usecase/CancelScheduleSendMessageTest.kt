package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.model.PreviousScheduleSendTime
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailcomposer.domain.repository.PreviousScheduleSendTimeRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class CancelScheduleSendMessageTest {

    private val draftRepository = mockk<DraftRepository>()
    private val previousScheduleTimeRepository = mockk<PreviousScheduleSendTimeRepository>(relaxUnitFun = true)

    private val cancelScheduleSendMessage = CancelScheduleSendMessage(
        draftRepository,
        previousScheduleTimeRepository
    )

    @Test
    fun `successful cancel schedule send caches previous send time in memory`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val previousScheduleTime = PreviousScheduleSendTime(Instant.DISTANT_FUTURE)
        coEvery { draftRepository.cancelScheduleSend(userId, messageId) } returns previousScheduleTime.right()

        // When
        cancelScheduleSendMessage(userId, messageId)

        // Then
        coVerify { previousScheduleTimeRepository.save(previousScheduleTime) }
    }
}
