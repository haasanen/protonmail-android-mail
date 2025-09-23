package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageActionRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.label.rust.LabelAsActionsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetMessageLabelAsActionsTest {

    private val actionRepository = mockk<MessageActionRepository>()

    private val getMessageLabelAsActions = GetMessageLabelAsActions(actionRepository)

    @Test
    fun `returns available label as actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageIds = listOf(MessageIdSample.AlphaAppInfoRequest)
        val expected = LabelAsActionsTestData.onlySelectedActions
        coEvery {
            actionRepository.getAvailableLabelAsActions(userId, labelId, messageIds)
        } returns expected.right()

        // When
        val actual = getMessageLabelAsActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageIds = listOf(MessageIdSample.AlphaAppQAReport)
        val expected = DataError.Local.CryptoError.left()
        coEvery { actionRepository.getAvailableLabelAsActions(userId, labelId, messageIds) } returns expected

        // When
        val actual = getMessageLabelAsActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected, actual)
    }
}
