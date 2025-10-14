package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailconversation.domain.entity.HiddenMessagesBanner
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMetadataUiModel
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.HiddenMessagesBannerState
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class HiddenMessagesBannerReducerTest(
    private val testName: String,
    private val input: TestInput
) {

    private val trashedMessagesBannerReducer = HiddenMessagesBannerReducer()

    @Test
    fun test() {
        val result = trashedMessagesBannerReducer.newStateFrom(input.operation)
        assertEquals(input.expectedState, result, testName)
    }

    data class TestInput(
        val operation: ConversationDetailOperation.AffectingHiddenMessagesBanner,
        val expectedState: HiddenMessagesBannerState
    )

    private companion object {

        private val testInputList = listOf(
            TestInput(
                operation = ConversationDetailEvent.ConversationData(
                    conversationUiModel = ConversationDetailMetadataUiModel(
                        conversationId = ConversationId("id"),
                        subject = "Subject",
                        isStarred = false,
                        messageCount = 3
                    ),
                    hiddenMessagesBanner = HiddenMessagesBanner.ContainsTrashedMessages,
                    showAllMessages = false
                ),
                expectedState = HiddenMessagesBannerState.Shown(
                    message = R.string.trashed_messages_banner,
                    isSwitchTurnedOn = false
                )
            ),
            TestInput(
                operation = ConversationDetailEvent.ConversationData(
                    conversationUiModel = ConversationDetailMetadataUiModel(
                        conversationId = ConversationId("id"),
                        subject = "Subject",
                        isStarred = false,
                        messageCount = 3
                    ),
                    hiddenMessagesBanner = HiddenMessagesBanner.ContainsNonTrashedMessages,
                    showAllMessages = true
                ),
                expectedState = HiddenMessagesBannerState.Shown(
                    message = R.string.non_trashed_messages_banner,
                    isSwitchTurnedOn = true
                )
            ),
            TestInput(
                operation = ConversationDetailEvent.ConversationData(
                    conversationUiModel = ConversationDetailMetadataUiModel(
                        conversationId = ConversationId("id"),
                        subject = "Subject",
                        isStarred = false,
                        messageCount = 3
                    ),
                    hiddenMessagesBanner = null,
                    showAllMessages = false
                ),
                expectedState = HiddenMessagesBannerState.Hidden
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
