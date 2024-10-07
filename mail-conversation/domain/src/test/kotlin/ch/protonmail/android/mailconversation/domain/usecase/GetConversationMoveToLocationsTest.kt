package ch.protonmail.android.mailconversation.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetConversationMoveToLocationsTest {

    private val conversationRepository = mockk<ConversationRepository>()

    private val getConversationMoveToLocations = GetConversationMoveToLocations(conversationRepository)

    @Test
    fun `returns available actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationIds = listOf(ConversationIdSample.Newsletter)
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery {
            conversationRepository.getSystemMoveToLocations(userId, labelId, conversationIds)
        } returns expected.right()

        // When
        val actual = getConversationMoveToLocations(userId, labelId, conversationIds)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationIds = listOf(ConversationIdSample.Newsletter)
        val expected = DataError.Local.Unknown.left()
        coEvery { conversationRepository.getSystemMoveToLocations(userId, labelId, conversationIds) } returns expected

        // When
        val actual = getConversationMoveToLocations(userId, labelId, conversationIds)

        // Then
        assertEquals(expected, actual)
    }
}
