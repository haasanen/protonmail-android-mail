package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.maildetail.presentation.model.MarkAsLegitimateDialogState
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class MarkAsLegitimateDialogReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val markAsLegitimateDialogReducer = MarkAsLegitimateDialogReducer()

    @Test
    fun `should produce the expected new state`() = with(testInput) {
        val actualState = markAsLegitimateDialogReducer.newStateFrom(operation)

        assertEquals(expectedState, actualState, testName)
    }

    companion object {

        private val transitions = listOf(
            TestInput(
                operation = ConversationDetailViewAction.MarkMessageAsLegitimateDismissed,
                expectedState = MarkAsLegitimateDialogState.Hidden
            ),
            TestInput(
                operation = ConversationDetailViewAction.MarkMessageAsLegitimateConfirmed(MessageIdSample.Invoice),
                expectedState = MarkAsLegitimateDialogState.Hidden
            ),
            TestInput(
                operation = ConversationDetailViewAction.MarkMessageAsLegitimate(
                    messageId = MessageIdSample.HtmlInvoice,
                    isPhishing = true
                ),
                expectedState = MarkAsLegitimateDialogState.Shown(
                    messageId = MessageIdSample.HtmlInvoice,
                    isPhishing = true
                )
            ),
            TestInput(
                operation = ConversationDetailViewAction.MarkMessageAsLegitimate(
                    messageId = MessageIdSample.HtmlInvoice,
                    isPhishing = false
                ),
                expectedState = MarkAsLegitimateDialogState.Shown(
                    messageId = MessageIdSample.HtmlInvoice,
                    isPhishing = false
                )
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return transitions
                .map {
                    val testName = """
                        Operation: ${it.operation}
                        Next State: ${it.expectedState}
                    """.trimIndent()
                    arrayOf(testName, it)
                }
        }

        data class TestInput(
            val operation: ConversationDetailOperation.AffectingMarkAsLegitimateDialog,
            val expectedState: MarkAsLegitimateDialogState
        )
    }
}
