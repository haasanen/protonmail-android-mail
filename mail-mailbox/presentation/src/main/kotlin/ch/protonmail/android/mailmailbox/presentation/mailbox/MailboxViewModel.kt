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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import java.util.Collections
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.extension.launchWithDelayedCallback
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcommon.presentation.model.BottomBarState
import ch.protonmail.android.mailcommon.presentation.ui.delete.DeleteDialogState
import ch.protonmail.android.mailconversation.domain.usecase.DeleteConversations
import ch.protonmail.android.mailconversation.domain.usecase.MarkConversationsAsRead
import ch.protonmail.android.mailconversation.domain.usecase.MarkConversationsAsUnread
import ch.protonmail.android.mailconversation.domain.usecase.MoveConversations
import ch.protonmail.android.mailconversation.domain.usecase.StarConversations
import ch.protonmail.android.mailconversation.domain.usecase.UnStarConversations
import ch.protonmail.android.maildetail.domain.usecase.GetAttachmentIntentValues
import ch.protonmail.android.mailfeatureflags.domain.annotation.ComposerEnabled
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.FindLocalSystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveCurrentViewMode
import ch.protonmail.android.maillabel.domain.usecase.ObserveMailLabels
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsItemId
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToItemId
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.model.MailboxPageKey
import ch.protonmail.android.mailmailbox.domain.model.toMailboxItemType
import ch.protonmail.android.mailmailbox.domain.usecase.GetBottomBarActions
import ch.protonmail.android.mailmailbox.domain.usecase.GetBottomSheetActions
import ch.protonmail.android.mailmailbox.domain.usecase.ObserveUnreadCounters
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.MailboxItemUiModelMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.SwipeActionsMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxEvent
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxTopAppBarState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxViewAction
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.UnreadFilterState
import ch.protonmail.android.mailmailbox.presentation.mailbox.reducer.MailboxReducer
import ch.protonmail.android.mailmailbox.presentation.paging.MailboxPagerFactory
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.UnreadCounter
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
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MailboxMoreActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.ManageAccountSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MoveToBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.UpsellingBottomSheetState
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.usecase.ObserveFolderColorSettings
import ch.protonmail.android.mailsettings.domain.usecase.ObserveSwipeActionsPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.android.core.accountmanager.domain.usecase.ObservePrimaryAccountAvatarItem
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ViewMode
import me.proton.core.util.kotlin.DispatcherProvider
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@SuppressWarnings("LongParameterList", "TooManyFunctions", "LargeClass")
class MailboxViewModel @Inject constructor(
    private val mailboxPagerFactory: MailboxPagerFactory,
    private val observeCurrentViewMode: ObserveCurrentViewMode,
    observePrimaryUserId: ObservePrimaryUserId,
    private val observeMailLabels: ObserveMailLabels,
    private val observeSwipeActionsPreference: ObserveSwipeActionsPreference,
    private val selectedMailLabelId: SelectedMailLabelId,
    private val observeUnreadCounters: ObserveUnreadCounters,
    private val observeFolderColorSettings: ObserveFolderColorSettings,
    private val getBottomBarActions: GetBottomBarActions,
    private val getBottomSheetActions: GetBottomSheetActions,
    private val actionUiModelMapper: ActionUiModelMapper,
    private val mailboxItemMapper: MailboxItemUiModelMapper,
    private val swipeActionsMapper: SwipeActionsMapper,
    private val markConversationsAsRead: MarkConversationsAsRead,
    private val markConversationsAsUnread: MarkConversationsAsUnread,
    private val markMessagesAsRead: MarkMessagesAsRead,
    private val markMessagesAsUnread: MarkMessagesAsUnread,
    private val moveConversations: MoveConversations,
    private val moveMessages: MoveMessages,
    private val deleteConversations: DeleteConversations,
    private val deleteMessages: DeleteMessages,
    private val starMessages: StarMessages,
    private val starConversations: StarConversations,
    private val unStarMessages: UnStarMessages,
    private val unStarConversations: UnStarConversations,
    private val mailboxReducer: MailboxReducer,
    private val dispatchersProvider: DispatcherProvider,
    private val deleteSearchResults: DeleteSearchResults,
    private val findLocalSystemLabelId: FindLocalSystemLabelId,
    private val loadAvatarImage: LoadAvatarImage,
    private val handleAvatarImageLoadingFailure: HandleAvatarImageLoadingFailure,
    private val observeAvatarImageStates: ObserveAvatarImageStates,
    private val observePrimaryAccountAvatarItem: ObservePrimaryAccountAvatarItem,
    private val getAttachmentIntentValues: GetAttachmentIntentValues,
    @ComposerEnabled val isComposerEnabled: Flow<Boolean>
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId().filterNotNull()
    private val mutableState = MutableStateFlow(initialState)
    private val itemIds = Collections.synchronizedList(mutableListOf<String>())
    private val folderColorSettings = primaryUserId.flatMapLatest {
        observeFolderColorSettings(it).distinctUntilChanged()
    }

    val state: StateFlow<MailboxState> = mutableState.asStateFlow()
    val items: Flow<PagingData<MailboxItemUiModel>> = observePagingData().cachedIn(viewModelScope)

    init {
        observeCurrentMailLabel()
            .onEach { currentMailLabel ->
                currentMailLabel?.let {
                    emitNewStateFrom(MailboxEvent.SelectedLabelChanged(currentMailLabel))
                } ?: run {
                    primaryUserId.firstOrNull()?.let { userId ->
                        findLocalSystemLabelId(userId, SystemLabelId.Inbox)?.let { inboxLabelId ->
                            emitNewStateFrom(
                                MailboxEvent.SelectedLabelChanged(
                                    MailLabel.System(inboxLabelId, SystemLabelId.Inbox, 0)
                                )
                            )
                        }
                    }
                }
            }
            .filterNotNull()
            .launchIn(viewModelScope)

        selectedMailLabelId.flow
            .mapToExistingLabel()
            .pairWithCurrentLabelCount()
            .combine(primaryUserId.filterNotNull()) { labelWithCount, userId ->
                Triple(labelWithCount.first, labelWithCount.second, userId)
            }
            .onEach { (currentMailLabel, currentLabelCount, _) ->
                itemIds.clear()
                emitNewStateFrom(MailboxEvent.NewLabelSelected(currentMailLabel, currentLabelCount))
            }
            .flatMapLatest { (currentMailLabel, _, userId) ->
                handleSwipeActionPreferences(userId, currentMailLabel)
            }
            .onEach {
                emitNewStateFrom(it)
            }
            .launchIn(viewModelScope)

        selectedMailLabelId.flow.mapToExistingLabel()
            .combine(state.observeSelectedMailboxItems()) { selectedMailLabel, selectedMailboxItems ->
                getBottomBarActions(
                    primaryUserId.filterNotNull().first(),
                    selectedMailLabel.id.labelId,
                    selectedMailboxItems.map { MailboxItemId(it.id) },
                    getViewModeForCurrentLocation(selectedMailLabel.id)
                ).fold(
                    ifLeft = { MailboxEvent.MessageBottomBarEvent(BottomBarEvent.ErrorLoadingActions) },
                    ifRight = { actions ->
                        MailboxEvent.MessageBottomBarEvent(
                            BottomBarEvent.ActionsData(
                                actions.map { action -> actionUiModelMapper.toUiModel(action) }
                                    .toImmutableList()
                            )
                        )
                    }
                )
            }
            .distinctUntilChanged()
            .onEach { emitNewStateFrom(it) }
            .launchIn(viewModelScope)

        observeUnreadCounters()
            .mapToCurrentLabelCount()
            .filterNotNull()
            .onEach { currentLabelCount ->
                emitNewStateFrom(MailboxEvent.SelectedLabelCountChanged(currentLabelCount))
            }
            .launchIn(viewModelScope)

        observeAvatarImageStates()
            .onEach { avatarImageStates ->
                emitNewStateFrom(MailboxEvent.AvatarImageStatesUpdated(avatarImageStates))
            }
            .launchIn(viewModelScope)

        observePrimaryAccountAvatarItem().onEach { item ->
            emitNewStateFrom(MailboxEvent.PrimaryAccountAvatarChanged(item))
        }.launchIn(viewModelScope)
    }

    private fun handleSwipeActionPreferences(userId: UserId, currentMailLabel: MailLabel): Flow<MailboxEvent> {
        return observeSwipeActionsPreference(userId)
            .map { swipeActionsPreference ->
                val currentMailLabelId = when (currentMailLabel) {
                    is MailLabel.Custom -> currentMailLabel.id.labelId
                    is MailLabel.System -> currentMailLabel.systemLabelId.labelId
                }
                val swipeActions = swipeActionsMapper(currentMailLabelId, swipeActionsPreference)
                MailboxEvent.SwipeActionsChanged(swipeActions)
            }
            .distinctUntilChanged()
    }

    @SuppressWarnings("ComplexMethod", "LongMethod")
    internal fun submit(viewAction: MailboxViewAction) {
        viewModelScope.launch {
            when (viewAction) {
                is MailboxViewAction.ExitSelectionMode,
                is MailboxViewAction.DisableUnreadFilter,
                is MailboxViewAction.EnableUnreadFilter -> emitNewStateFrom(viewAction)

                is MailboxViewAction.MailboxItemsChanged -> handleMailboxItemChanged(viewAction.itemIds)
                is MailboxViewAction.OnItemAvatarClicked -> handleOnAvatarClicked(viewAction.item)
                is MailboxViewAction.OnAvatarImageLoadRequested -> handleOnAvatarImageLoadRequested(viewAction.item)
                is MailboxViewAction.OnAvatarImageLoadFailed -> handleOnAvatarImageLoadFailed(viewAction.item)
                is MailboxViewAction.OnItemLongClicked -> handleItemLongClick(viewAction.item)
                is MailboxViewAction.Refresh -> emitNewStateFrom(viewAction)
                is MailboxViewAction.RefreshCompleted -> emitNewStateFrom(viewAction)
                is MailboxViewAction.ItemClicked -> handleItemClick(viewAction.item)
                is MailboxViewAction.OnOfflineWithData -> emitNewStateFrom(viewAction)
                is MailboxViewAction.OnErrorWithData -> emitNewStateFrom(viewAction)
                is MailboxViewAction.MarkAsRead -> handleMarkAsReadAction(viewAction)
                is MailboxViewAction.MarkAsUnread -> handleMarkAsUnreadAction(viewAction)
                is MailboxViewAction.SwipeReadAction -> handleSwipeReadAction(viewAction)
                is MailboxViewAction.SwipeArchiveAction -> handleSwipeArchiveAction(viewAction)
                is MailboxViewAction.SwipeSpamAction -> handleSwipeSpamAction(viewAction)
                is MailboxViewAction.SwipeTrashAction -> handleSwipeTrashAction(viewAction)
                is MailboxViewAction.StarAction -> handleSwipeStarAction(viewAction)
                is MailboxViewAction.SwipeLabelAsAction -> requestLabelAsBottomSheet(viewAction)
                is MailboxViewAction.SwipeMoveToAction -> requestMoveToBottomSheet(viewAction)
                is MailboxViewAction.Trash -> handleTrashAction()
                is MailboxViewAction.Delete -> handleDeleteAction()
                is MailboxViewAction.MoveToInbox -> handleMoveToInboxAction(viewAction)
                is MailboxViewAction.DeleteConfirmed -> handleDeleteConfirmedAction()
                is MailboxViewAction.DeleteDialogDismissed -> handleDeleteDialogDismissed()
                is MailboxViewAction.RequestLabelAsBottomSheet -> requestLabelAsBottomSheet(viewAction)

                is MailboxViewAction.RequestMoveToBottomSheet -> requestMoveToBottomSheet(viewAction)
                is MailboxViewAction.RequestMoreActionsBottomSheet -> showMoreBottomSheet(viewAction)
                is MailboxViewAction.RequestManageAccountsBottomSheet -> showAccountManagerBottomSheet(viewAction)
                is MailboxViewAction.DismissBottomSheet -> emitNewStateFrom(viewAction)
                is MailboxViewAction.Star -> handleStarAction(viewAction)
                is MailboxViewAction.UnStar -> handleUnStarAction(viewAction)
                is MailboxViewAction.MoveToArchive -> handleMoveToArchiveAction(viewAction)
                is MailboxViewAction.MoveToSpam -> handleMoveToSpamAction(viewAction)
                is MailboxViewAction.EnterSearchMode -> emitNewStateFrom(viewAction)
                is MailboxViewAction.ExitSearchMode -> handleExitSearchMode(viewAction)
                is MailboxViewAction.SearchQuery -> emitNewStateFrom(viewAction)
                is MailboxViewAction.SearchResult -> emitNewStateFrom(viewAction)
                is MailboxViewAction.RequestUpsellingBottomSheet -> showUpsellingBottomSheet(viewAction)
                is MailboxViewAction.NavigateToInboxLabel -> handleNavigateToInbox()
                is MailboxViewAction.SelectAll -> handleSelectAllAction(viewAction)
                is MailboxViewAction.DeselectAll -> handleDeselectAllAction()
                is MailboxViewAction.CustomizeToolbar -> handleCustomizeToolbar(viewAction)
                is MailboxViewAction.RequestAttachment -> handleRequestAttachment(viewAction)
            }
        }
    }

    private fun handleRequestAttachment(action: MailboxViewAction.RequestAttachment) {
        viewModelScope.launchWithDelayedCallback(
            onThresholdExceeded = { emitNewStateFrom(MailboxEvent.AttachmentDownloadOngoingEvent) }
        ) {
            val domainAttachmentId = AttachmentId(action.attachmentId.value)
            val attachmentIntentValues = getAttachmentIntentValues(primaryUserId.first(), domainAttachmentId)
                .getOrElse { return@launchWithDelayedCallback emitNewStateFrom(MailboxEvent.AttachmentErrorEvent) }

            emitNewStateFrom(MailboxEvent.AttachmentReadyEvent(attachmentIntentValues))
        }
    }

    private suspend fun handleNavigateToInbox() {
        val inboxLabel = findLocalSystemLabelId(primaryUserId.first(), SystemLabelId.Inbox)
            ?: return Timber.e("Unable to find Inbox system label")

        selectedMailLabelId.set(inboxLabel)
    }

    private fun handleCustomizeToolbar(viewAction: MailboxViewAction) {
        emitNewStateFrom(viewAction)
    }

    private fun handleSelectAllAction(action: MailboxViewAction.SelectAll) {
        emitNewStateFrom(MailboxEvent.AllItemsSelected(action.allItems))
    }

    private fun handleDeselectAllAction() {
        emitNewStateFrom(MailboxEvent.AllItemsDeselected)
    }

    private suspend fun handleExitSearchMode(viewAction: MailboxViewAction) {
        val user = primaryUserId.filterNotNull().first()

        deleteSearchResults(user, state.value.getSearchQuery())

        emitNewStateFrom(viewAction)
    }

    private suspend fun handleMailboxItemChanged(updatedItemIds: List<String>) {
        withContext(dispatchersProvider.Comp) {
            val removedItems = itemIds.filterNot { updatedItemIds.contains(it) }
            itemIds.clear()
            itemIds.addAll(updatedItemIds)
            Timber.d("Removed items: $removedItems")
            if (removedItems.isNotEmpty()) {
                when (val currentState = state.value.mailboxListState) {
                    is MailboxListState.Data.SelectionMode -> {
                        currentState.selectedMailboxItems
                            .map { it.id }
                            .filter { currentSelectedItem -> removedItems.contains(currentSelectedItem) }
                            .takeIf { it.isNotEmpty() }
                            ?.let { emitNewStateFrom(MailboxEvent.ItemsRemovedFromSelection(it)) }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun handleItemClick(item: MailboxItemUiModel) {
        when (state.value.mailboxListState) {
            is MailboxListState.Data.SelectionMode -> handleItemClickInSelectionMode(item)
            is MailboxListState.Data.ViewMode -> handleItemClickInViewMode(item)
            is MailboxListState.Loading -> {
                Timber.d("Loading state can't handle item clicks")
            }
        }
    }

    private suspend fun handleItemClickInViewMode(item: MailboxItemUiModel) {
        if (item.shouldOpenInComposer) {
            emitNewStateFrom(MailboxEvent.ItemClicked.OpenComposer(item))
        } else {
            val labelId = getFromLabelIdSearchAware()
            emitNewStateFrom(MailboxEvent.ItemClicked.ItemDetailsOpened(item, labelId))
        }
    }

    private fun handleItemLongClick(item: MailboxItemUiModel) {
        when (val state = state.value.mailboxListState) {
            is MailboxListState.Data.ViewMode -> enterSelectionMode(item)
            else -> {
                Timber.d("Long click not supported in state: $state")
            }
        }
    }

    private fun handleOnAvatarClicked(item: MailboxItemUiModel) {
        when (val state = state.value.mailboxListState) {
            is MailboxListState.Data.ViewMode -> enterSelectionMode(item)
            is MailboxListState.Data.SelectionMode -> handleItemClickInSelectionMode(item)
            else -> {
                Timber.d("Avatar clicked not supported in state: $state")
            }
        }
    }

    private fun handleOnAvatarImageLoadRequested(item: MailboxItemUiModel) {
        (item.avatar as? AvatarUiModel.ParticipantAvatar)?.let { avatar ->
            viewModelScope.launch {
                loadAvatarImage(avatar.address, avatar.bimiSelector)
            }
        }
    }

    private fun handleOnAvatarImageLoadFailed(item: MailboxItemUiModel) {
        (item.avatar as? AvatarUiModel.ParticipantAvatar)?.let { avatar ->
            viewModelScope.launch {
                handleAvatarImageLoadingFailure(avatar.address, avatar.bimiSelector)
            }
        }
    }

    private fun handleItemClickInSelectionMode(item: MailboxItemUiModel) {
        val selectionMode = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionMode == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }

        val event = if (selectionMode.selectedMailboxItems.any { it.id == item.id }) {
            if (selectionMode.selectedMailboxItems.size == 1) {
                MailboxViewAction.ExitSelectionMode
            } else {
                MailboxEvent.ItemClicked.ItemRemovedFromSelection(item)
            }
        } else {
            MailboxEvent.ItemClicked.ItemAddedToSelection(item)
        }

        emitNewStateFrom(event)
    }

    private fun enterSelectionMode(item: MailboxItemUiModel) {
        when (val state = state.value.mailboxListState) {
            is MailboxListState.Data.ViewMode -> emitNewStateFrom(MailboxEvent.EnterSelectionMode(item))
            else -> Timber.d("Cannot enter selection mode from state: $state")
        }
    }

    /**
     * Creates a [MailboxPagerFactory] and observes the emitted paging data. The Pager is re-created, when:
     * - The selected Mail Label (ie. Mailbox location) changes
     * - The "Unread filter" state changes
     * - The search keyword changes (search mode)
     *
     * This method keeps track of the "selected mail label" and the "search mode state" it's been called
     * to be able to hide the displayed items and show a loader as soon as such params change (avoiding
     * the old location's items from being displayed till the new ones are loaded).
     * This is achieved through `pagingDataFlow.emit(PagingData.empty())`
     */
    private fun observePagingData(): Flow<PagingData<MailboxItemUiModel>> {
        val pagingDataFlow = MutableStateFlow<PagingData<MailboxItemUiModel>>(PagingData.empty())
        var currentMailLabel: MailLabel? = null
        var currentSearchModeState: Boolean? = null

        primaryUserId.filterNotNull().flatMapLatest { userId ->
            combine(
                state.observeMailLabelChanges(),
                state.observeUnreadFilterState(),
                state.observeSearchQuery()
            ) { selectedMailLabel, unreadFilterEnabled, query ->

                val isInSearchMode = state.value.isInSearchMode()
                if (selectedMailLabel != currentMailLabel || currentSearchModeState != isInSearchMode) {
                    pagingDataFlow.emit(
                        PagingData.empty(
                            LoadStates(LoadState.Loading, LoadState.Loading, LoadState.Loading)
                        )
                    )
                    currentMailLabel = selectedMailLabel
                    currentSearchModeState = isInSearchMode
                }

                val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
                mailboxPagerFactory.create(
                    userId = userId,
                    selectedMailLabelId = selectedMailLabel.id,
                    filterUnread = unreadFilterEnabled,
                    type = if (query.isEmpty()) viewMode.toMailboxItemType() else MailboxItemType.Message,
                    searchQuery = query
                )
            }.flatMapLatest { mapPagingData(userId, it) }
        }.onEach {
            pagingDataFlow.emit(it)
        }.launchIn(viewModelScope)

        return pagingDataFlow
    }

    private suspend fun mapPagingData(
        userId: UserId,
        pager: Pager<MailboxPageKey, MailboxItem>
    ): Flow<PagingData<MailboxItemUiModel>> {
        return withContext(dispatchersProvider.Comp) {
            val folderColorSettingsValue = folderColorSettings.first()
            pager.flow.mapLatest { pagingData ->
                pagingData.map {
                    withContext(dispatchersProvider.Comp) {
                        mailboxItemMapper.toUiModel(
                            userId, it, folderColorSettingsValue,
                            state.value.isInSearchMode()
                        )
                    }
                }
            }
        }
    }

    private suspend fun handleMarkAsReadAction(markAsReadOperation: MailboxViewAction.MarkAsRead) {
        val selectionModeDataState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionModeDataState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }

        val user = primaryUserId.filterNotNull().first()
        val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
        when (viewMode) {
            ViewMode.ConversationGrouping -> markConversationsAsRead(
                userId = user,
                labelId = getFromLabelIdSearchAware(),
                conversationIds = selectionModeDataState.selectedMailboxItems.map { ConversationId(it.id) }
            )

            ViewMode.NoConversationGrouping -> markMessagesAsRead(
                userId = user,
                messageIds = selectionModeDataState.selectedMailboxItems.map { MessageId(it.id) }
            )
        }
        emitNewStateFrom(markAsReadOperation)
    }

    private suspend fun handleMarkAsUnreadAction(markAsReadOperation: MailboxViewAction.MarkAsUnread) {
        val selectionModeDataState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionModeDataState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }
        val userId = primaryUserId.filterNotNull().first()
        val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
        when (viewMode) {
            ViewMode.ConversationGrouping -> markConversationsAsUnread(
                userId = userId,
                labelId = getFromLabelIdSearchAware(),
                conversationIds = selectionModeDataState.selectedMailboxItems.map { ConversationId(it.id) }
            )

            ViewMode.NoConversationGrouping -> markMessagesAsUnread(
                userId = userId,
                messageIds = selectionModeDataState.selectedMailboxItems.map { MessageId(it.id) }
            )
        }
        emitNewStateFrom(markAsReadOperation)
    }

    private suspend fun handleSwipeReadAction(swipeReadAction: MailboxViewAction.SwipeReadAction) {
        if (swipeReadAction.isRead) {
            when (getViewModeForCurrentLocation(selectedMailLabelId.flow.value)) {
                ViewMode.ConversationGrouping -> markConversationsAsUnread(
                    userId = primaryUserId.filterNotNull().first(),
                    labelId = getFromLabelIdSearchAware(),
                    conversationIds = listOf(ConversationId(swipeReadAction.itemId))
                )

                ViewMode.NoConversationGrouping -> markMessagesAsUnread(
                    userId = primaryUserId.filterNotNull().first(),
                    messageIds = listOf(MessageId(swipeReadAction.itemId))
                )
            }
        } else {
            when (getViewModeForCurrentLocation(selectedMailLabelId.flow.value)) {
                ViewMode.ConversationGrouping -> markConversationsAsRead(
                    userId = primaryUserId.filterNotNull().first(),
                    labelId = getFromLabelIdSearchAware(),
                    conversationIds = listOf(ConversationId(swipeReadAction.itemId))
                )

                ViewMode.NoConversationGrouping -> markMessagesAsRead(
                    userId = primaryUserId.filterNotNull().first(),
                    messageIds = listOf(MessageId(swipeReadAction.itemId))
                )
            }
        }
        emitNewStateFrom(swipeReadAction)
    }

    private suspend fun handleSwipeStarAction(swipeStarAction: MailboxViewAction.StarAction) {
        if (swipeStarAction.isStarred) {
            when (getViewModeForCurrentLocation(selectedMailLabelId.flow.value)) {
                ViewMode.ConversationGrouping -> unStarConversations(
                    userId = primaryUserId.filterNotNull().first(),
                    conversationIds = listOf(ConversationId(swipeStarAction.itemId))
                )

                ViewMode.NoConversationGrouping -> unStarMessages(
                    userId = primaryUserId.filterNotNull().first(),
                    messageIds = listOf(MessageId(swipeStarAction.itemId))
                )
            }
        } else {
            when (getViewModeForCurrentLocation(selectedMailLabelId.flow.value)) {
                ViewMode.ConversationGrouping -> starConversations(
                    userId = primaryUserId.filterNotNull().first(),
                    conversationIds = listOf(ConversationId(swipeStarAction.itemId))
                )

                ViewMode.NoConversationGrouping -> starMessages(
                    userId = primaryUserId.filterNotNull().first(),
                    messageIds = listOf(MessageId(swipeStarAction.itemId))
                )
            }
        }
        emitNewStateFrom(swipeStarAction)
    }

    private suspend fun handleSwipeArchiveAction(swipeArchiveAction: MailboxViewAction.SwipeArchiveAction) {
        if (isActionAllowedForCurrentLabel(SystemLabelId.Archive.labelId)) {
            val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
            val userId = primaryUserId.filterNotNull().first()
            moveSingleItemToDestination(userId, swipeArchiveAction.itemId, SystemLabelId.Archive, viewMode)
            emitNewStateFrom(swipeArchiveAction)
        }
    }

    private suspend fun handleSwipeSpamAction(swipeSpamAction: MailboxViewAction.SwipeSpamAction) {
        if (isActionAllowedForCurrentLabel(SystemLabelId.Spam.labelId)) {
            val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
            val userId = primaryUserId.filterNotNull().first()
            moveSingleItemToDestination(userId, swipeSpamAction.itemId, SystemLabelId.Spam, viewMode)
            emitNewStateFrom(swipeSpamAction)
        }
    }

    private suspend fun handleSwipeTrashAction(swipeTrashAction: MailboxViewAction.SwipeTrashAction) {
        if (isActionAllowedForCurrentLabel(SystemLabelId.Trash.labelId)) {
            val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
            val userId = primaryUserId.filterNotNull().first()
            moveSingleItemToDestination(userId, swipeTrashAction.itemId, SystemLabelId.Trash, viewMode)
            emitNewStateFrom(swipeTrashAction)
        }
    }

    private suspend fun moveSingleItemToDestination(
        userId: UserId,
        itemId: String,
        systemLabelId: SystemLabelId,
        viewMode: ViewMode
    ) {
        when (viewMode) {
            ViewMode.ConversationGrouping -> moveConversations(
                userId = userId,
                conversationIds = listOf(ConversationId(itemId)),
                systemLabelId = systemLabelId
            )

            ViewMode.NoConversationGrouping -> moveMessages(
                userId = userId,
                messageIds = listOf(MessageId(itemId)),
                systemLabelId = systemLabelId
            )
        }
    }

    private fun requestLabelAsBottomSheet(operation: MailboxViewAction) {
        val items = when (operation) {
            is MailboxViewAction.RequestLabelAsBottomSheet -> {
                val selectionMode = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
                if (selectionMode == null) {
                    Timber.d("MailboxListState is not in SelectionMode")
                    return
                }
                selectionMode.selectedMailboxItems.map { LabelAsItemId(it.id) }
            }

            is MailboxViewAction.SwipeLabelAsAction -> {
                listOf(operation.itemId)
            }

            else -> return
        }

        viewModelScope.launch {
            val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
            val entryPoint = when (operation) {
                is MailboxViewAction.RequestLabelAsBottomSheet ->
                    LabelAsBottomSheetEntryPoint.Mailbox.SelectionMode(viewMode)

                is MailboxViewAction.SwipeLabelAsAction ->
                    LabelAsBottomSheetEntryPoint.Mailbox.LabelAsSwipeAction(viewMode, operation.itemId)

                else -> {
                    Timber.e("Unsupported operation: $operation")
                    return@launch
                }
            }

            val event = LabelAsBottomSheetState.LabelAsBottomSheetEvent.Ready(
                userId = primaryUserId.first(),
                currentLabel = selectedMailLabelId.flow.value.labelId,
                itemIds = items,
                entryPoint = entryPoint
            )
            emitNewStateFrom(MailboxEvent.MailboxBottomSheetEvent(event))
        }
    }

    private fun requestMoveToBottomSheet(operation: MailboxViewAction) {
        viewModelScope.launch {
            val userId = primaryUserId.filterNotNull().first()
            val currentMailLabel = selectedMailLabelId.flow.value
            val viewMode = getViewModeForCurrentLocation(currentMailLabel)

            val (entryPoint, selectedItemIds) = when (operation) {
                is MailboxViewAction.RequestMoveToBottomSheet -> {
                    val selectionMode = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
                    if (selectionMode == null) {
                        Timber.d("MailboxListState is not in SelectionMode")
                        return@launch
                    }
                    Pair(
                        MoveToBottomSheetEntryPoint.Mailbox.SelectionMode(viewMode),
                        selectionMode.selectedMailboxItems.map { MoveToItemId(it.id) }
                    )
                }

                is MailboxViewAction.SwipeMoveToAction -> Pair(
                    MoveToBottomSheetEntryPoint.Mailbox.MoveToSwipeAction(viewMode, operation.itemId),
                    listOf(operation.itemId)
                )

                else -> {
                    Timber.d("Unsupported operation: $operation")
                    return@launch
                }
            }

            val event = MoveToBottomSheetState.MoveToBottomSheetEvent.Ready(
                userId = userId,
                currentLabel = currentMailLabel.labelId,
                itemIds = selectedItemIds,
                entryPoint = entryPoint
            )

            emitNewStateFrom(MailboxEvent.MailboxBottomSheetEvent(event))
        }
    }


    private fun showAccountManagerBottomSheet(operation: MailboxViewAction) {
        emitNewStateFrom(operation)
        emitNewStateFrom(
            MailboxEvent.MailboxBottomSheetEvent(
                ManageAccountSheetState.ManageAccountsBottomSheetEvent.Ready
            )
        )
    }

    private suspend fun showMoreBottomSheet(operation: MailboxViewAction) {
        val selectionState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }
        emitNewStateFrom(operation)

        val userId = primaryUserId.filterNotNull().first()
        val currentMailLabel = selectedMailLabelId.flow.value
        val viewMode = getViewModeForCurrentLocation(currentMailLabel)
        val selectedItemIds: List<MailboxItemId> = selectionState.selectedMailboxItems.map { MailboxItemId(it.id) }

        val actions = getBottomSheetActions(userId, currentMailLabel.labelId, selectedItemIds, viewMode)
            .getOrElse {
                Timber.e("Mailbox failed to load the bottom-sheet actions: $it")
                return
            }

        emitNewStateFrom(
            MailboxEvent.MailboxBottomSheetEvent(
                MailboxMoreActionsBottomSheetState.MailboxMoreActionsBottomSheetEvent.ActionData(
                    hiddenActionUiModels = actions.hiddenActions
                        .map { actionUiModelMapper.toUiModel(it) }
                        .toImmutableList(),
                    visibleActionUiModels = actions.visibleActions
                        .map { actionUiModelMapper.toUiModel(it) }
                        .toImmutableList(),
                    customizeToolbarActionUiModel = actionUiModelMapper.toUiModel(Action.CustomizeToolbar),
                    selectedCount = selectionState.selectedMailboxItems.size
                )
            )
        )
    }

    private suspend fun handleTrashAction() {
        moveSelectedMailboxItemsTo(SystemLabelId.Trash).onRight {
            emitNewStateFrom(MailboxEvent.Trash(it))
        }.onLeft {
            emitNewStateFrom(MailboxEvent.ErrorMoving)
        }
    }

    private suspend fun handleMoveToInboxAction(action: MailboxViewAction.MoveToInbox) {
        moveSelectedMailboxItemsTo(SystemLabelId.Inbox).onRight {
            emitNewStateFrom(action)
        }.onLeft {
            emitNewStateFrom(MailboxEvent.ErrorMoving)
        }
    }

    private suspend fun handleMoveToArchiveAction(action: MailboxViewAction.MoveToArchive) {
        moveSelectedMailboxItemsTo(SystemLabelId.Archive).onRight {
            emitNewStateFrom(action)
        }.onLeft {
            emitNewStateFrom(MailboxEvent.ErrorMoving)
        }
    }

    private suspend fun handleMoveToSpamAction(action: MailboxViewAction.MoveToSpam) {
        moveSelectedMailboxItemsTo(SystemLabelId.Spam).onRight {
            emitNewStateFrom(action)
        }.onLeft {
            emitNewStateFrom(MailboxEvent.ErrorMoving)
        }
    }

    private suspend fun moveSelectedMailboxItemsTo(systemLabelId: SystemLabelId): Either<DataError, Int> {
        val selectionModeDataState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionModeDataState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return DataError.Local.Unknown.left()
        }
        val userId = primaryUserId.filterNotNull().first()
        val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
        return when (viewMode) {
            ViewMode.ConversationGrouping -> moveConversations(
                userId = userId,
                conversationIds = selectionModeDataState.selectedMailboxItems.map { ConversationId(it.id) },
                systemLabelId = systemLabelId
            )

            ViewMode.NoConversationGrouping -> moveMessages(
                userId = userId,
                messageIds = selectionModeDataState.selectedMailboxItems.map { MessageId(it.id) },
                systemLabelId = systemLabelId
            )
        }.flatMap {
            selectionModeDataState.selectedMailboxItems.size.right()
        }
    }

    private suspend fun handleDeleteAction() {
        val selectionModeDataState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionModeDataState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }
        val event = MailboxEvent.Delete(
            viewMode = getPreferredViewMode(),
            numAffectedMessages = selectionModeDataState.selectedMailboxItems.size
        )
        emitNewStateFrom(event)
    }

    private suspend fun handleDeleteConfirmedAction() {
        val selectionModeDataState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionModeDataState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }

        val userId = primaryUserId.filterNotNull().first()
        val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
        when (viewMode) {
            ViewMode.ConversationGrouping -> {
                deleteConversations(
                    userId = userId,
                    conversationIds = selectionModeDataState.selectedMailboxItems.map { ConversationId(it.id) }
                )
            }

            ViewMode.NoConversationGrouping -> {
                deleteMessages(
                    userId = userId,
                    messageIds = selectionModeDataState.selectedMailboxItems.map { MessageId(it.id) },
                    currentLabelId = selectionModeDataState.currentMailLabel.id.labelId
                )
            }
        }.onLeft {
            emitNewStateFrom(MailboxEvent.ErrorDeleting)
        }.onRight {
            emitNewStateFrom(MailboxEvent.DeleteConfirmed(viewMode, selectionModeDataState.selectedMailboxItems.size))
        }
    }

    private suspend fun handleStarAction(viewAction: MailboxViewAction) {
        val selectionModeDataState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionModeDataState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }
        val userId = primaryUserId.filterNotNull().first()
        val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
        when (viewMode) {
            ViewMode.ConversationGrouping -> {
                starConversations(userId, selectionModeDataState.selectedMailboxItems.map { ConversationId(it.id) })
            }

            ViewMode.NoConversationGrouping -> {
                starMessages(userId, selectionModeDataState.selectedMailboxItems.map { MessageId(it.id) })
            }
        }

        emitNewStateFrom(viewAction)
    }

    private suspend fun handleUnStarAction(viewAction: MailboxViewAction) {
        val selectionModeDataState = state.value.mailboxListState as? MailboxListState.Data.SelectionMode
        if (selectionModeDataState == null) {
            Timber.d("MailboxListState is not in SelectionMode")
            return
        }
        val userId = primaryUserId.filterNotNull().first()
        val viewMode = getViewModeForCurrentLocation(selectedMailLabelId.flow.value)
        when (viewMode) {
            ViewMode.ConversationGrouping -> {
                unStarConversations(userId, selectionModeDataState.selectedMailboxItems.map { ConversationId(it.id) })
            }

            ViewMode.NoConversationGrouping -> {
                unStarMessages(userId, selectionModeDataState.selectedMailboxItems.map { MessageId(it.id) })
            }
        }
        emitNewStateFrom(viewAction)
    }

    private fun handleDeleteDialogDismissed() {
        emitNewStateFrom(MailboxViewAction.DeleteDialogDismissed)
    }

    private fun showUpsellingBottomSheet(operation: MailboxViewAction) {
        viewModelScope.launch {
            emitNewStateFrom(operation)

            emitNewStateFrom(
                MailboxEvent.MailboxBottomSheetEvent(UpsellingBottomSheetState.UpsellingBottomSheetEvent.Ready)
            )
        }
    }

    private fun observeCurrentMailLabel() = observeMailLabels()
        .map { mailLabels ->
            mailLabels.allById[selectedMailLabelId.flow.value]
        }

    private fun Flow<MailLabelId>.mapToExistingLabel() = map {
        observeMailLabels().firstOrNull()?.let { mailLabels ->
            mailLabels.allById[selectedMailLabelId.flow.value]
        }
    }.filterNotNull()

    private fun observeUnreadCounters(): Flow<List<UnreadCounter>> = primaryUserId.flatMapLatest { userId ->
        observeUnreadCounters(userId)
    }

    private fun observeMailLabels() = primaryUserId.flatMapLatest { userId ->
        observeMailLabels(userId)
    }

    private suspend fun getPreferredViewMode(): ViewMode {
        val userId = primaryUserId.firstOrNull()

        return if (userId == null) {
            ObserveCurrentViewMode.DefaultViewMode
        } else {
            observeCurrentViewMode(userId).first()
        }
    }

    private suspend fun getViewModeForCurrentLocation(currentMailLabel: MailLabelId): ViewMode {
        val userId = primaryUserId.firstOrNull()

        return if (userId == null || state.value.isInSearchMode()) {
            ObserveCurrentViewMode.DefaultViewMode
        } else {
            observeCurrentViewMode(userId, currentMailLabel.labelId).first()
        }
    }

    private fun Flow<MailLabel>.pairWithCurrentLabelCount() = map { currentLabel ->
        val currentLabelCount = observeUnreadCounters().firstOrNull()
            ?.find { it.labelId == currentLabel.id.labelId }
            ?.count
        Pair(currentLabel, currentLabelCount)
    }

    private fun Flow<List<UnreadCounter>>.mapToCurrentLabelCount() = map { unreadCounters ->
        val currentMailLabelId = selectedMailLabelId.flow.value
        unreadCounters.find { it.labelId == currentMailLabelId.labelId }?.count
    }

    private fun emitNewStateFrom(operation: MailboxOperation) {
        val state = mailboxReducer.newStateFrom(state.value, operation)
        mutableState.value = state
    }

    private fun Flow<MailboxState>.observeUnreadFilterState() =
        this.map { it.unreadFilterState as? UnreadFilterState.Data }
            .mapNotNull { it?.isFilterEnabled }
            .distinctUntilChanged()

    private fun Flow<MailboxState>.observeMailLabelChanges() =
        this.map { it.mailboxListState as? MailboxListState.Data.ViewMode }
            .mapNotNull { it?.currentMailLabel }
            .distinctUntilChanged()

    private fun Flow<MailboxState>.observeSearchQuery() = this.map { it.mailboxListState as? MailboxListState.Data }
        .mapNotNull { it?.searchState?.searchQuery }
        .distinctUntilChanged()

    private fun MailboxState.isInSearchMode() =
        this.mailboxListState is MailboxListState.Data && this.mailboxListState.searchState.isInSearch()

    private fun MailboxState.getSearchQuery() = (this.mailboxListState as? MailboxListState.Data)
        ?.searchState
        ?.searchQuery
        ?: ""

    private fun Flow<MailboxState>.observeSelectedMailboxItems() =
        this.map { it.mailboxListState as? MailboxListState.Data.SelectionMode }
            .mapNotNull { it?.selectedMailboxItems }
            .distinctUntilChanged()

    private suspend fun isActionAllowedForCurrentLabel(labelId: LabelId): Boolean {
        return when (val mailLabel = observeCurrentMailLabel().first()) {
            is MailLabel.System -> mailLabel.systemLabelId.labelId != labelId
            else -> true
        }
    }

    private suspend fun getFromLabelIdSearchAware(): LabelId {
        val currentLabelId = selectedMailLabelId.flow.value.labelId

        if (!state.value.isInSearchMode()) {
            return currentLabelId
        }

        val userId = primaryUserId.filterNotNull().first()
        // Search should always happen with "AllMail" as current label
        return findLocalSystemLabelId(userId, SystemLabelId.AllMail)?.labelId
            ?: currentLabelId
    }

    companion object {

        val initialState = MailboxState(
            mailboxListState = MailboxListState.Loading,
            topAppBarState = MailboxTopAppBarState.Loading,
            unreadFilterState = UnreadFilterState.Loading,
            bottomAppBarState = BottomBarState.Data.Hidden(emptyList<ActionUiModel>().toImmutableList()),
            deleteDialogState = DeleteDialogState.Hidden,
            bottomSheetState = null,
            actionResult = Effect.empty(),
            error = Effect.empty()
        )
    }
}
