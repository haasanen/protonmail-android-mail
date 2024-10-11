/*
 * Copyright (c) 2022 Proton Technologies AG
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

package ch.protonmail.android.mailmessage.presentation.reducer

import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.sample.ActionUiModelSample
import ch.protonmail.android.mailmessage.presentation.mapper.DetailMoreActionsBottomSheetUiMapper
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.BottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import ch.protonmail.android.testdata.action.AvailableActionsTestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.collections.immutable.persistentListOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class DetailMoreActionsBottomSheetReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val mapper = mockk<DetailMoreActionsBottomSheetUiMapper>()
    private val actionsUiModelMapper = ActionUiModelMapper()
    private val reducer = DetailMoreActionsBottomSheetReducer(mapper, actionsUiModelMapper)

    @Before
    fun setup() {
        every {
            mapper.toHeaderUiModel(ExpectedSender, ExpectedSubject, ExpectedMessageId)
        } returns expectedUiModel
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should produce the expected new bottom sheet state`() = with(testInput) {
        val actualState = reducer.newStateFrom(currentState, operation)

        assertEquals(expectedState, actualState, testName)
    }

    companion object {

        private const val ExpectedSender = "Sender"
        private const val ExpectedSubject = "Subject"
        private const val ExpectedMessageId = "messageId"
        private const val SingleParticipantCount = 1
        private const val MultipleParticipantsCount = 10

        private val expectedUiModel = DetailMoreActionsBottomSheetState.DetailDataUiModel(
            headerSubjectText = TextUiModel(ExpectedSubject),
            headerDescriptionText = TextUiModel(ExpectedSender),
            messageIdInConversation = ExpectedMessageId
        )
        private val transitionsFromLoadingState = listOf(
            TestInput(
                currentState = BottomSheetState(DetailMoreActionsBottomSheetState.Loading),
                operation = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
                    messageSender = ExpectedSender,
                    messageSubject = ExpectedSubject,
                    messageIdInConversation = ExpectedMessageId,
                    participantsCount = MultipleParticipantsCount,
                    availableActions = AvailableActionsTestData.replyReportPhishing
                ),
                expectedState = BottomSheetState(
                    contentState = DetailMoreActionsBottomSheetState.Data(
                        detailDataUiModel = expectedUiModel,
                        replyActions = persistentListOf(ActionUiModelSample.Reply),
                        messageActions = persistentListOf(),
                        moveActions = persistentListOf(),
                        genericActions = persistentListOf(ActionUiModelSample.ReportPhishing)
                    )
                )
            ),
            TestInput(
                currentState = BottomSheetState(DetailMoreActionsBottomSheetState.Loading),
                operation = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
                    messageSender = ExpectedSender,
                    messageSubject = ExpectedSubject,
                    messageIdInConversation = ExpectedMessageId,
                    participantsCount = SingleParticipantCount,
                    availableActions = AvailableActionsTestData.forwardReportPhishingActions
                ),
                expectedState = BottomSheetState(
                    contentState = DetailMoreActionsBottomSheetState.Data(
                        detailDataUiModel = expectedUiModel,
                        replyActions = persistentListOf(ActionUiModelSample.Forward),
                        messageActions = persistentListOf(),
                        moveActions = persistentListOf(),
                        genericActions = persistentListOf(ActionUiModelSample.ReportPhishing)
                    )
                )
            ),
            TestInput(
                currentState = BottomSheetState(DetailMoreActionsBottomSheetState.Loading),
                operation = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
                    messageSender = ExpectedSender,
                    messageSubject = ExpectedSubject,
                    messageIdInConversation = ExpectedMessageId,
                    participantsCount = SingleParticipantCount,
                    availableActions = AvailableActionsTestData.fullAvailableActions
                ),
                expectedState = BottomSheetState(
                    contentState = DetailMoreActionsBottomSheetState.Data(
                        detailDataUiModel = expectedUiModel,
                        replyActions = persistentListOf(ActionUiModelSample.Reply),
                        messageActions = persistentListOf(ActionUiModelSample.MarkRead, ActionUiModelSample.Star),
                        moveActions = persistentListOf(ActionUiModelSample.Archive, ActionUiModelSample.Trash),
                        genericActions = persistentListOf(
                            ActionUiModelSample.ReportPhishing, ActionUiModelSample.SavePdf
                        )
                    )
                )
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = transitionsFromLoadingState
            .map { testInput ->
                val testName = """
                    Current state: ${testInput.currentState}
                    Operation: ${testInput.operation}
                    Next state: ${testInput.expectedState}   
                """.trimIndent()
                arrayOf(testName, testInput)
            }
    }

    data class TestInput(
        val currentState: BottomSheetState,
        val operation: DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetOperation,
        val expectedState: BottomSheetState
    )
}
