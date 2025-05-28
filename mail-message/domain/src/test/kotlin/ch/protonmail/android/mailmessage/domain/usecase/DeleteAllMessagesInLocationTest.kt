package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.right
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteAllMessagesInLocationTest {

    private val messageRepository = mockk<MessageRepository>()

    private val deleteAllMessagesInLocation = DeleteAllMessagesInLocation(messageRepository)

    @Test
    fun `use case calls the correct repository method`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LabelIdSample.Trash
        coEvery { messageRepository.deleteAllMessagesInLocation(userId, labelId) } returns Unit.right()

        // When
        val actual = deleteAllMessagesInLocation(userId, labelId)

        // Then
        coVerify { messageRepository.deleteAllMessagesInLocation(userId, labelId) }
        assertEquals(Unit.right(), actual)
    }
}
