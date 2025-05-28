package ch.protonmail.android.mailmailbox.presentation.mailbox.reducer

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.ui.delete.DeleteDialogState
import ch.protonmail.android.mailmailbox.domain.model.SpamOrTrash
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxEvent
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxViewAction
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class MailboxClearAllDialogReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val mailboxClearAllDialogReducer = MailboxClearAllDialogReducer()

    @Test
    fun `should produce the expected new state`() = with(testInput) {
        val actualState = mailboxClearAllDialogReducer.newStateFrom(operation)

        assertEquals(expectedState, actualState, testName)
    }

    companion object {

        private val transitions = listOf(
            TestInput(
                operation = MailboxEvent.ClearAll(spamOrTrash = SpamOrTrash.Spam),
                expectedState = DeleteDialogState.Shown(
                    title = TextUiModel.TextRes(R.string.mailbox_action_clear_spam_dialog_title),
                    message = TextUiModel.TextRes(R.string.mailbox_action_clear_spam_dialog_body_message)
                )
            ),
            TestInput(
                operation = MailboxEvent.ClearAll(spamOrTrash = SpamOrTrash.Trash),
                expectedState = DeleteDialogState.Shown(
                    title = TextUiModel.TextRes(R.string.mailbox_action_clear_trash_dialog_title),
                    message = TextUiModel.TextRes(R.string.mailbox_action_clear_trash_dialog_body_message)
                )
            ),
            TestInput(
                operation = MailboxViewAction.ClearAllConfirmed,
                expectedState = DeleteDialogState.Hidden
            ),
            TestInput(
                operation = MailboxViewAction.ClearAllDismissed,
                expectedState = DeleteDialogState.Hidden
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
    }

    data class TestInput(
        val operation: MailboxOperation.AffectingClearAllDialog,
        val expectedState: DeleteDialogState
    )
}
