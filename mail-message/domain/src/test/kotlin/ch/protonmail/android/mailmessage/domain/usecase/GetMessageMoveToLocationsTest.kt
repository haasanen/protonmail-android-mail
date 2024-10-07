package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetMessageMoveToLocationsTest {

    private val messageRepository = mockk<MessageRepository>()

    private val getMessageMoveToLocations = GetMessageMoveToLocations(messageRepository)

    @Test
    fun `returns available move to locations when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageIds = listOf(MessageIdSample.AlphaAppQAReport)
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery { messageRepository.getSystemMoveToLocations(userId, labelId, messageIds) } returns expected.right()

        // When
        val actual = getMessageMoveToLocations(userId, labelId, messageIds)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails to get available move to locations`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageIds = listOf(MessageIdSample.AlphaAppQAReport)
        val expected = DataError.Local.Unknown.left()
        coEvery { messageRepository.getSystemMoveToLocations(userId, labelId, messageIds) } returns expected

        // When
        val actual = getMessageMoveToLocations(userId, labelId, messageIds)

        // Then
        assertEquals(expected, actual)
    }
}
