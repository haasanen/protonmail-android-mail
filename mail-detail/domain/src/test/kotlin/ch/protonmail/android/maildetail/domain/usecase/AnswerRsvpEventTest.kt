package ch.protonmail.android.maildetail.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.domain.repository.RsvpEventRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerRsvpEventTest {

    private val repository = mockk<RsvpEventRepository>()

    private val answerRsvpEvent = AnswerRsvpEvent(repository)

    @Test
    fun `use case should call the right repository method`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = MessageIdSample.CalendarInvite
        coEvery { repository.answerRsvpEvent(userId, messageId, RsvpAnswer.Yes) } returns Unit.right()

        // When
        val actual = answerRsvpEvent(userId, messageId, RsvpAnswer.Yes)

        // Then
        coVerify { repository.answerRsvpEvent(userId, messageId, RsvpAnswer.Yes) }
        assertEquals(Unit.right(), actual)
    }
}
