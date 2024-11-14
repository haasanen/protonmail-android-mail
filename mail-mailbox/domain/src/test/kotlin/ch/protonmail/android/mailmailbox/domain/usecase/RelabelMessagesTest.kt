package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class RelabelMessagesTest {

    private val userId = UserIdSample.Primary

    private val messageRepository: MessageRepository = mockk()
    private val relabel = RelabelMessages(messageRepository)

    @Test
    fun `when repository fails then error is returned`() = runTest {
        // Given
        val expectedMessageIds = listOf(MessageIdSample.Invoice)
        val error = DataError.Local.NoDataCached.left()
        val selectedLabels = listOf(LabelId("labelId"))
        val partiallySelectedLabels = listOf(LabelId("labelId2"))
        coEvery {
            messageRepository.labelAs(
                userId = userId,
                messageIds = expectedMessageIds,
                selectedLabels = selectedLabels,
                partiallySelectedLabels = partiallySelectedLabels,
                shouldArchive = false
            )
        } returns error

        // When
        val result = relabel(
            userId = UserIdSample.Primary,
            messageIds = expectedMessageIds,
            updatedSelections = LabelSelectionList(
                selectedLabels = selectedLabels,
                partiallySelectionLabels = partiallySelectedLabels
            ),
            shouldArchive = false
        )

        // Then
        assertEquals(error, result)
    }

    @Test
    fun `when repository succeeds then Unit is returned`() = runTest {
        // Given
        val expectedMessageIds = listOf(MessageIdSample.Invoice)
        val selectedLabels = listOf(LabelId("labelId"))
        val partiallySelectedLabels = listOf(LabelId("labelId2"))
        coEvery {
            messageRepository.labelAs(
                userId = userId,
                messageIds = expectedMessageIds,
                selectedLabels = selectedLabels,
                partiallySelectedLabels = partiallySelectedLabels,
                shouldArchive = false
            )
        } returns Unit.right()

        // When
        val result = relabel(
            userId = UserIdSample.Primary,
            messageIds = expectedMessageIds,
            updatedSelections = LabelSelectionList(
                selectedLabels = selectedLabels,
                partiallySelectionLabels = partiallySelectedLabels
            ),
            shouldArchive = false
        )

        // Then
        assertEquals(Unit.right(), result)
    }
}
