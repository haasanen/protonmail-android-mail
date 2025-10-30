/*
 * Copyright (c) 2025 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.maildetail.presentation.model.BlockSenderDialogState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.testdata.contact.ContactIdSample
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ConversationBlockSenderDialogReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val blockSenderDialogReducer = ConversationBlockSenderDialogReducer()

    @Test
    fun `should produce the expected new state`() = with(testInput) {
        val actualState = blockSenderDialogReducer.newStateFrom(operation)

        assertEquals(testName, expectedState, actualState)
    }

    companion object {

        private val transitions = listOf(
            TestInput(
                operation = ConversationDetailViewAction.BlockSenderDismissed,
                expectedState = BlockSenderDialogState.Hidden
            ),
            TestInput(
                operation = ConversationDetailViewAction.BlockSenderConfirmed(
                    MessageIdUiModel("msg-123"), "test@example.com"
                ),
                expectedState = BlockSenderDialogState.Hidden
            ),
            TestInput(
                operation = ConversationDetailViewAction.BlockSender(
                    messageId = MessageIdUiModel("HtmlInvoice"),
                    email = "test@example.com",
                    contactId = ContactIdSample.Doe
                ),
                expectedState = BlockSenderDialogState.Shown.ShowConfirmation(
                    messageId = MessageIdUiModel("HtmlInvoice"),
                    email = "test@example.com",
                    contactId = ContactIdSample.Doe
                )
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return transitions.map {
                val testName = """
                    Operation: ${it.operation}
                    Expected State: ${it.expectedState}
                """.trimIndent()
                arrayOf(testName, it)
            }
        }

        data class TestInput(
            val operation: ConversationDetailOperation.AffectingBlockSenderDialog,
            val expectedState: BlockSenderDialogState
        )
    }
}
