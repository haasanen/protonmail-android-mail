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

package ch.protonmail.android.maillabel.presentation.bottomsheet

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailconversation.domain.usecase.LabelConversations
import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.ViewMode
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.model.LabelAsBottomSheetUiModel
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.LabelMessages
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LabelAsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val getLabelAsBottomSheetContent = mockk<GetLabelAsBottomSheetContent>()
    private val labelMessages = mockk<LabelMessages>()
    private val labelConversations = mockk<LabelConversations>()
    private val reducer = spyk<LabelAsReducer>()

    @BeforeTest
    fun setup() {
        every { observePrimaryUserId() } returns flowOf(userId)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should emit an error when no items are provided`() = runTest {
        // Given
        val initialData = defaultInitialData.copy(items = emptyList())

        // When + Then
        viewModel(initialData).state.test {
            assertEquals(LabelAsState.Error, awaitItem())
        }
    }

    @Test
    fun `should emit data error when data can't be fetched`() = runTest {
        // Given
        val viewMode = ViewMode.NoConversationGrouping
        val initialData =
            defaultInitialData.copy(
                entryPoint = LabelAsBottomSheetEntryPoint.Mailbox.SelectionMode(
                    itemCount = 1,
                    viewMode = viewMode
                )
            )

        coEvery {
            getLabelAsBottomSheetContent.forMailbox(userId, labelId, items, viewMode)
        } returns DataError.Local.NoDataCached.left()

        // When + Then
        viewModel(initialData).state.test {
            assertEquals(LabelAsState.Error, awaitItem())
        }
    }

    @Test
    fun `should emit data loaded for conversation`() = runTest {
        // Given
        val initialData = defaultInitialData.copy(entryPoint = LabelAsBottomSheetEntryPoint.Conversation)
        val conversationId = ConversationId("item1")
        expectLoadedDataForConversation(conversationId)

        // When + Then
        viewModel(initialData).state.test {
            assertEquals(defaultDataState, awaitItem())
        }
    }

    @Test
    fun `should emit data loaded for message`() = runTest {
        // Given
        val messageId = MessageId("item1")
        val initialData = defaultInitialData.copy(entryPoint = LabelAsBottomSheetEntryPoint.Message(messageId))
        expectLoadedDataForMessage(messageId, initialData.entryPoint)

        // When + Then
        viewModel(initialData).state.test {
            assertEquals(defaultDataState, awaitItem())
        }
    }

    @Test
    fun `should emit data loaded for selection mode`() = runTest {
        // Given
        val messageId = MessageId("item1")
        val entryPoint =
            LabelAsBottomSheetEntryPoint.Mailbox.SelectionMode(itemCount = 1, ViewMode.NoConversationGrouping)
        val initialData = defaultInitialData.copy(entryPoint = entryPoint)
        val labelAsItemId = LabelAsItemId(messageId.id)
        expectLoadedDataForMailbox(entryPoint, listOf(labelAsItemId))

        // When + Then
        viewModel(initialData).state.test {
            assertEquals(defaultDataState, awaitItem())
        }
    }

    @Test
    fun `should emit data loaded for swipe action`() = runTest {
        // Given
        val messageId = MessageId("item1")
        val labelAsItemId = LabelAsItemId(messageId.id)
        val initialData = defaultInitialData.copy(
            entryPoint = LabelAsBottomSheetEntryPoint.Mailbox.LabelAsSwipeAction(
                ViewMode.ConversationGrouping,
                labelAsItemId
            )
        )

        expectLoadedDataForMailbox(
            initialData.entryPoint as LabelAsBottomSheetEntryPoint.Mailbox,
            listOf(labelAsItemId)
        )

        // When + Then
        viewModel(initialData).state.test {
            assertEquals(defaultDataState, awaitItem())
        }
    }

    @Test
    fun `should emit updated state on label toggling`() = runTest {
        // Given
        val conversationId = ConversationId("item1")
        expectLoadedDataForConversation(conversationId)

        val toggleAction = LabelAsOperation.LabelAsAction.LabelToggled(LabelId("labelId2"))
        val updatedState = mockk<LabelAsState.Data>()
        every { reducer.newStateFrom(defaultDataState, toggleAction) } returns updatedState

        // When + Then
        val viewModel = viewModel(defaultInitialData)
        viewModel.state.test {
            assertEquals(defaultDataState, awaitItem())
            viewModel.submit(toggleAction)
            assertEquals(updatedState, awaitItem())
        }
    }

    @Test
    fun `should trigger label as action and update state on operation confirmed (conversation)`() = runTest {
        // Given
        val conversationId = ConversationId("item1")
        val labelId = MailLabelId.Custom.Label(labelId)
        val uiModel = LabelAsBottomSheetUiModel(id = labelId, text = TextUiModel("Text"), icon = 1, iconTint = null)
        val unselectedUiModel = LabelUiModelWithSelectedState(uiModel, selectedState = LabelSelectedState.NotSelected)

        val initialState = LabelAsState.Data(
            entryPoint = defaultInitialData.entryPoint,
            labelUiModels = listOf(unselectedUiModel).toImmutableList(),
            shouldDismissEffect = Effect.empty(),
            errorEffect = Effect.empty()
        )

        val updatedState = initialState.copy(shouldDismissEffect = Effect.of(Unit))

        expectLoadedDataForConversation(conversationId = conversationId, initialState = initialState)
        coEvery { labelConversations(any(), any(), any(), any()) } returns Unit.right()

        // When
        val confirmAction = LabelAsOperation.LabelAsAction.OperationConfirmed(alsoArchive = true)

        // Then
        verifyConversationMove(defaultInitialData, initialState, confirmAction, updatedState, conversationId)
    }

    @Test
    fun `should trigger label as action and update state on operation confirmed (message)`() = runTest {
        // Given
        val messageId = MessageId("item1")
        val labelId = MailLabelId.Custom.Label(labelId)
        val uiModel = LabelAsBottomSheetUiModel(id = labelId, text = TextUiModel("Text"), icon = 1, iconTint = null)

        val unselectedUiModel = LabelUiModelWithSelectedState(uiModel, selectedState = LabelSelectedState.NotSelected)
        val initialData = defaultInitialData.copy(entryPoint = LabelAsBottomSheetEntryPoint.Message(messageId))

        val initialState = LabelAsState.Data(
            entryPoint = initialData.entryPoint,
            labelUiModels = listOf(unselectedUiModel).toImmutableList(),
            shouldDismissEffect = Effect.empty(),
            errorEffect = Effect.empty()
        )

        val updatedState = initialState.copy(shouldDismissEffect = Effect.of(Unit))
        expectLoadedDataForMessage(messageId, initialState = initialState)
        coEvery { labelMessages(any(), any(), any(), any()) } returns Unit.right()

        // When
        val confirmAction = LabelAsOperation.LabelAsAction.OperationConfirmed(alsoArchive = true)

        // Then
        verifyMessageMove(initialData, initialState, confirmAction, updatedState, messageId)
    }

    @Test
    fun `should trigger label as action and update state on operation confirmed (swipe action, message)`() = runTest {
        // Given
        val messageId = MessageId("item1")
        val labelId = MailLabelId.Custom.Label(labelId)
        val uiModel = LabelAsBottomSheetUiModel(id = labelId, text = TextUiModel("Text"), icon = 1, iconTint = null)
        val unselectedUiModel = LabelUiModelWithSelectedState(uiModel, selectedState = LabelSelectedState.NotSelected)
        val items = listOf(LabelAsItemId(messageId.id))

        val entryPoint =
            LabelAsBottomSheetEntryPoint.Mailbox.LabelAsSwipeAction(ViewMode.NoConversationGrouping, items.first())
        val initialData = defaultInitialData.copy(entryPoint = entryPoint)

        val initialState = LabelAsState.Data(
            entryPoint = initialData.entryPoint,
            labelUiModels = listOf(unselectedUiModel).toImmutableList(),
            shouldDismissEffect = Effect.empty(),
            errorEffect = Effect.empty()
        )

        val updatedState = initialState.copy(shouldDismissEffect = Effect.of(Unit))
        expectLoadedDataForMailbox(entryPoint, items, initialState = initialState)
        coEvery { labelMessages(any(), any(), any(), any()) } returns Unit.right()

        // When
        val action = LabelAsOperation.LabelAsAction.OperationConfirmed(true)

        // Then
        verifyMessageMove(initialData, initialState, action, updatedState, messageId)
    }

    @Test
    fun `should trigger label as action and update state on operation confirmed (swipe action, convo)`() = runTest {
        // Given
        val conversationId = ConversationId("item1")
        val labelId = MailLabelId.Custom.Label(labelId)
        val uiModel = LabelAsBottomSheetUiModel(id = labelId, text = TextUiModel("Text"), icon = 1, iconTint = null)
        val unselectedUiModel = LabelUiModelWithSelectedState(uiModel, selectedState = LabelSelectedState.NotSelected)
        val items = listOf(LabelAsItemId(conversationId.id))

        val entryPoint =
            LabelAsBottomSheetEntryPoint.Mailbox.LabelAsSwipeAction(ViewMode.ConversationGrouping, items.first())
        val initialData = defaultInitialData.copy(entryPoint = entryPoint)

        val initialState = LabelAsState.Data(
            entryPoint = initialData.entryPoint,
            labelUiModels = listOf(unselectedUiModel).toImmutableList(),
            shouldDismissEffect = Effect.empty(),
            errorEffect = Effect.empty()
        )

        val updatedState = initialState.copy(shouldDismissEffect = Effect.of(Unit))
        expectLoadedDataForMailbox(entryPoint, items, initialState = initialState)
        coEvery { labelConversations(any(), any(), any(), any()) } returns Unit.right()

        // When
        val action = LabelAsOperation.LabelAsAction.OperationConfirmed(true)

        // Then
        verifyConversationMove(initialData, initialState, action, updatedState, conversationId)
    }

    @Test
    fun `should propagate an error when operation confirmed errors`() = runTest {
        // Given
        val conversationId = ConversationId("item1")
        val labelId = MailLabelId.Custom.Label(labelId)
        val uiModel = LabelAsBottomSheetUiModel(id = labelId, text = TextUiModel("Text"), icon = 1, iconTint = null)
        val unselectedUiModel = LabelUiModelWithSelectedState(uiModel, selectedState = LabelSelectedState.NotSelected)

        val initialState = LabelAsState.Data(
            entryPoint = defaultInitialData.entryPoint,
            labelUiModels = listOf(unselectedUiModel).toImmutableList(),
            shouldDismissEffect = Effect.empty(),
            errorEffect = Effect.empty()
        )

        val updatedState = initialState.copy(
            shouldDismissEffect = Effect.of(Unit),
            errorEffect = Effect.of(TextUiModel(R.string.bottom_sheet_label_as_error_apply))
        )

        expectLoadedDataForConversation(conversationId = conversationId, initialState = initialState)
        coEvery { labelConversations(any(), any(), any(), any()) } returns DataError.Local.Unknown.left()

        // When
        val confirmAction = LabelAsOperation.LabelAsAction.OperationConfirmed(false)

        // Then
        verifyConversationMove(defaultInitialData, initialState, confirmAction, updatedState, conversationId)
    }

    private fun viewModel(initialData: LabelAsBottomSheet.InitialData) = LabelAsViewModel(
        initialData = initialData,
        observePrimaryUserId = observePrimaryUserId,
        getLabelAsBottomSheetContent = getLabelAsBottomSheetContent,
        labelMessages = labelMessages,
        labelConversations = labelConversations,
        reducer = reducer
    )

    private suspend fun verifyMessageMove(
        initialData: LabelAsBottomSheet.InitialData,
        initialState: LabelAsState.Data,
        action: LabelAsOperation.LabelAsAction.OperationConfirmed,
        updatedState: LabelAsState.Data,
        messageId: MessageId
    ) {
        val viewModel = viewModel(initialData)
        viewModel.state.test {
            assertEquals(initialState, awaitItem())

            viewModel.submit(action)
            assertEquals(updatedState, awaitItem())
        }

        verify { labelConversations wasNot called }
        coVerify(exactly = 1) {
            labelMessages(
                userId,
                messageIds = listOf(messageId),
                updatedSelections = LabelSelectionList(emptyList(), emptyList()),
                shouldArchive = action.alsoArchive
            )
        }
        confirmVerified(labelMessages, labelConversations)
    }

    private suspend fun verifyConversationMove(
        initialData: LabelAsBottomSheet.InitialData,
        initialState: LabelAsState.Data,
        action: LabelAsOperation.LabelAsAction.OperationConfirmed,
        updatedState: LabelAsState,
        conversationId: ConversationId
    ) {
        val viewModel = viewModel(initialData)
        viewModel.state.test {
            assertEquals(initialState, awaitItem())

            viewModel.submit(action)
            assertEquals(updatedState, awaitItem())
        }

        verify { labelMessages wasNot called }
        coVerify(exactly = 1) {
            labelConversations(
                userId,
                conversationIds = listOf(conversationId),
                updatedSelections = LabelSelectionList(emptyList(), emptyList()),
                shouldArchive = action.alsoArchive
            )
        }
        confirmVerified(labelMessages, labelConversations)
    }

    private fun expectLoadedDataForConversation(
        conversationId: ConversationId,
        entryPoint: LabelAsBottomSheetEntryPoint = LabelAsBottomSheetEntryPoint.Conversation,
        initialState: LabelAsState = defaultDataState
    ) {
        setupInitialDataLoading(actions = defaultLabelAsActions, entryPoint = entryPoint, state = initialState)

        coEvery {
            getLabelAsBottomSheetContent.forConversation(userId, labelId, conversationId)
        } returns defaultLabelAsActions.right()
    }

    private fun expectLoadedDataForMessage(
        messageId: MessageId,
        entryPoint: LabelAsBottomSheetEntryPoint = LabelAsBottomSheetEntryPoint.Message(messageId),
        initialState: LabelAsState = defaultDataState
    ) {
        setupInitialDataLoading(actions = defaultLabelAsActions, entryPoint = entryPoint, state = initialState)

        coEvery {
            getLabelAsBottomSheetContent.forMessage(userId, labelId, messageId)
        } returns defaultLabelAsActions.right()
    }

    private fun expectLoadedDataForMailbox(
        entryPoint: LabelAsBottomSheetEntryPoint.Mailbox,
        items: List<LabelAsItemId>,
        initialState: LabelAsState = defaultDataState
    ) {
        setupInitialDataLoading(actions = defaultLabelAsActions, entryPoint = entryPoint, state = initialState)

        coEvery {
            getLabelAsBottomSheetContent.forMailbox(userId, labelId, items, entryPoint.viewMode)
        } returns defaultLabelAsActions.right()
    }

    private fun setupInitialDataLoading(
        actions: LabelAsActions,
        entryPoint: LabelAsBottomSheetEntryPoint,
        state: LabelAsState
    ) {
        every {
            reducer.newStateFrom(
                contentState = LabelAsState.Loading,
                event = LabelAsOperation.LabelAsEvent.InitialData(actions = actions, entryPoint = entryPoint)
            )
        } returns state
    }

    private companion object {

        val userId = UserId("userId")
        val labelId = LabelId("labelId")
        val items = listOf(LabelAsItemId("item1"))

        val defaultLabelAsActions = mockk<LabelAsActions>()
        val defaultDataState = mockk<LabelAsState.Data>()
        val defaultInitialData = LabelAsBottomSheet.InitialData(
            userId = userId,
            currentLocationLabelId = labelId,
            items = items,
            entryPoint = LabelAsBottomSheetEntryPoint.Conversation
        )
    }
}
