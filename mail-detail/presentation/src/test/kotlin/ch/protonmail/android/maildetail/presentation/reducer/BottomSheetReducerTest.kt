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

package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetOperation
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetState
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetVisibilityEffect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.sample.ActionUiModelSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MailboxMoreActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.SnoozeSheetState
import ch.protonmail.android.mailmessage.presentation.reducer.BottomSheetReducer
import ch.protonmail.android.mailmessage.presentation.reducer.ContactActionsBottomSheetReducer
import ch.protonmail.android.mailmessage.presentation.reducer.DetailMoreActionsBottomSheetReducer
import ch.protonmail.android.mailmessage.presentation.reducer.MailboxMoreActionsBottomSheetReducer
import ch.protonmail.android.mailpadlocks.presentation.EncryptionInfoBottomSheetEvent
import ch.protonmail.android.mailpadlocks.presentation.EncryptionInfoSheetState
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.TrackersUiModelSample
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedTrackersBottomSheetEvent
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedTrackersSheetState
import ch.protonmail.android.testdata.action.AvailableActionsTestData
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.domain.entity.UserId
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class BottomSheetReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val mailboxMoreActionsBottomSheetReducer: MailboxMoreActionsBottomSheetReducer = mockk(relaxed = true)
    private val detailMoreActionsBottomSheetReducer: DetailMoreActionsBottomSheetReducer = mockk(relaxed = true)
    private val contactActionsBottomSheetReducer: ContactActionsBottomSheetReducer = mockk(relaxed = true)
    private val reducer = BottomSheetReducer(
        mailboxMoreActionsBottomSheetReducer,
        detailMoreActionsBottomSheetReducer,
        contactActionsBottomSheetReducer
    )

    @Test
    fun `should produce the expected new bottom sheet state`() = with(testInput) {
        val actualState = reducer.newStateFrom(currentState, operation)

        if (reducesBottomSheetVisibilityEffects) {
            assertEquals(expectedState, actualState, testName)
        }

        if (reducesMailboxMoreActions) {
            verify {
                mailboxMoreActionsBottomSheetReducer.newStateFrom(
                    currentState,
                    testInput.operation as MailboxMoreActionsBottomSheetState.MailboxMoreActionsBottomSheetOperation
                )
            }
        } else {
            verify { mailboxMoreActionsBottomSheetReducer wasNot Called }
        }

        if (reducesDetailMoreActions) {
            verify {
                detailMoreActionsBottomSheetReducer.newStateFrom(
                    currentState,
                    testInput.operation
                        as DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetOperation
                )
            }
        } else {
            verify { detailMoreActionsBottomSheetReducer wasNot Called }
        }
    }

    companion object {

        private val bottomSheetVisibilityOperations = listOf(
            TestInput(
                currentState = null,
                operation = BottomSheetOperation.Requested,
                expectedState = BottomSheetState(null, Effect.of(BottomSheetVisibilityEffect.Show)),
                reducesBottomSheetVisibilityEffects = true,
                reducesMailboxMoreActions = false,
                reducesDetailMoreActions = false
            ),
            TestInput(
                currentState = BottomSheetState(
                    MailboxMoreActionsBottomSheetState.Data(
                        listOf<ActionUiModel>().toImmutableList(),
                        listOf<ActionUiModel>().toImmutableList(),
                        ActionUiModelSample.Star,
                        2
                    )
                ),
                operation = BottomSheetOperation.Dismiss,
                expectedState = BottomSheetState(null, Effect.of(BottomSheetVisibilityEffect.Hide)),
                reducesBottomSheetVisibilityEffects = true,
                reducesMailboxMoreActions = false,
                reducesDetailMoreActions = false
            ),
            TestInput(
                currentState = null,
                operation = SnoozeSheetState.SnoozeOptionsBottomSheetEvent.Ready(
                    userId = UserId("testuser"),
                    LabelId("testlabel"),
                    listOf()
                ),
                expectedState = BottomSheetState(
                    SnoozeSheetState.Requested(userId = UserId("testuser"), LabelId("testlabel"), listOf()),
                    Effect.of(BottomSheetVisibilityEffect.Show)
                ),
                reducesBottomSheetVisibilityEffects = true,
                reducesMailboxMoreActions = false,
                reducesDetailMoreActions = false
            ),
            TestInput(
                currentState = null,
                operation = BlockedTrackersBottomSheetEvent.Ready(TrackersUiModelSample.trackersAndLinks),
                expectedState = BottomSheetState(
                    BlockedTrackersSheetState.Requested(TrackersUiModelSample.trackersAndLinks),
                    Effect.of(BottomSheetVisibilityEffect.Show)
                ),
                reducesBottomSheetVisibilityEffects = true,
                reducesMailboxMoreActions = false,
                reducesDetailMoreActions = false
            ),
            TestInput(
                currentState = null,
                operation = EncryptionInfoBottomSheetEvent.Ready(EncryptionInfoUiModel.ProtonE2ee),
                expectedState = BottomSheetState(
                    EncryptionInfoSheetState.Requested(EncryptionInfoUiModel.ProtonE2ee),
                    Effect.of(BottomSheetVisibilityEffect.Show)
                ),
                reducesBottomSheetVisibilityEffects = true,
                reducesMailboxMoreActions = false,
                reducesDetailMoreActions = false
            )
        )

        private val mailboxMoreActionsBottomSheetOperation = listOf(
            TestInput(
                currentState = BottomSheetState(null, Effect.empty()),
                operation = MailboxMoreActionsBottomSheetState.MailboxMoreActionsBottomSheetEvent.ActionData(
                    hiddenActionUiModels = listOf<ActionUiModel>().toImmutableList(),
                    visibleActionUiModels = listOf<ActionUiModel>().toImmutableList(),
                    customizeToolbarActionUiModel = ActionUiModelSample.CustomizeToolbar,
                    selectedCount = 2
                ),
                expectedState = BottomSheetState(
                    MailboxMoreActionsBottomSheetState.Data(
                        listOf<ActionUiModel>().toImmutableList(),
                        listOf<ActionUiModel>().toImmutableList(),
                        ActionUiModelSample.Star,
                        2
                    )
                ),
                reducesBottomSheetVisibilityEffects = false,
                reducesMailboxMoreActions = true,
                reducesDetailMoreActions = false
            )
        )

        private val detailMoreActionsBottomSheetOperation = listOf(
            TestInput(
                currentState = BottomSheetState(null, Effect.empty()),
                operation = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
                    messageSubject = "Subject",
                    messageIdInConversation = "messageId",
                    availableActions = AvailableActionsTestData.replyActionsOnly,
                    customizeToolbarAction = null
                ),
                expectedState = BottomSheetState(
                    DetailMoreActionsBottomSheetState.Data(
                        DetailMoreActionsBottomSheetState.DetailDataUiModel(
                            headerSubjectText = TextUiModel("Subject"),
                            messageIdInConversation = "messageId"
                        ),
                        persistentListOf(ActionUiModelSample.ReplyAll),
                        persistentListOf(),
                        persistentListOf(),
                        persistentListOf(),
                        null
                    )
                ),
                reducesBottomSheetVisibilityEffects = false,
                reducesMailboxMoreActions = false,
                reducesDetailMoreActions = true
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = (
            bottomSheetVisibilityOperations +
                mailboxMoreActionsBottomSheetOperation +
                detailMoreActionsBottomSheetOperation
            )
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
        val currentState: BottomSheetState?,
        val operation: BottomSheetOperation,
        val expectedState: BottomSheetState?,
        val reducesBottomSheetVisibilityEffects: Boolean,
        val reducesMailboxMoreActions: Boolean,
        val reducesDetailMoreActions: Boolean
    )
}
