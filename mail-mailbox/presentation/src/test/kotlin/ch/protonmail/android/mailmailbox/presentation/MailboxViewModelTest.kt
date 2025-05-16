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

package ch.protonmail.android.mailmailbox.presentation

import android.graphics.Color
import android.util.Log
import androidx.paging.PagingData
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.OpenAttachmentIntentValues
import ch.protonmail.android.mailattachments.domain.usecase.GetAttachmentIntentValues
import ch.protonmail.android.mailattachments.presentation.model.AttachmentIdUiModel
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcommon.presentation.model.BottomBarState
import ch.protonmail.android.mailcommon.presentation.sample.ActionUiModelSample
import ch.protonmail.android.mailcommon.presentation.ui.delete.DeleteDialogState
import ch.protonmail.android.mailconversation.domain.usecase.DeleteConversations
import ch.protonmail.android.mailconversation.domain.usecase.MarkConversationsAsRead
import ch.protonmail.android.mailconversation.domain.usecase.MarkConversationsAsUnread
import ch.protonmail.android.mailconversation.domain.usecase.MoveConversations
import ch.protonmail.android.mailconversation.domain.usecase.StarConversations
import ch.protonmail.android.mailconversation.domain.usecase.UnStarConversations
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabels
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.FindLocalSystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveCurrentViewMode
import ch.protonmail.android.maillabel.domain.usecase.ObserveMailLabels
import ch.protonmail.android.maillabel.presentation.text
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType.Conversation
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType.Message
import ch.protonmail.android.mailmailbox.domain.model.OpenMailboxItemRequest
import ch.protonmail.android.mailmailbox.domain.usecase.GetBottomBarActions
import ch.protonmail.android.mailmailbox.domain.usecase.GetBottomSheetActions
import ch.protonmail.android.mailmailbox.domain.usecase.ObserveUnreadCounters
import ch.protonmail.android.mailmailbox.presentation.helper.MailboxAsyncPagingDataDiffer
import ch.protonmail.android.mailmailbox.presentation.mailbox.MailboxViewModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.MailboxItemUiModelMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.SwipeActionsMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxEvent
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxTopAppBarState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxViewAction
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.SwipeActionsUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.UnreadFilterState
import ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata.MailboxSearchStateSampleData
import ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata.MailboxStateSampleData
import ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata.SwipeUiModelSampleData
import ch.protonmail.android.mailmailbox.presentation.mailbox.reducer.MailboxReducer
import ch.protonmail.android.mailmailbox.presentation.paging.MailboxPagerFactory
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.usecase.DeleteMessages
import ch.protonmail.android.mailmessage.domain.usecase.DeleteSearchResults
import ch.protonmail.android.mailmessage.domain.usecase.HandleAvatarImageLoadingFailure
import ch.protonmail.android.mailmessage.domain.usecase.LoadAvatarImage
import ch.protonmail.android.mailmessage.domain.usecase.MarkMessagesAsRead
import ch.protonmail.android.mailmessage.domain.usecase.MarkMessagesAsUnread
import ch.protonmail.android.mailmessage.domain.usecase.MoveMessages
import ch.protonmail.android.mailmessage.domain.usecase.ObserveAvatarImageStates
import ch.protonmail.android.mailmessage.domain.usecase.StarMessages
import ch.protonmail.android.mailmessage.domain.usecase.UnStarMessages
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.BottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MailboxMoreActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.UpsellingBottomSheetState
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.FolderColorSettings
import ch.protonmail.android.mailsettings.domain.model.SwipeActionsPreference
import ch.protonmail.android.mailsettings.domain.usecase.ObserveFolderColorSettings
import ch.protonmail.android.mailsettings.domain.usecase.ObserveSwipeActionsPreference
import ch.protonmail.android.testdata.avatar.AvatarImageStatesTestData
import ch.protonmail.android.testdata.avatar.AvatarImagesUiModelTestData
import ch.protonmail.android.testdata.mailbox.MailboxItemUiModelTestData.buildMailboxUiModelItem
import ch.protonmail.android.testdata.mailbox.MailboxItemUiModelTestData.draftMailboxItemUiModel
import ch.protonmail.android.testdata.mailbox.MailboxItemUiModelTestData.readMailboxItemUiModel
import ch.protonmail.android.testdata.mailbox.MailboxItemUiModelTestData.unreadMailboxItemUiModel
import ch.protonmail.android.testdata.mailbox.MailboxItemUiModelTestData.unreadMailboxItemUiModelWithLabel
import ch.protonmail.android.testdata.mailbox.MailboxTestData.readMailboxItem
import ch.protonmail.android.testdata.mailbox.MailboxTestData.unreadMailboxItem
import ch.protonmail.android.testdata.mailbox.UnreadCountersTestData
import ch.protonmail.android.testdata.mailbox.UnreadCountersTestData.update
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData.userId
import ch.protonmail.android.testdata.user.UserIdTestData.userId1
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.android.core.accountmanager.domain.usecase.ObservePrimaryAccountAvatarItem
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.SwipeAction
import me.proton.core.mailsettings.domain.entity.ViewMode
import me.proton.core.mailsettings.domain.entity.ViewMode.ConversationGrouping
import me.proton.core.mailsettings.domain.entity.ViewMode.NoConversationGrouping
import me.proton.core.test.kotlin.TestDispatcherProvider
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MailboxViewModelTest {

    private val initialLocationMailLabelId = MailLabelTestData.archiveSystemLabel.id
    private val actionUiModelMapper = ActionUiModelMapper()
    private val swipeActionsMapper = SwipeActionsMapper()

    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk.invoke() } returns flowOf(userId)
    }

    private val selectedMailLabelId = mockk<SelectedMailLabelId> {
        every { this@mockk.flow } returns MutableStateFlow<MailLabelId>(initialLocationMailLabelId)
    }

    private val observeMailLabels = mockk<ObserveMailLabels> {
        every { this@mockk.invoke(any()) } returns MutableStateFlow(
            MailLabels(
                system = MailLabelTestData.dynamicSystemLabels,
                folders = emptyList(),
                labels = listOf(MailLabelTestData.customLabelOne)
            )
        )
    }
    private val observeSwipeActionsPreference = mockk<ObserveSwipeActionsPreference> {
        every { this@mockk(userId) } returns flowOf(SwipeActionsPreference(SwipeAction.MarkRead, SwipeAction.Archive))
        every { this@mockk(userId1) } returns flowOf(SwipeActionsPreference(SwipeAction.MarkRead, SwipeAction.Archive))
    }

    private val observeCurrentViewMode = mockk<ObserveCurrentViewMode> {
        coEvery { this@mockk(userId = any()) } returns flowOf(NoConversationGrouping)
        coEvery { this@mockk(any(), any()) } returns flowOf(NoConversationGrouping)
    }

    private val observeUnreadCounters = mockk<ObserveUnreadCounters> {
        coEvery { this@mockk(userId = any()) } returns flowOf(UnreadCountersTestData.systemUnreadCounters)
    }

    private val pagerFactory = mockk<MailboxPagerFactory>()

    private val mailboxItemMapper = mockk<MailboxItemUiModelMapper>()

    private val mailboxReducer = mockk<MailboxReducer> {
        every { newStateFrom(any(), any()) } returns MailboxStateSampleData.Loading
    }

    private val observeMailboxActions = mockk<GetBottomBarActions> {
        coEvery { this@mockk(any(), any(), any(), any()) } returns listOf(Action.Archive, Action.Trash).right()
    }

    private val getAttachmentIntentValues = mockk<GetAttachmentIntentValues>()

    private val findLocalSystemLabelId = mockk<FindLocalSystemLabelId>()
    private val markConversationsAsRead = mockk<MarkConversationsAsRead>()
    private val markConversationsAsUnread = mockk<MarkConversationsAsUnread>()
    private val markMessagesAsRead = mockk<MarkMessagesAsRead>()
    private val markMessagesAsUnread = mockk<MarkMessagesAsUnread>()
    private val moveConversations = mockk<MoveConversations>()
    private val moveMessages = mockk<MoveMessages>()
    private val deleteConversations = mockk<DeleteConversations>()
    private val deleteMessages = mockk<DeleteMessages>()
    private val starMessages = mockk<StarMessages>()
    private val starConversations = mockk<StarConversations>()
    private val unStarMessages = mockk<UnStarMessages>()
    private val unStarConversations = mockk<UnStarConversations>()
    private val deleteSearchResults = mockk<DeleteSearchResults>()
    private val getBottomSheetActions = mockk<GetBottomSheetActions>()
    private val observePrimaryAccountAvatarItem = mockk<ObservePrimaryAccountAvatarItem> {
        every { this@mockk() } returns flowOf()
    }

    private val observeFolderColorSettings = mockk<ObserveFolderColorSettings> {
        every { this@mockk(userId) } returns flowOf(
            FolderColorSettings(
                useFolderColor = true,
                inheritParentFolderColor = true
            )
        )
        every { this@mockk(userId1) } returns flowOf(
            FolderColorSettings(
                useFolderColor = true,
                inheritParentFolderColor = true
            )
        )
    }

    private val loadAvatarImage = mockk<LoadAvatarImage> {
        every { this@mockk.invoke(any(), any()) } returns Unit
    }

    private val handleAvatarImageLoadingFailure = mockk<HandleAvatarImageLoadingFailure> {
        every { this@mockk.invoke(any(), any()) } returns Unit
    }

    private val observeAvatarImageStates = mockk<ObserveAvatarImageStates> {
        every { this@mockk() } returns flowOf()
    }

    private val mailboxViewModel by lazy {
        MailboxViewModel(
            mailboxPagerFactory = pagerFactory,
            observeCurrentViewMode = observeCurrentViewMode,
            observePrimaryUserId = observePrimaryUserId,
            observeMailLabels = observeMailLabels,
            observeSwipeActionsPreference = observeSwipeActionsPreference,
            selectedMailLabelId = selectedMailLabelId,
            observeUnreadCounters = observeUnreadCounters,
            observeFolderColorSettings = observeFolderColorSettings,
            getBottomBarActions = observeMailboxActions,
            getBottomSheetActions = getBottomSheetActions,
            actionUiModelMapper = actionUiModelMapper,
            mailboxItemMapper = mailboxItemMapper,
            swipeActionsMapper = swipeActionsMapper,
            markConversationsAsRead = markConversationsAsRead,
            markConversationsAsUnread = markConversationsAsUnread,
            markMessagesAsRead = markMessagesAsRead,
            markMessagesAsUnread = markMessagesAsUnread,
            moveConversations = moveConversations,
            moveMessages = moveMessages,
            deleteConversations = deleteConversations,
            deleteMessages = deleteMessages,
            starMessages = starMessages,
            starConversations = starConversations,
            unStarMessages = unStarMessages,
            unStarConversations = unStarConversations,
            mailboxReducer = mailboxReducer,
            dispatchersProvider = TestDispatcherProvider(),
            deleteSearchResults = deleteSearchResults,
            findLocalSystemLabelId = findLocalSystemLabelId,
            loadAvatarImage = loadAvatarImage,
            handleAvatarImageLoadingFailure = handleAvatarImageLoadingFailure,
            observeAvatarImageStates = observeAvatarImageStates,
            observePrimaryAccountAvatarItem = observePrimaryAccountAvatarItem,
            isComposerEnabled = flowOf(true),
            getAttachmentIntentValues = getAttachmentIntentValues
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkStatic(Log::class)
        mockkStatic(Color::parseColor)
        every { Log.isLoggable(any(), any()) } returns false
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
        unmockkStatic(Color::class)
    }

    @Test
    fun `emits initial mailbox state when initialized`() = runTest {
        // Given
        coEvery { observeUnreadCounters(userId = any()) } returns emptyFlow()
        coEvery { observeMailLabels(userId = any()) } returns emptyFlow()

        // When
        mailboxViewModel.state.test {
            // Then
            val actual = awaitItem()
            val expected = MailboxState(
                mailboxListState = MailboxListState.Loading,
                topAppBarState = MailboxTopAppBarState.Loading,
                unreadFilterState = UnreadFilterState.Loading,
                bottomAppBarState = BottomBarState.Data.Hidden(emptyList<ActionUiModel>().toImmutableList()),
                deleteDialogState = DeleteDialogState.Hidden,
                bottomSheetState = null,
                actionResult = Effect.empty(),
                error = Effect.empty()
            )

            assertEquals(expected, actual)

            verify { pagerFactory wasNot Called }
        }
    }

    @Test
    fun `when new location selected, new state is created and emitted`() = runTest {
        // Given
        val expectedMailLabel = MailLabelTestData.spamSystemLabel
        val expectedCount = UnreadCountersTestData.labelToCounterMap[expectedMailLabel.id.labelId]
        val expectedState = createMailboxDataState(
            selectedMailLabelId = expectedMailLabel.id,
            scrollToMailboxTop = Effect.of(expectedMailLabel.id)
        )
        val currentLocationFlow = MutableStateFlow<MailLabelId>(MailLabelTestData.inboxSystemLabel.id)
        every { selectedMailLabelId.flow } returns currentLocationFlow
        every {
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.NewLabelSelected(expectedMailLabel, expectedCount)
            )
        } returns expectedState
        returnExpectedStateForBottomBarEvent(expectedState = expectedState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem()

            currentLocationFlow.emit(expectedMailLabel.id)

            // Then
            assertEquals(expectedState, awaitItem())
            awaitItem() // swipe gestures
        }
    }

    @Test
    fun `when new location selected, new bottom bar state is created and emitted`() = runTest {
        // Given
        val expectedMailLabel = MailLabelTestData.spamSystemLabel
        val expectedCount = UnreadCountersTestData.labelToCounterMap[expectedMailLabel.id.labelId]
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            currentMailLabel = expectedMailLabel,
            selectedMailboxItemUiModels = listOf(unreadMailboxItemUiModelWithLabel)
        )
        val expectedState = intermediateState.copy(
            bottomAppBarState = BottomBarState.Data.Shown(
                actions = listOf(
                    ActionUiModelSample.Archive,
                    ActionUiModelSample.Trash
                ).toImmutableList()
            )
        )
        val currentLocationFlow = MutableStateFlow<MailLabelId>(MailLabelTestData.inboxSystemLabel.id)
        every { selectedMailLabelId.flow } returns currentLocationFlow
        every {
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.NewLabelSelected(expectedMailLabel, expectedCount)
            )
        } returns intermediateState
        returnExpectedStateForBottomBarEvent(expectedState = expectedState)

        mailboxViewModel.state.test {
            awaitItem()

            // When
            currentLocationFlow.emit(expectedMailLabel.id)

            // Then
            assertEquals(intermediateState, awaitItem())
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when selected label changes, new state is created and emitted`() = runTest {
        // Given
        val initialMailLabel = MailLabelTestData.customLabelOne
        val modifiedMailLabel = MailLabelTestData.customLabelTwo
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = modifiedMailLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.of(initialMailLabel.id),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        val expectedSwipeActions = SwipeActionsUiModel(
            end = SwipeUiModelSampleData.MarkRead,
            start = SwipeUiModelSampleData.Archive
        )
        val expectedStateWithSwipeGestures = expectedState.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = modifiedMailLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.of(initialMailLabel.id),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false,
                swipeActions = expectedSwipeActions,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        val mailLabelsFlow = MutableStateFlow(
            MailLabels(
                system = MailLabelTestData.dynamicSystemLabels,
                folders = emptyList(),
                labels = listOf(
                    MailLabelTestData.customLabelOne,
                    MailLabelTestData.customLabelTwo
                )
            )
        )
        val currentLocationFlow = MutableStateFlow<MailLabelId>(initialMailLabel.id)
        every { observeMailLabels(userId) } returns mailLabelsFlow
        every { selectedMailLabelId.flow } returns currentLocationFlow

        every {
            mailboxReducer.newStateFrom(any(), MailboxEvent.NewLabelSelected(modifiedMailLabel, null))
        } returns expectedState
        every {
            mailboxReducer.newStateFrom(expectedState, MailboxEvent.SwipeActionsChanged(expectedSwipeActions))
        } returns expectedStateWithSwipeGestures

        mailboxViewModel.state.test {
            awaitItem()

            // When
            currentLocationFlow.emit(modifiedMailLabel.id)

            // Then
            assertEquals(expectedState, awaitItem())
            assertEquals(expectedStateWithSwipeGestures, awaitItem())
        }
    }

    @Test
    fun `when userId changes, new state is created and emitted`() = runTest {
        // Given
        val initialMailLabel = MailLabelTestData.customLabelOne
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = initialMailLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.of(initialMailLabel.id),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        val expectedSwipeActions = SwipeActionsUiModel(
            end = SwipeUiModelSampleData.MarkRead,
            start = SwipeUiModelSampleData.Archive
        )
        val expectedStateWithSwipeGestures = expectedState.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = initialMailLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.of(initialMailLabel.id),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false,
                swipeActions = expectedSwipeActions,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        val mailLabelsFlow = MutableStateFlow(
            MailLabels(
                system = MailLabelTestData.dynamicSystemLabels,
                folders = emptyList(),
                labels = listOf(
                    MailLabelTestData.customLabelOne,
                    MailLabelTestData.customLabelTwo
                )
            )
        )
        val currentLocationFlow = MutableStateFlow<MailLabelId>(initialMailLabel.id)
        every { observeMailLabels(userId) } returns mailLabelsFlow
        every { selectedMailLabelId.flow } returns currentLocationFlow

        val currentUserIdFlow = MutableStateFlow(userId)
        every { observePrimaryUserId() } returns currentUserIdFlow

        every {
            mailboxReducer.newStateFrom(any(), MailboxEvent.NewLabelSelected(initialMailLabel, null))
        } returns expectedState
        every {
            mailboxReducer.newStateFrom(any(), MailboxEvent.SwipeActionsChanged(expectedSwipeActions))
        } returns expectedStateWithSwipeGestures

        mailboxViewModel.state.test {
            awaitItem()

            // When
            currentUserIdFlow.emit(userId1)
            advanceUntilIdle()

            // Then
            assertEquals(MailboxStateSampleData.Loading, awaitItem())
            assertEquals(expectedState, awaitItem())
            assertEquals(expectedStateWithSwipeGestures, awaitItem())
        }
    }

    @Test
    fun `when selected label is deleted, new state is created and emitted`() = runTest {
        // Given
        val initialMailLabel = MailLabelTestData.customLabelOne
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.inboxSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.of(initialMailLabel.id),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        val mailLabelsFlow = MutableStateFlow(
            MailLabels(
                system = MailLabelTestData.dynamicSystemLabels,
                folders = emptyList(),
                labels = listOf(MailLabelTestData.customLabelOne, MailLabelTestData.customLabelTwo)
            )
        )
        val currentLocationFlow = MutableStateFlow<MailLabelId>(initialMailLabel.id)
        every { observeMailLabels(userId) } returns mailLabelsFlow
        every { selectedMailLabelId.flow } returns currentLocationFlow
        every {
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.SelectedLabelChanged(MailLabelTestData.inboxSystemLabel)
            )
        } returns expectedState

        coEvery { findLocalSystemLabelId(userId, SystemLabelId.Inbox) } returns MailLabelId.System(LabelId("0"))

        mailboxViewModel.state.test {
            awaitItem()

            mailLabelsFlow.emit(
                MailLabels(
                    system = MailLabelTestData.dynamicSystemLabels,
                    folders = emptyList(),
                    labels = listOf(MailLabelTestData.customLabelTwo)
                )
            )

            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when counters for selected location change, new state is created and emitted`() = runTest {
        // Given
        val expectedCount = 42
        val expectedState = MailboxStateSampleData.Loading.copy(
            unreadFilterState = UnreadFilterState.Data(expectedCount, false)
        )
        val currentCountersFlow = MutableStateFlow(UnreadCountersTestData.systemUnreadCounters)
        val modifiedCounters = UnreadCountersTestData.systemUnreadCounters
            .update(initialLocationMailLabelId.labelId, expectedCount)
        coEvery { observeUnreadCounters(userId) } returns currentCountersFlow
        every {
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.SelectedLabelCountChanged(expectedCount)
            )
        } returns expectedState

        mailboxViewModel.state.test {
            awaitItem()

            currentCountersFlow.emit(modifiedCounters)

            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when counters for a different location change, should not produce nor emit a new state`() = runTest {
        // Given
        val expectedCount = 42
        val expectedState = MailboxStateSampleData.Loading.copy(
            unreadFilterState = UnreadFilterState.Data(expectedCount, false)
        )
        val currentCountersFlow = MutableStateFlow(UnreadCountersTestData.systemUnreadCounters)
        val modifiedCounters = UnreadCountersTestData.systemUnreadCounters
            .update(SystemLabelId.Spam.labelId, expectedCount)
        coEvery { observeUnreadCounters(userId) } returns currentCountersFlow
        every {
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.SelectedLabelCountChanged(expectedCount)
            )
        } returns expectedState

        mailboxViewModel.state.test {
            awaitItem()

            currentCountersFlow.emit(modifiedCounters)

            // Then
            verify(exactly = 0) {
                mailboxReducer.newStateFrom(any(), MailboxEvent.SelectedLabelCountChanged(expectedCount))
            }
            expectNoEvents()
        }
    }

    @Test
    fun `when long item click action is submitted and state is view mode, new state is created and emitted`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val intermediateState = createMailboxDataState()
            val expectedSelectionState = MailboxStateSampleData.createSelectionMode(listOf(item))
            val expectedBottomBarState = expectedSelectionState.copy(
                bottomAppBarState = BottomBarState.Data.Shown(
                    listOf(ActionUiModelSample.Archive, ActionUiModelSample.Trash).toImmutableList()
                )
            )
            expectedSelectedLabelCountStateChange(intermediateState)
            returnExpectedStateWhenEnterSelectionMode(intermediateState, item, expectedSelectionState)
            returnExpectedStateForBottomBarEvent(expectedSelectionState, expectedBottomBarState)
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemLongClicked(item))

                // Then
                assertEquals(expectedSelectionState, awaitItem())
                assertEquals(expectedBottomBarState, awaitItem())
            }
        }

    @Test
    fun `when long item click action is submitted and state is not view mode, no new state is created nor emitted`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val intermediateState = createMailboxDataState()
            val expectedSelectionState = MailboxStateSampleData.createSelectionMode(listOf(item))
            val expectedBottomBarState = expectedSelectionState.copy(
                bottomAppBarState = BottomBarState.Data.Shown(
                    listOf(ActionUiModelSample.Archive, ActionUiModelSample.Trash).toImmutableList()
                )
            )
            expectedSelectedLabelCountStateChange(intermediateState)
            returnExpectedStateWhenEnterSelectionMode(intermediateState, item, expectedSelectionState)
            returnExpectedStateForBottomBarEvent(expectedSelectionState, expectedBottomBarState)
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemLongClicked(item))

                // Then
                assertEquals(expectedSelectionState, awaitItem())
                assertEquals(expectedBottomBarState, awaitItem())

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemLongClicked(item))

                // Then
                verify(exactly = 1) { mailboxReducer.newStateFrom(any(), MailboxEvent.EnterSelectionMode(item)) }
            }
        }

    @Test
    fun `when avatar click action is submitted and state is view mode, new state is created and emitted`() = runTest {
        // Given
        val item = readMailboxItemUiModel
        val intermediateState = createMailboxDataState()
        val expectedState = MailboxStateSampleData.createSelectionMode(listOf(item))
        val expectedBottomBarActions = listOf(ActionUiModelSample.Archive, ActionUiModelSample.Trash).toImmutableList()
        val expectedBottomBarState = MailboxStateSampleData.createSelectionMode(
            selectedMailboxItemUiModels = listOf(item),
            bottomBarAction = expectedBottomBarActions
        )
        expectedSelectedLabelCountStateChange(intermediateState)
        returnExpectedStateWhenEnterSelectionMode(intermediateState, item, expectedState)
        every {
            mailboxReducer.newStateFrom(
                currentState = expectedState,
                operation = MailboxEvent.MessageBottomBarEvent(BottomBarEvent.ActionsData(expectedBottomBarActions))
            )
        } returns expectedBottomBarState
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

            // Then
            assertEquals(expectedState, awaitItem())
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(intermediateState, MailboxEvent.EnterSelectionMode(item))
            }
            assertEquals(expectedBottomBarState, awaitItem())
        }
    }

    @Test
    fun `when avatar click action is submitted to add item to selection, new state is created and emitted`() = runTest {
        // Given
        val item = readMailboxItemUiModel
        val secondItem = draftMailboxItemUiModel
        val dataState = createMailboxDataState()
        val intermediateSelectionState = MailboxStateSampleData.createSelectionMode(listOf(item))
        val expectedSelectionState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))

        val expectedBottomBarActions = listOf(ActionUiModelSample.Archive, ActionUiModelSample.Trash).toImmutableList()

        val expectedBottomBarState = MailboxStateSampleData.createSelectionMode(
            selectedMailboxItemUiModels = listOf(item),
            bottomBarAction = expectedBottomBarActions
        )
        expectedSelectedLabelCountStateChange(dataState)
        returnExpectedStateWhenEnterSelectionMode(dataState, item, intermediateSelectionState)
        every {
            mailboxReducer.newStateFrom(
                currentState = intermediateSelectionState,
                operation = MailboxEvent.MessageBottomBarEvent(BottomBarEvent.ActionsData(expectedBottomBarActions))
            )
        } returns expectedBottomBarState
        every {
            mailboxReducer.newStateFrom(
                currentState = expectedBottomBarState,
                operation = MailboxEvent.ItemClicked.ItemAddedToSelection(secondItem)
            )
        } returns expectedSelectionState
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

            // Then
            assertEquals(intermediateSelectionState, awaitItem())
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(dataState, MailboxEvent.EnterSelectionMode(item))
            }
            assertEquals(expectedBottomBarState, awaitItem())

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(secondItem))

            // Then
            assertEquals(expectedSelectionState, awaitItem())
        }
    }

    @Test
    fun `when avatar click action is submitted to remove last item from selection, exit selection mode is triggered`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val initialState = createMailboxDataState()
            val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item))
            expectedSelectedLabelCountStateChange(initialState)
            returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
            returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
            every {
                mailboxReducer.newStateFrom(
                    intermediateState,
                    MailboxViewAction.ExitSelectionMode
                )
            } returns initialState
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(intermediateState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
                }

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(initialState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(
                        currentState = intermediateState,
                        operation = MailboxViewAction.ExitSelectionMode
                    )
                }
            }
        }

    @Test
    fun `when avatar click action is submitted to remove item from selection, new state is created and emitted`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val secondItem = unreadMailboxItemUiModel
            val initialState = createMailboxDataState()
            val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))
            val expectedState = MailboxStateSampleData.createSelectionMode(listOf(secondItem))
            expectedSelectedLabelCountStateChange(initialState)
            returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
            returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
            every {
                mailboxReducer.newStateFrom(
                    intermediateState,
                    MailboxEvent.ItemClicked.ItemRemovedFromSelection(item)
                )
            } returns expectedState
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(intermediateState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
                }

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(expectedState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(
                        currentState = intermediateState,
                        operation = MailboxEvent.ItemClicked.ItemRemovedFromSelection(item)
                    )
                }
            }
        }

    @Test
    fun `when exit selection mode action submitted, new state is created and emitted`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Loading.copy(
            topAppBarState = MailboxTopAppBarState.Data.DefaultMode(
                currentLabelName = MailLabelTestData.inboxSystemLabel.text(),
                primaryAvatarItem = null
            )
        )
        every {
            mailboxReducer.newStateFrom(MailboxStateSampleData.Loading, MailboxViewAction.ExitSelectionMode)
        } returns expectedState

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.ExitSelectionMode)

            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `mailbox items for the current location are requested when location changes`() = runTest {
        // Given
        val currentLocationFlow = MutableStateFlow<MailLabelId>(initialLocationMailLabelId)
        val initialMailboxState = createMailboxDataState()
        val expectedState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.spamSystemLabel.id)
        every { selectedMailLabelId.flow } returns currentLocationFlow
        expectPagerMock(
            filterUnread = false,
            itemType = Message,
            pagingDataFlow = flowOf(PagingData.from(listOf(unreadMailboxItem)))
        )
        every { mailboxReducer.newStateFrom(any(), any()) } returns initialMailboxState
        every {
            mailboxReducer.newStateFrom(
                initialMailboxState,
                MailboxEvent.NewLabelSelected(
                    MailLabelTestData.spamSystemLabel,
                    UnreadCountersTestData.labelToCounterMap[MailLabelTestData.spamSystemLabel.id.labelId]!!
                )
            )
        } returns expectedState
        returnExpectedStateForBottomBarEvent(expectedState = expectedState)

        mailboxViewModel.items.test {
            // Then
            awaitItem()
            verify { pagerFactory.create(userId, initialLocationMailLabelId, false, Message, any()) }

            // When
            currentLocationFlow.emit(MailLabelTestData.spamSystemLabel.id)

            // Then
            awaitItem()
            verify { pagerFactory.create(userId, MailLabelTestData.spamSystemLabel.id, false, Message, any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mailbox items are mapped to mailbox item ui models`() = runTest {
        // Given
        val folderColorSettings = FolderColorSettings(
            useFolderColor = true,
            inheritParentFolderColor = true
        )
        coEvery {
            mailboxItemMapper.toUiModel(userId, unreadMailboxItem, folderColorSettings, false)
        } returns unreadMailboxItemUiModel
        coEvery {
            mailboxItemMapper.toUiModel(userId, readMailboxItem, folderColorSettings, false)
        } returns readMailboxItemUiModel
        expectPagerMock(
            pagingDataFlow = flowOf(PagingData.from(listOf(unreadMailboxItem, readMailboxItem)))
        )
        every { mailboxReducer.newStateFrom(any(), any()) } returns createMailboxDataState()
        val differ = MailboxAsyncPagingDataDiffer.differ

        // When
        mailboxViewModel.items.test {
            // Initial item is an empty page to clear the currently shown items (when switching label)
            awaitItem()
            // Then
            val pagingData = awaitItem()
            differ.submitData(pagingData)

            val expected = listOf(unreadMailboxItemUiModel, readMailboxItemUiModel)
            assertEquals(expected, differ.snapshot().items)
        }
    }

    @Test
    fun `mailbox items are not mapped again in the same page when folder color change`() = runTest {
        // See ET-2929: avoid updating the MailboxItems when the page is not re-created (eg. folder color update)
        // this is needed to respect paging lib's immutable pages requirement.
        // Given
        val initialFolderColorSettings = FolderColorSettings(useFolderColor = true, inheritParentFolderColor = true)
        val updateFolderColorSettings = FolderColorSettings(useFolderColor = false, inheritParentFolderColor = false)
        val folderColorSettingsFlow = MutableStateFlow(initialFolderColorSettings)
        coEvery {
            mailboxItemMapper.toUiModel(userId, unreadMailboxItem, initialFolderColorSettings, false)
        } returns unreadMailboxItemUiModel
        expectPagerMock(
            pagingDataFlow = flowOf(PagingData.from(listOf(unreadMailboxItem)))
        )
        every { mailboxReducer.newStateFrom(any(), any()) } returns createMailboxDataState()
        every { observeFolderColorSettings(userId) } returns folderColorSettingsFlow
        val differ = MailboxAsyncPagingDataDiffer.differ

        mailboxViewModel.items.test {
            // Initial item is an empty page to clear the currently shown items (when switching label)
            awaitItem()
            // First actual emission
            val pagingData = awaitItem()
            differ.submitData(pagingData)

            // When
            folderColorSettingsFlow.emit(updateFolderColorSettings)

            // Then
            val expected = listOf(unreadMailboxItemUiModel)
            assertEquals(expected, differ.snapshot().items)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `user contacts are used to map mailbox items to ui models`() = runTest {
        // Given
        val folderColorSettings = FolderColorSettings(
            useFolderColor = true,
            inheritParentFolderColor = true
        )
        coEvery {
            mailboxItemMapper.toUiModel(
                userId, unreadMailboxItem, folderColorSettings, false
            )
        } returns unreadMailboxItemUiModel
        coEvery {
            mailboxItemMapper.toUiModel(
                userId, readMailboxItem, folderColorSettings,
                false
            )
        } returns readMailboxItemUiModel
        expectPagerMock(
            pagingDataFlow = flowOf(PagingData.from(listOf(unreadMailboxItem, readMailboxItem)))
        )
        every { mailboxReducer.newStateFrom(any(), any()) } returns createMailboxDataState()
        val differ = MailboxAsyncPagingDataDiffer.differ

        // When
        mailboxViewModel.items.test {
            // Initial item is an empty page to clear the currently shown items (when switching label)
            awaitItem()
            // Then
            val pagingData = awaitItem()
            differ.submitData(pagingData)

            coVerify {
                mailboxItemMapper.toUiModel(
                    userId, any(), folderColorSettings, false
                )
            }
        }
    }

    @Test
    fun `when open item action submitted in message mode, new state is produced and emitted`() = runTest {
        // Given
        val item = buildMailboxUiModelItem(id = "id", type = Message)
        val intermediateState = createMailboxDataState()
        val labelId = initialLocationMailLabelId.labelId
        val expectedState = createMailboxDataState(
            openEffect = Effect.of(
                OpenMailboxItemRequest(
                    MailboxItemId(item.id), shouldOpenInComposer = false, openedFromLocation = labelId
                )
            )
        )
        expectViewMode(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(intermediateState)
        every {
            mailboxReducer.newStateFrom(
                intermediateState,
                MailboxEvent.ItemClicked.ItemDetailsOpened(item, labelId)
            )
        } returns expectedState
        expectPagerMock()

        // When
        mailboxViewModel.submit(MailboxViewAction.ItemClicked(item))
        mailboxViewModel.state.test {
            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when item action to open composer submitted in draft, new state is produced and emitted`() = runTest {
        // Given
        val item = buildMailboxUiModelItem(id = "id", type = Message, shouldOpenInComposer = true)
        val intermediateState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.draftsSystemLabel.id)
        val labelId = initialLocationMailLabelId.labelId
        val expectedState = createMailboxDataState(
            selectedMailLabelId = MailLabelTestData.draftsSystemLabel.id,
            openEffect = Effect.of(
                OpenMailboxItemRequest(
                    MailboxItemId(item.id), shouldOpenInComposer = true,
                    openedFromLocation = labelId
                )
            )
        )
        expectViewMode(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(intermediateState)
        every {
            mailboxReducer.newStateFrom(
                intermediateState,
                MailboxEvent.ItemClicked.OpenComposer(item)
            )
        } returns expectedState
        expectPagerMock()

        // When
        mailboxViewModel.submit(MailboxViewAction.ItemClicked(item))
        mailboxViewModel.state.test {
            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when on offline with data is submitted, new state is produced and emitted`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.inboxSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        every {
            mailboxReducer.newStateFrom(
                MailboxStateSampleData.Loading,
                MailboxViewAction.OnOfflineWithData
            )
        } returns expectedState

        // When
        mailboxViewModel.submit(MailboxViewAction.OnOfflineWithData)
        mailboxViewModel.state.test {
            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when on error with data is submitted, new state is produced and emitted`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.inboxSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                refreshErrorEffect = Effect.of(Unit),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        every {
            mailboxReducer.newStateFrom(
                MailboxStateSampleData.Loading,
                MailboxViewAction.OnErrorWithData
            )
        } returns expectedState

        // When
        mailboxViewModel.submit(MailboxViewAction.OnErrorWithData)
        mailboxViewModel.state.test {
            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when refresh action is submitted, new state is produced and emitted`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.inboxSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = true,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        every {
            mailboxReducer.newStateFrom(
                MailboxStateSampleData.Loading,
                MailboxViewAction.Refresh
            )
        } returns expectedState

        // When
        mailboxViewModel.submit(MailboxViewAction.Refresh)
        mailboxViewModel.state.test {
            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when open item action submitted in conversation mode, new state is produced and emitted`() = runTest {
        // Given
        val item = buildMailboxUiModelItem(id = "id", type = Conversation)
        val intermediateState = createMailboxDataState()
        val labelId = initialLocationMailLabelId.labelId
        val expectedState = createMailboxDataState(
            openEffect = Effect.of(
                OpenMailboxItemRequest(
                    MailboxItemId(item.id), shouldOpenInComposer = false, openedFromLocation = labelId
                )
            )
        )

        every { observeCurrentViewMode(userId = any()) } returns flowOf(ConversationGrouping)

        expectedSelectedLabelCountStateChange(intermediateState)
        every {
            mailboxReducer.newStateFrom(
                intermediateState,
                MailboxEvent.ItemClicked.ItemDetailsOpened(item, labelId)
            )
        } returns expectedState
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // await that label count gets emitted

            // When
            mailboxViewModel.submit(MailboxViewAction.ItemClicked(item))

            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when enable unread filter action submitted, produces and emits a new state`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Loading.copy(
            unreadFilterState = UnreadFilterState.Data(5, true)
        )
        every { mailboxReducer.newStateFrom(any(), MailboxViewAction.EnableUnreadFilter) } returns expectedState

        // When
        mailboxViewModel.submit(MailboxViewAction.EnableUnreadFilter)
        mailboxViewModel.state.test {
            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when disable unread filter action submitted, produces and emits a new state`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Loading.copy(
            unreadFilterState = UnreadFilterState.Data(5, false)
        )
        every {
            mailboxReducer.newStateFrom(MailboxStateSampleData.Loading, MailboxViewAction.DisableUnreadFilter)
        } returns expectedState

        // When
        mailboxViewModel.submit(MailboxViewAction.DisableUnreadFilter)
        mailboxViewModel.state.test {
            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `mailbox pager is recreated when unread filter state changes`() = runTest {
        // Given
        val expectedMailBoxState = createMailboxDataState(unreadFilterState = false)
        every { mailboxReducer.newStateFrom(any(), any()) } returns expectedMailBoxState
        val pagingData = PagingData.from(listOf(unreadMailboxItem))
        expectPagerMock(
            selectedLabelId = MailLabelTestData.archiveSystemLabel.id,
            itemType = Message,
            pagingDataFlow = flowOf(pagingData)
        )
        every { mailboxReducer.newStateFrom(expectedMailBoxState, MailboxViewAction.EnableUnreadFilter) } returns
            createMailboxDataState(unreadFilterState = true)

        mailboxViewModel.items.test {
            // When

            // Then
            awaitItem()
            verify(exactly = 1) {
                pagerFactory.create(userId, MailLabelTestData.archiveSystemLabel.id, false, Message, any())
            }

            // When
            mailboxViewModel.submit(MailboxViewAction.EnableUnreadFilter)

            // Then
            awaitItem()
            verify(exactly = 1) {
                pagerFactory.create(userId, MailLabelTestData.archiveSystemLabel.id, true, Message, any())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mailbox pager is recreated when selected mail label state changes`() = runTest {
        // Given
        val expectedMailBoxState = createMailboxDataState(selectedMailLabelId = initialLocationMailLabelId)
        val inboxLabel = MailLabelTestData.inboxSystemLabel
        val currentLocationFlow = MutableStateFlow<MailLabelId>(initialLocationMailLabelId)
        val expectedState = createMailboxDataState(selectedMailLabelId = inboxLabel.id)
        every { selectedMailLabelId.flow } returns currentLocationFlow
        every { mailboxReducer.newStateFrom(any(), any()) } returns expectedMailBoxState
        val pagingData = PagingData.from(listOf(unreadMailboxItem))
        expectPagerMock(pagingDataFlow = flowOf(pagingData))
        every {
            mailboxReducer.newStateFrom(
                expectedMailBoxState,
                MailboxEvent.NewLabelSelected(
                    inboxLabel,
                    UnreadCountersTestData.labelToCounterMap[inboxLabel.id.labelId]!!
                )
            )
        } returns expectedState
        returnExpectedStateForBottomBarEvent(expectedState = expectedState)

        mailboxViewModel.items.test {
            // When

            // Then
            awaitItem()
            verify(exactly = 1) {
                pagerFactory.create(userId, MailLabelTestData.archiveSystemLabel.id, false, Message, any())
            }

            // When
            currentLocationFlow.emit(inboxLabel.id)

            // Then
            awaitItem()
            // mailbox pager is recreated only once when view mode for the newly selected location does not change
            verify(exactly = 1) { pagerFactory.create(userId, inboxLabel.id, false, Message, any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pager is not recreated when any state beside selectedLabel, unreadFilter, viewMode or primaryUser changes`() =
        runTest {
            // Given
            val expectedMailBoxState = createMailboxDataState(Effect.empty())
            every { mailboxReducer.newStateFrom(any(), any()) } returns expectedMailBoxState
            val pagingData = PagingData.from(listOf(unreadMailboxItem))
            expectPagerMock(pagingDataFlow = flowOf(pagingData))
            val labelId = initialLocationMailLabelId.labelId
            every {
                mailboxReducer.newStateFrom(
                    expectedMailBoxState,
                    MailboxEvent.ItemClicked.ItemDetailsOpened(
                        unreadMailboxItemUiModel,
                        contextLabel = labelId
                    )
                )
            } returns createMailboxDataState(
                Effect.of(
                    OpenMailboxItemRequest(
                        MailboxItemId(unreadMailboxItem.id),
                        shouldOpenInComposer = false,
                        openedFromLocation = labelId
                    )
                )
            )

            mailboxViewModel.items.test {
                // When
                awaitItem()
                verify(exactly = 1) {
                    pagerFactory.create(userId, MailLabelTestData.archiveSystemLabel.id, false, Message, any())
                }

                mailboxViewModel.submit(MailboxViewAction.ItemClicked(unreadMailboxItemUiModel))

                // Then
                expectNoEvents()
                confirmVerified(pagerFactory)
            }
        }

    @Test
    fun `verify mapped paging data is cached`() = runTest {
        // Given
        every { mailboxReducer.newStateFrom(any(), any()) } returns createMailboxDataState()
        expectPagerMock(pagingDataFlow = flowOf(PagingData.from(listOf(unreadMailboxItem))))

        mailboxViewModel.items.test {
            // When
            awaitItem()
            // Then
            verify(exactly = 1) {
                pagerFactory.create(userId, MailLabelTestData.archiveSystemLabel.id, false, Message, any())
            }
        }

        mailboxViewModel.items.test {
            // When
            awaitItem()
            // Then
            confirmVerified(pagerFactory)
        }
    }

    @Test
    fun `when mark as read is triggered for conversation grouping then mark conversations as read is called`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val secondItem = unreadMailboxItemUiModel
            val initialState = createMailboxDataState()
            val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))
            val expectedState = MailboxStateSampleData.createSelectionMode(
                listOf(item.copy(isRead = true), secondItem.copy(isRead = true))
            )
            val labelId = initialLocationMailLabelId.labelId
            expectViewModeForCurrentLocation(ConversationGrouping)
            expectedSelectedLabelCountStateChange(initialState)
            returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
            returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
            returnExpectedStateForMarkAsRead(intermediateState, expectedState)
            expectMarkConversationsAsReadSucceeds(userId, labelId, listOf(item, secondItem))
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(intermediateState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
                }

                // When
                mailboxViewModel.submit(MailboxViewAction.MarkAsRead)

                // Then
                assertEquals(expectedState, awaitItem())
                coVerify(exactly = 1) {
                    markConversationsAsRead(
                        userId,
                        labelId,
                        listOf(ConversationId(item.id), ConversationId(secondItem.id))
                    )
                }
                coVerify { markMessagesAsRead wasNot Called }
            }
        }

    @Test
    fun `when mark as unread is triggered for conversation grouping then mark conversations as unread is called`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val secondItem = unreadMailboxItemUiModel
            val initialState = createMailboxDataState()
            val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))
            val expectedState = MailboxStateSampleData.createSelectionMode(
                listOf(item.copy(isRead = false), secondItem.copy(isRead = false))
            )
            val labelId = initialLocationMailLabelId.labelId
            expectViewModeForCurrentLocation(ConversationGrouping)
            expectedSelectedLabelCountStateChange(initialState)
            returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
            returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
            returnExpectedStateForMarkAsUnread(intermediateState, expectedState)
            expectMarkConversationsAsUnreadSucceeds(userId, labelId, listOf(item, secondItem))
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(intermediateState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
                }

                // When
                mailboxViewModel.submit(MailboxViewAction.MarkAsUnread)

                // Then
                assertEquals(expectedState, awaitItem())
                coVerify(exactly = 1) {
                    markConversationsAsUnread(
                        userId,
                        labelId,
                        listOf(ConversationId(item.id), ConversationId(secondItem.id))
                    )
                }
                coVerify { markMessagesAsUnread wasNot Called }
            }
        }

    @Test
    fun `when mark as read is triggered for no conversation grouping then mark messages as read use case is called`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val secondItem = unreadMailboxItemUiModel
            val initialState = createMailboxDataState()
            val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))
            val expectedState = MailboxStateSampleData.createSelectionMode(
                listOf(item.copy(isRead = true), secondItem.copy(isRead = true))
            )
            expectViewModeForCurrentLocation(NoConversationGrouping)
            expectedSelectedLabelCountStateChange(initialState)
            returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
            returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
            returnExpectedStateForMarkAsRead(intermediateState, expectedState)
            expectMarkMessagesAsReadSucceeds(userId, listOf(item, secondItem))
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(intermediateState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
                }

                // When
                mailboxViewModel.submit(MailboxViewAction.MarkAsRead)

                // Then
                assertEquals(expectedState, awaitItem())
                coVerify(exactly = 1) {
                    markMessagesAsRead(userId, listOf(MessageId(item.id), MessageId(secondItem.id)))
                }
                coVerify { markConversationsAsRead wasNot Called }
            }
        }

    @Test
    fun `when mark as unread is triggered for no conversation grouping then mark messages as unread is called`() =
        runTest {
            // Given
            val item = readMailboxItemUiModel
            val secondItem = unreadMailboxItemUiModel
            val initialState = createMailboxDataState()
            val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))
            val expectedState = MailboxStateSampleData.createSelectionMode(
                listOf(item.copy(isRead = true), secondItem.copy(isRead = true))
            )
            expectViewModeForCurrentLocation(NoConversationGrouping)
            expectedSelectedLabelCountStateChange(initialState)
            returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
            returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
            returnExpectedStateForMarkAsUnread(intermediateState, expectedState)
            expectMarkMessagesAsUnreadSucceeds(userId, listOf(item, secondItem))
            expectPagerMock()

            mailboxViewModel.state.test {
                // Given
                awaitItem() // First emission for selected user

                // When
                mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

                // Then
                assertEquals(intermediateState, awaitItem())
                verify(exactly = 1) {
                    mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
                }

                // When
                mailboxViewModel.submit(MailboxViewAction.MarkAsUnread)

                // Then
                assertEquals(expectedState, awaitItem())
                coVerify(exactly = 1) {
                    markMessagesAsUnread(userId, listOf(MessageId(item.id), MessageId(secondItem.id)))
                }
                coVerify { markConversationsAsUnread wasNot Called }
            }
        }

    @Test
    fun `when trash is triggered for conversation grouping then move conversations is called`() = runTest {
        // Given
        val item = readMailboxItemUiModel
        val secondItem = unreadMailboxItemUiModel
        val initialState = createMailboxDataState()
        val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))
        expectViewModeForCurrentLocation(ConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateForTrash(intermediateState, initialState, 2)
        expectMoveConversationsSucceeds(userId, listOf(item, secondItem), SystemLabelId.Trash)
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

            // Then
            assertEquals(intermediateState, awaitItem())
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
            }

            // When
            mailboxViewModel.submit(MailboxViewAction.Trash)

            // Then
            assertEquals(initialState, awaitItem())
            coVerify(exactly = 1) {
                moveConversations(
                    userId,
                    listOf(ConversationId(item.id), ConversationId(secondItem.id)),
                    SystemLabelId.Trash
                )
            }
            coVerify { moveMessages wasNot Called }
        }
    }

    @Test
    fun `when trash is triggered for no conversation grouping then move messages is called `() = runTest {
        val item = readMailboxItemUiModel
        val secondItem = unreadMailboxItemUiModel
        val initialState = createMailboxDataState()
        val intermediateState = MailboxStateSampleData.createSelectionMode(listOf(item, secondItem))
        expectViewModeForCurrentLocation(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateForTrash(intermediateState, initialState, 2)
        expectMoveMessagesSucceeds(userId, listOf(item, secondItem), SystemLabelId.Trash)
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

            // Then
            assertEquals(intermediateState, awaitItem())
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
            }

            // When
            mailboxViewModel.submit(MailboxViewAction.Trash)

            // Then
            assertEquals(initialState, awaitItem())
            coVerify(exactly = 1) {
                moveMessages(userId, listOf(MessageId(item.id), MessageId(secondItem.id)), SystemLabelId.Trash)
            }
            coVerify { moveConversations wasNot Called }
        }
    }

    @Test
    fun `when delete is triggered for conversation grouping then delete conversations is called `() = runTest {
        // Given
        val item = readMailboxItemUiModel
        val secondItem = unreadMailboxItemUiModel
        val initialState = createMailboxDataState()
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewModeForCurrentLocation(ConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateForDeleteConfirmed(intermediateState, initialState, ConversationGrouping, 2)
        expectDeleteConversationsSucceeds(userId, listOf(item, secondItem))
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

            // Then
            assertEquals(intermediateState, awaitItem())
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
            }

            // When
            mailboxViewModel.submit(MailboxViewAction.DeleteConfirmed)

            // Then
            assertEquals(initialState, awaitItem())
            coVerify(exactly = 1) {
                deleteConversations(
                    userId,
                    listOf(ConversationId(item.id), ConversationId(secondItem.id))
                )
            }
            coVerify { deleteMessages wasNot Called }
        }
    }

    @Test
    fun `when delete is triggered for no conversation grouping then delete messages is called`() = runTest {
        val item = readMailboxItemUiModel.copy(type = Message)
        val secondItem = unreadMailboxItemUiModel.copy(type = Message)
        val initialState = createMailboxDataState()
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateForDeleteConfirmed(intermediateState, initialState, NoConversationGrouping, 2)
        expectDeleteMessagesSucceeds(userId, listOf(item, secondItem), SystemLabelId.Trash.labelId)
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))

            // Then
            assertEquals(intermediateState, awaitItem())
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
            }

            // When
            mailboxViewModel.submit(MailboxViewAction.DeleteConfirmed)

            // Then
            assertEquals(initialState, awaitItem())
            coVerify(exactly = 1) {
                deleteMessages(
                    userId,
                    listOf(MessageId(item.id), MessageId(secondItem.id)),
                    SystemLabelId.Trash.labelId
                )
            }
            coVerify { deleteConversations wasNot Called }
        }
    }

    @Test
    fun `when bottom sheet dismissal is triggered then the label as bottom sheet is dismissed `() = runTest {
        // Given
        val item = readMailboxItemUiModel.copy(id = MessageIdSample.Invoice.id)
        val initialState = createMailboxDataState()
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewModeForCurrentLocation(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        expectedLabelAsBottomSheetDismissed(initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))
            assertEquals(intermediateState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.DismissBottomSheet)

            // Then
            assertEquals(initialState, awaitItem())
        }
    }

    @Test
    fun `when star action is triggered for no-conversation grouping then star messages is called`() = runTest {
        // Given
        val item = readMailboxItemUiModel.copy(id = MessageIdSample.Invoice.id, isStarred = true)
        val secondItem = unreadMailboxItemUiModel.copy(id = MessageIdSample.AlphaAppQAReport.id, isStarred = false)
        val selectedItemsList = listOf(item, secondItem)

        val initialState = createMailboxDataState()
        val expectedActions = listOf(Action.Unstar, Action.Archive, Action.Spam)
        val expectedActionItems = expectedActions.map { ActionUiModelSample.build(it) }
        val expectedBottomSheetContent = MailboxMoreActionsBottomSheetState.Data(
            hiddenActionUiModels = expectedActionItems.toImmutableList(),
            visibleActionUiModels = emptyList<ActionUiModel>().toImmutableList(),
            customizeToolbarActionUiModel = ActionUiModelSample.CustomizeToolbar,
            selectedCount = 1
        )
        val bottomSheetShownState =
            createMailboxStateWithMoreActionBottomSheet(selectedItemsList, expectedBottomSheetContent)
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewModeForCurrentLocation(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectBottomSheetActionsSucceeds(
            expectedActions,
            selectedMailLabelId.flow.value.labelId,
            selectedItemsList.map { MailboxItemId(it.id) },
            NoConversationGrouping
        )
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        expectedMoreActionBottomSheetRequestedStateChange(
            expectedActionItems, bottomSheetShownState, selectedItemsList.size
        )
        expectedStarMessagesSucceeds(userId, selectedItemsList)
        returnExpectedStateWhenStarringSucceeds(intermediateState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When + Then
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))
            assertEquals(intermediateState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.RequestMoreActionsBottomSheet)
            assertEquals(bottomSheetShownState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.Star)
            assertEquals(intermediateState, awaitItem())
        }
        coVerify(exactly = 1) { starMessages(userId, selectedItemsList.map { MessageId(it.id) }) }
        verify { starConversations wasNot Called }
    }

    @Test
    fun `when unstar action is triggered for no-conversation grouping then unstar messages is called`() = runTest {
        // Given
        val item = readMailboxItemUiModel.copy(id = MessageIdSample.Invoice.id, isStarred = true)
        val secondItem = unreadMailboxItemUiModel.copy(id = MessageIdSample.AlphaAppQAReport.id, isStarred = true)
        val selectedItemsList = listOf(item, secondItem)

        val initialState = createMailboxDataState()
        val expectedActions = listOf(Action.Unstar, Action.Archive, Action.Spam)
        val expectedActionItems = expectedActions.map { ActionUiModelSample.build(it) }
        val expectedBottomSheetContent = MailboxMoreActionsBottomSheetState.Data(
            hiddenActionUiModels = expectedActionItems.toImmutableList(),
            visibleActionUiModels = listOf<ActionUiModel>().toImmutableList(),
            customizeToolbarActionUiModel = ActionUiModelSample.CustomizeToolbar,
            selectedCount = selectedItemsList.size
        )
        val bottomSheetShownState =
            createMailboxStateWithMoreActionBottomSheet(selectedItemsList, expectedBottomSheetContent)
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewModeForCurrentLocation(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectBottomSheetActionsSucceeds(
            expectedActions,
            selectedMailLabelId.flow.value.labelId,
            selectedItemsList.map { MailboxItemId(it.id) },
            NoConversationGrouping
        )
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        expectedMoreActionBottomSheetRequestedStateChange(
            expectedActionItems, bottomSheetShownState, selectedItemsList.size
        )
        expectedUnStarMessagesSucceeds(userId, selectedItemsList)
        returnExpectedStateWhenUnStarringSucceeds(intermediateState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When + Then
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))
            assertEquals(intermediateState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.RequestMoreActionsBottomSheet)
            assertEquals(bottomSheetShownState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.UnStar)
            assertEquals(intermediateState, awaitItem())
        }
        coVerify(exactly = 1) { unStarMessages(userId, selectedItemsList.map { MessageId(it.id) }) }
        verify { unStarConversations wasNot Called }
    }

    @Test
    fun `when move to archive is triggered for no-conversation grouping then move messages is called`() = runTest {
        // Given
        val item = readMailboxItemUiModel.copy(id = MessageIdSample.Invoice.id, isStarred = true)
        val secondItem = unreadMailboxItemUiModel.copy(id = MessageIdSample.AlphaAppQAReport.id, isStarred = true)
        val selectedItemsList = listOf(item, secondItem)
        val initialState = createMailboxDataState()
        val expectedActions = listOf(Action.Unstar, Action.Archive, Action.Spam)
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewModeForCurrentLocation(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectBottomSheetActionsSucceeds(
            expectedActions,
            selectedMailLabelId.flow.value.labelId,
            selectedItemsList.map { MailboxItemId(it.id) },
            NoConversationGrouping
        )
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        expectMoveMessagesSucceeds(userId, selectedItemsList, SystemLabelId.Archive)
        expectedReducerResult(MailboxEvent.MoveToConfirmed, initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When + Then
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))
            assertEquals(intermediateState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.MoveToArchive)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { moveMessages(userId, selectedItemsList.map { MessageId(it.id) }, SystemLabelId.Archive) }
        coVerify { moveConversations wasNot Called }
    }

    @Test
    fun `when move to archive is triggered for conversation grouping then move conversation is called`() = runTest {
        // Given
        val item = readMailboxItemUiModel.copy(id = MessageIdSample.Invoice.id, isStarred = true)
        val secondItem = unreadMailboxItemUiModel.copy(id = MessageIdSample.AlphaAppQAReport.id, isStarred = true)
        val selectedItemsList = listOf(item, secondItem)
        val initialState = createMailboxDataState()
        val expectedActions = listOf(Action.Unstar, Action.Archive, Action.Spam)
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewMode(ConversationGrouping)
        expectViewModeForCurrentLocation(ConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        expectBottomSheetActionsSucceeds(
            expectedActions,
            selectedMailLabelId.flow.value.labelId,
            selectedItemsList.map { MailboxItemId(it.id) },
            ConversationGrouping
        )
        expectMoveConversationsSucceeds(userId, selectedItemsList, SystemLabelId.Archive)
        expectedReducerResult(MailboxEvent.MoveToConfirmed, initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When + Then
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))
            assertEquals(intermediateState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.MoveToArchive)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify {
            moveConversations(userId, selectedItemsList.map { ConversationId(it.id) }, SystemLabelId.Archive)
        }
        coVerify { moveMessages wasNot Called }
    }

    @Test
    fun `when move to spam is triggered for no-conversation grouping then move messages is called`() = runTest {
        // Given
        val item = readMailboxItemUiModel.copy(id = MessageIdSample.Invoice.id, isStarred = true)
        val secondItem = unreadMailboxItemUiModel.copy(id = MessageIdSample.AlphaAppQAReport.id, isStarred = true)
        val selectedItemsList = listOf(item, secondItem)

        val initialState = createMailboxDataState()
        val expectedActions = listOf(Action.Unstar, Action.Archive, Action.Spam)
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewModeForCurrentLocation(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectBottomSheetActionsSucceeds(
            expectedActions,
            selectedMailLabelId.flow.value.labelId,
            selectedItemsList.map { MailboxItemId(it.id) },
            NoConversationGrouping
        )
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        expectMoveMessagesSucceeds(userId, selectedItemsList, SystemLabelId.Spam)
        expectedReducerResult(MailboxEvent.MoveToConfirmed, initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When + Then
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))
            assertEquals(intermediateState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.MoveToSpam)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { moveMessages(userId, selectedItemsList.map { MessageId(it.id) }, SystemLabelId.Spam) }
        coVerify { moveConversations wasNot Called }
    }

    @Test
    fun `when move to spam is triggered for conversation grouping then move conversation is called`() = runTest {
        // Given
        val item = readMailboxItemUiModel.copy(id = MessageIdSample.Invoice.id, isStarred = true)
        val secondItem = unreadMailboxItemUiModel.copy(id = MessageIdSample.AlphaAppQAReport.id, isStarred = true)
        val selectedItemsList = listOf(item, secondItem)

        val initialState = createMailboxDataState()
        val expectedActions = listOf(Action.Unstar, Action.Archive, Action.Spam)
        val intermediateState = MailboxStateSampleData.createSelectionMode(
            listOf(item, secondItem),
            currentMailLabel = MailLabelTestData.trashSystemLabel
        )
        expectViewMode(ConversationGrouping)
        expectViewModeForCurrentLocation(ConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectBottomSheetActionsSucceeds(
            expectedActions,
            selectedMailLabelId.flow.value.labelId,
            selectedItemsList.map { MailboxItemId(it.id) },
            ConversationGrouping
        )
        returnExpectedStateForBottomBarEvent(expectedState = intermediateState)
        returnExpectedStateWhenEnterSelectionMode(initialState, item, intermediateState)
        expectMoveConversationsSucceeds(userId, selectedItemsList, SystemLabelId.Spam)
        expectedReducerResult(MailboxEvent.MoveToConfirmed, initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When + Then
            mailboxViewModel.submit(MailboxViewAction.OnItemAvatarClicked(item))
            assertEquals(intermediateState, awaitItem())
            mailboxViewModel.submit(MailboxViewAction.MoveToSpam)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify {
            moveConversations(userId, selectedItemsList.map { ConversationId(it.id) }, SystemLabelId.Spam)
        }
        coVerify { moveMessages wasNot Called }
    }

    @Test
    fun `verify dismiss delete dialog calls reducer`() = runTest {
        // Given
        val initialState = createMailboxDataState()
        expectViewMode(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateForDeleteDismissed(initialState, initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.DeleteDialogDismissed)

            // Then
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(initialState, MailboxViewAction.DeleteDialogDismissed)
            }
        }
    }

    @Test
    fun `verify dismiss bottom sheet calls reducer`() = runTest {
        // Given
        val initialState = createMailboxDataState()
        expectViewMode(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        returnExpectedStateForDeleteDismissed(initialState, initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            // Given
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.DeleteDialogDismissed)

            // Then
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(initialState, MailboxViewAction.DeleteDialogDismissed)
            }
        }
    }

    @Test
    fun `mailbox items are not requested when a user account is removed`() = runTest {
        // Given
        val currentLocationFlow = MutableStateFlow<MailLabelId>(initialLocationMailLabelId)
        val initialMailboxState = createMailboxDataState()
        val expectedState = createMailboxDataState()
        val primaryUserFlow = MutableStateFlow<UserId?>(userId)
        coEvery {
            observeCurrentViewMode(userId = userId, initialLocationMailLabelId.labelId)
        } returns flowOf(ConversationGrouping)
        every { observePrimaryUserId.invoke() } returns primaryUserFlow
        every { selectedMailLabelId.flow } returns currentLocationFlow
        val pagingData = PagingData.from(listOf(unreadMailboxItem))
        expectPagerMock(
            itemType = Conversation,
            pagingDataFlow = flowOf(pagingData)
        )
        every { mailboxReducer.newStateFrom(any(), any()) } returns initialMailboxState
        returnExpectedStateForBottomBarEvent(expectedState = expectedState)

        mailboxViewModel.items.test {
            // Then
            awaitItem()
            verify { pagerFactory.create(userId, initialLocationMailLabelId, false, Conversation, any()) }

            // When
            primaryUserFlow.emit(null)

            // Then
            verify(exactly = 0) {
                pagerFactory.create(userId, initialLocationMailLabelId, false, Message, any())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe read is called for no conversation grouping and item is read then mark as unread is called`() {
        // Given
        val expectedItemId = "itemId"
        expectViewMode(NoConversationGrouping)
        expectMarkMessagesAsUnreadSucceeds(userId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        val expectedViewAction = MailboxViewAction.SwipeReadAction(expectedItemId, true)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerifyOrder {
            markMessagesAsUnread(userId, listOf(MessageId(expectedItemId)))
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            markMessagesAsRead wasNot Called
            markConversationsAsRead wasNot Called
            markConversationsAsUnread wasNot Called
        }
    }

    @Test
    fun `when swipe read is called for conversation grouping and item is read then mark as unread is called`() {
        // Given
        val expectedItemId = "itemId"
        val labelId = initialLocationMailLabelId.labelId
        expectViewModeForCurrentLocation(ConversationGrouping)
        expectMarkConversationsAsUnreadSucceeds(userId, labelId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        val expectedViewAction = MailboxViewAction.SwipeReadAction(expectedItemId, true)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerifyOrder {
            markConversationsAsUnread(
                userId,
                labelId,
                listOf(ConversationId(expectedItemId))
            )
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            markMessagesAsRead wasNot Called
            markMessagesAsUnread wasNot Called
            markConversationsAsRead wasNot Called
        }
    }

    @Test
    fun `when swipe read is called for no conversation grouping and item is unread then mark as read is called`() {
        // Given
        val expectedItemId = "itemId"
        expectViewMode(NoConversationGrouping)
        expectMarkMessagesAsReadSucceeds(userId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        val expectedViewAction = MailboxViewAction.SwipeReadAction(expectedItemId, false)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerifyOrder {
            markMessagesAsRead(userId, listOf(MessageId(expectedItemId)))
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            markMessagesAsUnread wasNot Called
            markConversationsAsRead wasNot Called
            markConversationsAsUnread wasNot Called
        }
    }

    @Test
    fun `when swipe read is called for conversation grouping and item is unread then mark as read is called`() {
        // Given
        val expectedItemId = "itemId"
        val labelId = initialLocationMailLabelId.labelId
        expectViewModeForCurrentLocation(ConversationGrouping)
        expectMarkConversationsAsReadSucceeds(userId, labelId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        val expectedViewAction = MailboxViewAction.SwipeReadAction(expectedItemId, false)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerify {
            markConversationsAsRead(
                userId,
                labelId,
                listOf(ConversationId(expectedItemId))
            )
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            markMessagesAsRead wasNot Called
            markMessagesAsUnread wasNot Called
            markConversationsAsUnread wasNot Called
        }
    }

    @Test
    fun `when swipe archive is called for no conversation grouping then move is called`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.inboxSystemLabel.id)
        val itemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeArchiveAction(itemId)
        val currentLocationFlow = MutableStateFlow<MailLabelId>(MailLabelTestData.inboxSystemLabel.id)

        every { selectedMailLabelId.flow } returns currentLocationFlow
        expectedSelectedLabelCountStateChange(initialState)
        expectMoveMessagesSucceeds(userId, listOf(buildMailboxUiModelItem(id = itemId)), SystemLabelId.Archive)
        expectPagerMock()


        mailboxViewModel.state.test {
            advanceUntilIdle()

            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveMessages(userId, listOf(MessageId(itemId)), SystemLabelId.Archive) }
            coVerify { moveConversations wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe archive is called for conversation grouping then move is called`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.inboxSystemLabel.id)
        val expectedItemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeArchiveAction(expectedItemId)
        val currentLocationFlow = MutableStateFlow<MailLabelId>(MailLabelTestData.inboxSystemLabel.id)

        every { selectedMailLabelId.flow } returns currentLocationFlow
        expectViewModeForCurrentLocation(ConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectMoveConversationsSucceeds(
            userId,
            listOf(buildMailboxUiModelItem(id = expectedItemId)),
            SystemLabelId.Archive
        )
        expectPagerMock()

        mailboxViewModel.state.test {
            advanceUntilIdle()

            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveConversations(userId, listOf(ConversationId(expectedItemId)), SystemLabelId.Archive) }
            coVerify { moveMessages wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe archive is called when current label is archive, no action is performed`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.archiveSystemLabel.id)
        val expectedItemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeArchiveAction(expectedItemId)

        expectedSelectedLabelCountStateChange(initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveConversations wasNot Called }
            coVerify { moveMessages wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe spam is called for no conversation grouping then move is called`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.inboxSystemLabel.id)
        val itemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeSpamAction(itemId)

        expectedSelectedLabelCountStateChange(initialState)
        expectMoveMessagesSucceeds(userId, listOf(buildMailboxUiModelItem(id = itemId)), SystemLabelId.Spam)
        expectPagerMock()

        mailboxViewModel.state.test {
            advanceUntilIdle()

            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveMessages(userId, listOf(MessageId(itemId)), SystemLabelId.Spam) }
            coVerify { moveConversations wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe spam is called for conversation grouping then move is called`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.inboxSystemLabel.id)
        val expectedItemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeSpamAction(expectedItemId)

        expectViewModeForCurrentLocation(ConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectMoveConversationsSucceeds(
            userId,
            listOf(buildMailboxUiModelItem(id = expectedItemId)),
            SystemLabelId.Spam
        )
        expectPagerMock()

        mailboxViewModel.state.test {
            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveConversations(userId, listOf(ConversationId(expectedItemId)), SystemLabelId.Spam) }
            coVerify { moveMessages wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe spam is called when current label is spam, no action is performed`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.spamSystemLabel.id)
        val expectedItemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeSpamAction(expectedItemId)
        val currentLocationFlow = MutableStateFlow<MailLabelId>(MailLabelTestData.spamSystemLabel.id)

        every { selectedMailLabelId.flow } returns currentLocationFlow
        expectedSelectedLabelCountStateChange(initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveConversations wasNot Called }
            coVerify { moveMessages wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe trash is called for no conversation grouping then move is called`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.inboxSystemLabel.id)
        val itemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeTrashAction(itemId)

        expectedSelectedLabelCountStateChange(initialState)
        expectMoveMessagesSucceeds(userId, listOf(buildMailboxUiModelItem(id = itemId)), SystemLabelId.Trash)
        expectPagerMock()

        mailboxViewModel.state.test {
            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveMessages(userId, listOf(MessageId(itemId)), SystemLabelId.Trash) }
            coVerify { moveConversations wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe trash is called for conversation grouping then move is called`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.inboxSystemLabel.id)
        val expectedItemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeTrashAction(expectedItemId)

        expectViewModeForCurrentLocation(ConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectMoveConversationsSucceeds(
            userId,
            listOf(buildMailboxUiModelItem(id = expectedItemId)),
            SystemLabelId.Trash
        )
        expectPagerMock()

        mailboxViewModel.state.test {
            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveConversations(userId, listOf(ConversationId(expectedItemId)), SystemLabelId.Trash) }
            coVerify { moveMessages wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe trash is called when current label is trash, no action is performed`() = runTest {
        // Given
        val initialState = createMailboxDataState(selectedMailLabelId = MailLabelTestData.trashSystemLabel.id)
        val expectedItemId = "itemId"
        val expectedViewAction = MailboxViewAction.SwipeTrashAction(expectedItemId)
        val currentLocationFlow = MutableStateFlow<MailLabelId>(MailLabelTestData.trashSystemLabel.id)

        every { selectedMailLabelId.flow } returns currentLocationFlow
        expectedSelectedLabelCountStateChange(initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            // When
            mailboxViewModel.submit(expectedViewAction)

            // Then
            coVerify { moveConversations wasNot Called }
            coVerify { moveMessages wasNot Called }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when swipe star is called for no conversation grouping and item is starred then unstar is called`() {
        // Given
        val expectedItemId = "itemId"
        expectViewMode(NoConversationGrouping)
        expectedUnStarMessagesSucceeds(userId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        val expectedViewAction = MailboxViewAction.StarAction(expectedItemId, true)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerifyOrder {
            unStarMessages(userId, listOf(MessageId(expectedItemId)))
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            starMessages wasNot Called
            starConversations wasNot Called
            unStarConversations wasNot Called
        }
    }

    @Test
    fun `when swipe star is called for conversation grouping and item is starred then unstar is called`() {
        // Given
        val expectedItemId = "itemId"
        expectViewMode(ConversationGrouping)
        expectedUnStarConversationsSucceeds(userId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        expectViewModeForCurrentLocation(ConversationGrouping)
        val expectedViewAction = MailboxViewAction.StarAction(expectedItemId, true)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerifyOrder {
            unStarConversations(userId, listOf(ConversationId(expectedItemId)))
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            starConversations wasNot Called
            starMessages wasNot Called
            unStarMessages wasNot Called
        }
    }

    @Test
    fun `when swipe star is called for no conversation grouping and item is not starred then star is called`() {
        // Given
        val expectedItemId = "itemId"
        expectViewMode(NoConversationGrouping)
        expectedStarMessagesSucceeds(userId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        val expectedViewAction = MailboxViewAction.StarAction(expectedItemId, false)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerifyOrder {
            starMessages(userId, listOf(MessageId(expectedItemId)))
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            unStarMessages wasNot Called
            unStarConversations wasNot Called
            starConversations wasNot Called
        }
    }

    @Test
    fun `when swipe star is called for conversation grouping and item is not starred then star is called`() {
        // Given
        val expectedItemId = "itemId"
        expectViewMode(ConversationGrouping)
        expectedStarConversationsSucceeds(userId, listOf(buildMailboxUiModelItem(id = expectedItemId)))
        expectViewModeForCurrentLocation(ConversationGrouping)
        val expectedViewAction = MailboxViewAction.StarAction(expectedItemId, false)

        // When
        mailboxViewModel.submit(expectedViewAction)

        // Then
        coVerifyOrder {
            starConversations(userId, listOf(ConversationId(expectedItemId)))
            mailboxReducer.newStateFrom(any(), expectedViewAction)
        }
        coVerify {
            unStarConversations wasNot Called
            unStarMessages wasNot Called
            starMessages wasNot Called
        }

    }

    private fun createMailboxDataState(
        openEffect: Effect<OpenMailboxItemRequest> = Effect.empty(),
        scrollToMailboxTop: Effect<MailLabelId> = Effect.empty(),
        unreadFilterState: Boolean = false,
        selectedMailLabelId: MailLabelId.System = initialLocationMailLabelId,
        selectedSystemMailLabelId: SystemLabelId = SystemLabelId.Inbox
    ): MailboxState {
        return MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabel.System(
                    selectedMailLabelId,
                    selectedSystemMailLabelId,
                    0
                ),
                openItemEffect = openEffect,
                scrollToMailboxTop = scrollToMailboxTop,
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            ),
            unreadFilterState = UnreadFilterState.Data(
                numUnread = UnreadCountersTestData.labelToCounterMap[initialLocationMailLabelId.labelId]!!,
                isFilterEnabled = unreadFilterState
            )
        )
    }

    @Test
    fun `when enter search mode action is submitted, search mode is updated and emitted`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Inbox.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.allMailSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                refreshErrorEffect = Effect.of(Unit),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NewSearch,
                shouldShowFab = false,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        every {
            mailboxReducer.newStateFrom(
                MailboxStateSampleData.Loading,
                MailboxViewAction.EnterSearchMode
            )
        } returns expectedState
        expectPagerMock()

        // When
        mailboxViewModel.submit(MailboxViewAction.EnterSearchMode)
        mailboxViewModel.state.test {

            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when exit search mode action is submitted, search mode is updated and emitted`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Inbox.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.inboxSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                refreshErrorEffect = Effect.of(Unit),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.NotSearching,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        every {
            mailboxReducer.newStateFrom(
                MailboxStateSampleData.Loading,
                MailboxViewAction.ExitSearchMode
            )
        } returns expectedState
        coEvery { deleteSearchResults.invoke(any(), any()) } just runs
        expectPagerMock()

        // When
        mailboxViewModel.submit(MailboxViewAction.ExitSearchMode)
        mailboxViewModel.state.test {

            // Then
            assertEquals(expectedState, awaitItem())
            coVerify { deleteSearchResults.invoke(any(), any()) }
        }
    }

    @Test
    fun `when search query action is submitted, search mode is updated and emitted`() = runTest {
        // Given
        val queryText = "query"
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.inboxSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                refreshErrorEffect = Effect.of(Unit),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.SearchLoading,
                shouldShowFab = false,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        every {
            mailboxReducer.newStateFrom(
                MailboxStateSampleData.Loading,
                MailboxViewAction.SearchQuery(queryText)
            )
        } returns expectedState

        // When
        mailboxViewModel.submit(MailboxViewAction.SearchQuery(queryText))
        mailboxViewModel.state.test {

            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `when search result action is submitted, search mode is updated and emitted`() = runTest {
        // Given
        val expectedState = MailboxStateSampleData.Loading.copy(
            mailboxListState = MailboxListState.Data.ViewMode(
                currentMailLabel = MailLabelTestData.inboxSystemLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                refreshErrorEffect = Effect.of(Unit),
                refreshRequested = false,
                swipeActions = null,
                searchState = MailboxSearchStateSampleData.SearchData,
                shouldShowFab = true,
                avatarImagesUiModel = AvatarImagesUiModelTestData.SampleData1
            )
        )
        every {
            mailboxReducer.newStateFrom(
                MailboxStateSampleData.Loading,
                MailboxViewAction.SearchResult
            )
        } returns expectedState

        // When
        mailboxViewModel.submit(MailboxViewAction.SearchResult)
        mailboxViewModel.state.test {

            // Then
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `should emit upselling data event when showing the upselling bottom sheet was requested`() = runTest {
        // Given
        val initialState = createMailboxDataState()
        expectViewMode(NoConversationGrouping)
        expectedSelectedLabelCountStateChange(initialState)
        expectRequestUpsellingBottomSheet(initialState)
        expectUpsellingBottomSheetDataLoaded(initialState)
        expectPagerMock()

        mailboxViewModel.state.test {
            awaitItem() // First emission for selected user

            // When
            mailboxViewModel.submit(MailboxViewAction.RequestUpsellingBottomSheet)

            // Then
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(initialState, MailboxViewAction.RequestUpsellingBottomSheet)
            }
            verify(exactly = 1) {
                mailboxReducer.newStateFrom(
                    initialState,
                    MailboxEvent.MailboxBottomSheetEvent(UpsellingBottomSheetState.UpsellingBottomSheetEvent.Ready)
                )
            }
        }
    }

    @Test
    fun `navigate to inbox label will trigger selected mail label use case`() = runTest {
        // Given
        val expectedLabel = MailLabelTestData.inboxSystemLabel
        coEvery { findLocalSystemLabelId(userId, SystemLabelId.Inbox) } returns expectedLabel.id
        coJustRun { selectedMailLabelId.set(expectedLabel.id) }

        // When
        mailboxViewModel.submit(MailboxViewAction.NavigateToInboxLabel)

        // Then
        coVerify { selectedMailLabelId.set(expectedLabel.id) }
    }

    @Test
    fun `should invoke avatar image loading when requested`() = runTest {
        // Given
        val address = "user@example.com"
        val bimiSelector = "selector"
        val mailboxItem = mockk<MailboxItemUiModel> {
            every { avatar } returns AvatarUiModel.ParticipantAvatar(
                address = address,
                bimiSelector = bimiSelector,
                initial = "U",
                color = androidx.compose.ui.graphics.Color.Red,
                selected = false
            )
        }

        val mailboxAction = MailboxViewAction.OnAvatarImageLoadRequested(mailboxItem)

        // When
        mailboxViewModel.submit(mailboxAction)

        // Then
        verify {
            loadAvatarImage.invoke(
                address = address,
                bimiSelector = bimiSelector
            )
        }
    }

    @Test
    fun `should handle avatar image loading failure`() = runTest {
        // Given
        val address = "user@example.com"
        val bimiSelector = "selector"
        val mailboxItem = mockk<MailboxItemUiModel> {
            every { avatar } returns AvatarUiModel.ParticipantAvatar(
                address = address,
                bimiSelector = bimiSelector,
                initial = "U",
                color = androidx.compose.ui.graphics.Color.Red,
                selected = false
            )
        }

        val mailboxAction = MailboxViewAction.OnAvatarImageLoadFailed(mailboxItem)

        // When
        mailboxViewModel.submit(mailboxAction)

        // Then
        verify {
            handleAvatarImageLoadingFailure.invoke(
                address = address,
                bimiSelector = bimiSelector
            )
        }
    }

    @Test
    fun `should raise avatar image states updated event when a new state observed`() = runTest {
        // Given
        val avatarImageStates = AvatarImageStatesTestData.SampleData1
        val avatarImageStatesFlow = MutableStateFlow(avatarImageStates)

        every { observeAvatarImageStates() } returns avatarImageStatesFlow
        every { mailboxReducer.newStateFrom(any(), any()) } returns MailboxStateSampleData.Loading

        mailboxViewModel.state.test {
            skipItems(1)

            // When
            avatarImageStatesFlow.emit(avatarImageStates)

            // Then
            verify {
                mailboxReducer.newStateFrom(
                    any(),
                    MailboxEvent.AvatarImageStatesUpdated(avatarImageStates)
                )
            }
        }
    }

    @Test
    fun `should get attachment intent values and pass it to the reducer upon requesting an attachment`() = runTest {
        // Given
        val attachmentIdUiModel = AttachmentIdUiModel("attachment-id")
        val attachmentId = AttachmentId(attachmentIdUiModel.value)

        val attachmentIntentValues = OpenAttachmentIntentValues(mimeType = "mimeType", uri = mockk())
        coEvery { getAttachmentIntentValues(userId, attachmentId) } returns attachmentIntentValues.right()

        val mailboxAction = MailboxViewAction.RequestAttachment(attachmentIdUiModel)


        // When
        mailboxViewModel.submit(mailboxAction)

        // Then
        coVerify(exactly = 1) {
            getAttachmentIntentValues.invoke(userId, attachmentId)
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.AttachmentReadyEvent(attachmentIntentValues)
            )
        }
    }

    @Test
    fun `should get attachment intent values and emit an error when it fails`() = runTest {
        // Given
        val attachmentIdUiModel = AttachmentIdUiModel("attachment-id")
        val attachmentId = AttachmentId(attachmentIdUiModel.value)

        val mailboxAction = MailboxViewAction.RequestAttachment(attachmentIdUiModel)
        coEvery { getAttachmentIntentValues(userId, attachmentId) } returns DataError.Local.NoDataCached.left()
        // When
        mailboxViewModel.submit(mailboxAction)

        // Then
        coVerify(exactly = 1) {
            getAttachmentIntentValues.invoke(userId, attachmentId)
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.AttachmentErrorEvent
            )
        }
    }

    @Test
    fun `should emit intermediate state on intent values fetch when it takes too long (success)`() = runTest {
        // Given
        val attachmentIdUiModel = AttachmentIdUiModel("attachment-id")
        val attachmentId = AttachmentId(attachmentIdUiModel.value)

        val attachmentIntentValues = OpenAttachmentIntentValues(mimeType = "mimeType", uri = mockk())
        coEvery { getAttachmentIntentValues(userId, attachmentId) } coAnswers {
            delay(1500)
            attachmentIntentValues.right()
        }

        val mailboxAction = MailboxViewAction.RequestAttachment(attachmentIdUiModel)

        // When
        mailboxViewModel.submit(mailboxAction)

        advanceTimeBy(2000)

        // Then
        coVerify(exactly = 1) {
            getAttachmentIntentValues.invoke(userId, attachmentId)
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.AttachmentDownloadOngoingEvent
            )
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.AttachmentReadyEvent(attachmentIntentValues)
            )
        }
    }

    @Test
    fun `should emit intermediate state on intent values fetch when it takes too long (error)`() = runTest {
        // Given
        val attachmentIdUiModel = AttachmentIdUiModel("attachment-id")
        val attachmentId = AttachmentId(attachmentIdUiModel.value)

        coEvery { getAttachmentIntentValues(userId, attachmentId) } coAnswers {
            delay(1500)
            DataError.Local.NoDataCached.left()
        }

        val mailboxAction = MailboxViewAction.RequestAttachment(attachmentIdUiModel)

        // When
        mailboxViewModel.submit(mailboxAction)

        advanceTimeBy(2000)

        // Then
        coVerify(exactly = 1) {
            getAttachmentIntentValues.invoke(userId, attachmentId)
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.AttachmentDownloadOngoingEvent
            )
            mailboxReducer.newStateFrom(
                any(),
                MailboxEvent.AttachmentErrorEvent
            )
        }
    }

    private fun returnExpectedStateForBottomBarEvent(
        intermediateState: MailboxState? = null,
        expectedState: MailboxState
    ) {
        every {
            mailboxReducer.newStateFrom(
                intermediateState ?: any(),
                MailboxEvent.MessageBottomBarEvent(
                    BottomBarEvent.ActionsData(
                        listOf(ActionUiModelSample.Archive, ActionUiModelSample.Trash).toImmutableList()
                    )
                )
            )
        } returns expectedState
    }

    private fun returnExpectedStateForMarkAsUnread(intermediateState: MailboxState, expectedState: MailboxState) {
        every {
            mailboxReducer.newStateFrom(intermediateState, MailboxViewAction.MarkAsUnread)
        } returns expectedState
    }

    private fun returnExpectedStateWhenEnterSelectionMode(
        initialState: MailboxState,
        item: MailboxItemUiModel,
        intermediateState: MailboxState
    ) {
        every {
            mailboxReducer.newStateFrom(initialState, MailboxEvent.EnterSelectionMode(item))
        } returns intermediateState
    }

    private fun expectedSelectedLabelCountStateChange(initialState: MailboxState) {
        every {
            mailboxReducer.newStateFrom(any(), MailboxEvent.SelectedLabelCountChanged(5))
        } returns initialState
    }

    private fun expectedMoreActionBottomSheetRequestedStateChange(
        actionUiModels: List<ActionUiModel>,
        bottomSheetState: MailboxState,
        selectedCount: Int
    ) {
        every {
            mailboxReducer.newStateFrom(
                currentState = any(),
                operation = MailboxEvent.MailboxBottomSheetEvent(
                    MailboxMoreActionsBottomSheetState.MailboxMoreActionsBottomSheetEvent.ActionData(
                        actionUiModels.toImmutableList(),
                        listOf<ActionUiModel>().toImmutableList(),
                        ActionUiModelSample.CustomizeToolbar,
                        selectedCount
                    )
                )
            )
        } returns bottomSheetState
    }

    private fun returnExpectedStateWhenStarringSucceeds(expectedState: MailboxState) {
        every { mailboxReducer.newStateFrom(any(), MailboxViewAction.Star) } returns expectedState
    }

    private fun returnExpectedStateWhenUnStarringSucceeds(expectedState: MailboxState) {
        every { mailboxReducer.newStateFrom(any(), MailboxViewAction.UnStar) } returns expectedState
    }

    private fun expectedReducerResult(operation: MailboxOperation, expectedState: MailboxState) {
        every { mailboxReducer.newStateFrom(any(), operation) } returns expectedState
    }

    private fun expectedLabelAsBottomSheetDismissed(expectedState: MailboxState) {
        every { mailboxReducer.newStateFrom(any(), MailboxViewAction.DismissBottomSheet) } returns expectedState
    }

    private fun expectViewMode(viewMode: ViewMode) {
        every { observeCurrentViewMode(any()) } returns flowOf(viewMode)
    }

    private fun expectViewModeForCurrentLocation(viewMode: ViewMode) {
        every { observeCurrentViewMode(any(), any()) } returns flowOf(viewMode)
    }

    private fun expectBottomSheetActionsSucceeds(
        expectedActions: List<Action>,
        labelId: LabelId,
        items: List<MailboxItemId>,
        viewMode: ViewMode
    ) {
        println("$userId, $labelId, $items $viewMode")
        val actions = AllBottomBarActions(expectedActions, emptyList())
        coEvery { getBottomSheetActions(userId, labelId, items, viewMode) } returns actions.right()
    }

    private fun returnExpectedStateForMarkAsRead(intermediateState: MailboxState, expectedState: MailboxState) {
        every { mailboxReducer.newStateFrom(intermediateState, MailboxViewAction.MarkAsRead) } returns expectedState
    }

    private fun returnExpectedStateForTrash(
        intermediateState: MailboxState,
        expectedState: MailboxState,
        expectedItemCount: Int
    ) {
        every {
            mailboxReducer.newStateFrom(intermediateState, MailboxEvent.Trash(expectedItemCount))
        } returns expectedState
    }

    private fun returnExpectedStateForDeleteConfirmed(
        intermediateState: MailboxState,
        expectedState: MailboxState,
        viewMode: ViewMode,
        expectedItemCount: Int
    ) {
        every {
            mailboxReducer.newStateFrom(intermediateState, MailboxEvent.DeleteConfirmed(viewMode, expectedItemCount))
        } returns expectedState
    }

    private fun returnExpectedStateForDeleteDismissed(intermediateState: MailboxState, expectedState: MailboxState) {
        every {
            mailboxReducer.newStateFrom(intermediateState, MailboxViewAction.DeleteDialogDismissed)
        } returns expectedState
    }

    private fun expectMarkConversationsAsReadSucceeds(
        userId: UserId,
        labelId: LabelId,
        items: List<MailboxItemUiModel>
    ) {
        coEvery {
            markConversationsAsRead(userId, labelId, items.map { ConversationId(it.id) })
        } returns Unit.right()
    }

    private fun expectMarkConversationsAsUnreadSucceeds(
        userId: UserId,
        labelId: LabelId,
        items: List<MailboxItemUiModel>
    ) {
        coEvery {
            markConversationsAsUnread(userId, labelId, items.map { ConversationId(it.id) })
        } returns Unit.right()
    }

    private fun expectMarkMessagesAsReadSucceeds(userId: UserId, items: List<MailboxItemUiModel>) {
        coEvery {
            markMessagesAsRead(userId, items.map { MessageId(it.id) })
        } returns Unit.right()
    }

    private fun expectMarkMessagesAsUnreadSucceeds(userId: UserId, items: List<MailboxItemUiModel>) {
        coEvery {
            markMessagesAsUnread(userId, items.map { MessageId(it.id) })
        } returns Unit.right()
    }

    private fun expectMoveConversationsSucceeds(
        userId: UserId,
        items: List<MailboxItemUiModel>,
        systemLabelId: SystemLabelId
    ) {
        coEvery { moveConversations(userId, items.map { ConversationId(it.id) }, systemLabelId) } returns Unit.right()
    }

    private fun expectMoveMessagesSucceeds(
        userId: UserId,
        items: List<MailboxItemUiModel>,
        systemLabelId: SystemLabelId
    ) {
        coEvery { moveMessages(userId, items.map { MessageId(it.id) }, systemLabelId) } returns Unit.right()
    }

    private fun expectDeleteConversationsSucceeds(userId: UserId, items: List<MailboxItemUiModel>) {
        coEvery { deleteConversations(userId, items.map { ConversationId(it.id) }) } returns Unit.right()
    }

    private fun expectDeleteMessagesSucceeds(
        userId: UserId,
        items: List<MailboxItemUiModel>,
        labelId: LabelId
    ) {
        coEvery { deleteMessages(userId, items.map { MessageId(it.id) }, labelId) } returns Unit.right()
    }

    private fun expectedStarMessagesSucceeds(userId: UserId, items: List<MailboxItemUiModel>) {
        coEvery {
            starMessages(userId, items.map { MessageId(it.id) })
        } returns
            Unit.right()
    }

    private fun expectedUnStarMessagesSucceeds(userId: UserId, items: List<MailboxItemUiModel>) {
        coEvery {
            unStarMessages(userId, items.map { MessageId(it.id) })
        } returns
            Unit.right()
    }

    private fun expectedStarConversationsSucceeds(userId: UserId, items: List<MailboxItemUiModel>) {
        coEvery {
            starConversations(userId, items.map { ConversationId(it.id) })
        } returns
            emptyList<ch.protonmail.android.mailconversation.domain.entity.Conversation>().right()
    }

    private fun expectedUnStarConversationsSucceeds(userId: UserId, items: List<MailboxItemUiModel>) {
        coEvery {
            unStarConversations(userId, items.map { ConversationId(it.id) })
        } returns
            emptyList<ch.protonmail.android.mailconversation.domain.entity.Conversation>().right()
    }

    private fun expectRequestUpsellingBottomSheet(initialState: MailboxState) {
        every {
            mailboxReducer.newStateFrom(initialState, MailboxViewAction.RequestUpsellingBottomSheet)
        } returns initialState
    }

    private fun expectUpsellingBottomSheetDataLoaded(initialState: MailboxState) {
        every {
            mailboxReducer.newStateFrom(
                initialState,
                MailboxEvent.MailboxBottomSheetEvent(UpsellingBottomSheetState.UpsellingBottomSheetEvent.Ready)
            )
        } returns initialState
    }

    private fun createMailboxStateWithMoreActionBottomSheet(
        selectedMailboxItems: List<MailboxItemUiModel>,
        expectedBottomSheetContent: MailboxMoreActionsBottomSheetState
    ) = MailboxStateSampleData.createSelectionMode(
        selectedMailboxItemUiModels = selectedMailboxItems,
        currentMailLabel = MailLabelTestData.trashSystemLabel,
        bottomSheetState = BottomSheetState(expectedBottomSheetContent)
    )

    private fun expectPagerMock(
        selectedLabelId: MailLabelId? = null,
        filterUnread: Boolean? = null,
        itemType: MailboxItemType? = null,
        searchQuery: String? = null,
        pagingDataFlow: Flow<PagingData<MailboxItem>> = flowOf()
    ) {

        every {
            pagerFactory.create(
                userId,
                selectedLabelId ?: any(),
                filterUnread ?: any(),
                itemType ?: any(),
                searchQuery ?: any()
            )
        } returns mockk mockPager@{ every { this@mockPager.flow } returns pagingDataFlow }
    }
}
