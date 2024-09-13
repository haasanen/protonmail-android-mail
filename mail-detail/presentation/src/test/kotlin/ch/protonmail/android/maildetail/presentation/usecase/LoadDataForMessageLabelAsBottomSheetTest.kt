package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveCustomMailLabels
import ch.protonmail.android.maillabel.presentation.MailLabelUiModel
import ch.protonmail.android.maillabel.presentation.toUiModel
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadDataForMessageLabelAsBottomSheetTest {

    private val userId = UserIdTestData.userId
    private val messageId = MessageIdSample.PlainTextMessage

    private val observeCustomMailLabels = mockk<ObserveCustomMailLabels> {
        coEvery { this@mockk.invoke(userId) } returns flowOf(
            listOf(
                MailLabelTestData.customLabelOne,
                MailLabelTestData.customLabelTwo
            ).right()
        )
    }
    private val loadDataForMessageLabelAsBottomSheet = LoadDataForMessageLabelAsBottomSheet(
        observeCustomMailLabels
    )

    @Test
    fun `should return bottom sheet action data when all operations succeeded`() = runTest {
        // When
        val actual = loadDataForMessageLabelAsBottomSheet(userId, messageId)

        // Then
        val expected = LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
            customLabelList = listOf(
                MailLabelTestData.customLabelOne, MailLabelTestData.customLabelTwo
            ).map {
                it.toUiModel(
                    emptyMap(), MailLabelTestData.inboxSystemLabel.id
                ) as MailLabelUiModel.Custom
            }.toImmutableList(),
            selectedLabels = emptyList<LabelId>().toImmutableList(),
            messageIdInConversation = messageId
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return empty bottom sheet action data when observing labels failed`() = runTest {
        // Given
        coEvery { observeCustomMailLabels(userId) } returns flowOf(DataError.Local.NoDataCached.left())

        // When
        val actual = loadDataForMessageLabelAsBottomSheet(userId, messageId)

        // Then
        val expected = LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
            customLabelList = emptyList<MailLabelUiModel.Custom>().toImmutableList(),
            selectedLabels = emptyList<LabelId>().toImmutableList(),
            messageIdInConversation = messageId
        )
        assertEquals(expected, actual)
    }

}
