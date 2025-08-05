package ch.protonmail.android.maildetail.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import ch.protonmail.android.mailmessage.domain.repository.RsvpEventRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRsvpEventTest {

    private val repository = mockk<RsvpEventRepository>()

    private val getRsvpEvent = GetRsvpEvent(repository)

    @Test
    fun `use case should call the right repository method`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = MessageIdSample.CalendarInvite
        val event = mockk<RsvpEvent>()
        coEvery { repository.getRsvpEvent(userId, messageId) } returns event.right()

        // When
        val actual = getRsvpEvent(userId, messageId)

        // Then
        coVerify { repository.getRsvpEvent(userId, messageId) }
        assertEquals(event.right(), actual)
    }
}
