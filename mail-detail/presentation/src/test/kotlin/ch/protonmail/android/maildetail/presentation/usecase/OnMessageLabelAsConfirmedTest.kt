package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.right
import ch.protonmail.android.maildetail.domain.usecase.RelabelMessage
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import ch.protonmail.android.maillabel.presentation.sample.LabelUiModelWithSelectedStateSample
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class OnMessageLabelAsConfirmedTest {

    private val userId = UserIdTestData.userId
    private val messageId = MessageIdSample.PlainTextMessage

    private val relabelMessage = mockk<RelabelMessage> {
        coEvery {
            this@mockk.invoke(
                userId = userId,
                messageId = messageId,
                updatedSelection = any(),
                shouldArchive = any()
            )
        } returns Unit.right()
    }

    private val onMessageLabelAsConfirmed = OnMessageLabelAsConfirmed(
        relabelMessage
    )

    @Test
    fun `should call relabel message when label as was confirmed`() = runTest {
        // Given
        val labelUiModelsWithSelectedState = LabelUiModelWithSelectedStateSample.customLabelListWithSelection
        val updatedSelection = labelUiModelsWithSelectedState.toLabelSelectionList()

        // When
        onMessageLabelAsConfirmed(userId, messageId, labelUiModelsWithSelectedState, false)

        // Then
        coVerify {
            relabelMessage(
                userId,
                messageId,
                updatedSelection,
                shouldArchive = false
            )
        }
    }

    @Test
    fun `should call move message when label as was confirmed and archive was selected`() = runTest {
        // Given
        val labelUiModelsWithSelectedState = LabelUiModelWithSelectedStateSample.customLabelListWithSelection
        val updatedSelection = labelUiModelsWithSelectedState.toLabelSelectionList()

        // When
        onMessageLabelAsConfirmed(userId, messageId, labelUiModelsWithSelectedState, true)

        // Then
        coVerify {
            relabelMessage(
                userId,
                messageId,
                updatedSelection,
                shouldArchive = true
            )
        }
    }

    private fun List<LabelUiModelWithSelectedState>.toLabelSelectionList(): LabelSelectionList {
        val selected = this.filter {
            it.selectedState == LabelSelectedState.Selected
        }.map { it.labelUiModel.id.labelId }
        val partially = this.filter {
            it.selectedState == LabelSelectedState.PartiallySelected
        }.map { it.labelUiModel.id.labelId }

        return LabelSelectionList(selected, partially)
    }
}
