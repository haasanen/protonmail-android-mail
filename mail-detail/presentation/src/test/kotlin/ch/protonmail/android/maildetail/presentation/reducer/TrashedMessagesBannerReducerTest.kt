package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.TrashedMessagesBannerState
import ch.protonmail.android.maillabel.domain.model.SystemLabelId.Archive
import ch.protonmail.android.maillabel.domain.model.SystemLabelId.Inbox
import ch.protonmail.android.maillabel.domain.model.SystemLabelId.Trash
import kotlinx.collections.immutable.toImmutableList
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class TrashedMessagesBannerReducerTest(
    private val testName: String,
    private val input: TestInput
) {

    private val trashedMessagesBannerReducer = TrashedMessagesBannerReducer()

    @Test
    fun test() {
        val result = trashedMessagesBannerReducer.newStateFrom(input.operation)
        assertEquals(input.expectedState, result, testName)
    }

    data class TestInput(
        val operation: ConversationDetailOperation.AffectingTrashedMessagesBanner,
        val expectedState: TrashedMessagesBannerState
    )

    private companion object {

        private val testInputList = listOf(
            TestInput(
                operation = ConversationDetailEvent.MessagesData(
                    messagesUiModels = emptyList<ConversationDetailMessageUiModel>().toImmutableList(),
                    requestScrollToMessageId = null,
                    filterByLocation = Trash.labelId,
                    shouldHideMessagesBasedOnTrashFilter = true
                ),
                expectedState = TrashedMessagesBannerState.Hidden
            ),
            TestInput(
                operation = ConversationDetailEvent.MessagesData(
                    messagesUiModels = emptyList<ConversationDetailMessageUiModel>().toImmutableList(),
                    requestScrollToMessageId = null,
                    filterByLocation = Archive.labelId,
                    shouldHideMessagesBasedOnTrashFilter = true
                ),
                expectedState = TrashedMessagesBannerState.Hidden
            ),
            TestInput(
                operation = ConversationDetailEvent.MessagesData(
                    messagesUiModels = emptyList<ConversationDetailMessageUiModel>().toImmutableList(),
                    requestScrollToMessageId = null,
                    filterByLocation = Trash.labelId,
                    shouldHideMessagesBasedOnTrashFilter = false
                ),
                expectedState = TrashedMessagesBannerState.Hidden
            ),
            TestInput(
                operation = ConversationDetailEvent.MessagesData(
                    messagesUiModels = emptyList<ConversationDetailMessageUiModel>().toImmutableList(),
                    requestScrollToMessageId = null,
                    filterByLocation = Archive.labelId,
                    shouldHideMessagesBasedOnTrashFilter = false
                ),
                expectedState = TrashedMessagesBannerState.Hidden
            ),
            TestInput(
                operation = ConversationDetailEvent.MessagesData(
                    messagesUiModels = emptyList<ConversationDetailMessageUiModel>().toImmutableList(),
                    requestScrollToMessageId = null,
                    filterByLocation = Inbox.labelId,
                    shouldHideMessagesBasedOnTrashFilter = true
                ),
                expectedState = TrashedMessagesBannerState.Hidden
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = testInputList
            .map { testInput ->
                val testName = """
                    Operation: ${testInput.operation}
                    Next state: ${testInput.expectedState}
                """.trimIndent()
                arrayOf(testName, testInput)
            }
    }
}
