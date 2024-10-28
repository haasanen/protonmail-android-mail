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

package ch.protonmail.android.maildetail.presentation.viewmodel

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.isOfflineError
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.usecase.FindContactByEmail
import ch.protonmail.android.mailcontact.domain.usecase.ObserveContacts
import ch.protonmail.android.mailconversation.domain.usecase.DeleteConversations
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationMoveToLocations
import ch.protonmail.android.mailconversation.domain.usecase.ObserveConversation
import ch.protonmail.android.mailconversation.domain.usecase.StarConversations
import ch.protonmail.android.mailconversation.domain.usecase.UnStarConversations
import ch.protonmail.android.maildetail.domain.annotations.ObservableFlowScope
import ch.protonmail.android.maildetail.domain.model.OpenProtonCalendarIntentValues
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.maildetail.domain.usecase.GetAttachmentIntentValues
import ch.protonmail.android.maildetail.domain.usecase.GetDownloadingAttachmentsForMessages
import ch.protonmail.android.maildetail.domain.usecase.IsProtonCalendarInstalled
import ch.protonmail.android.maildetail.domain.usecase.MarkConversationAsUnread
import ch.protonmail.android.maildetail.domain.usecase.MarkMessageAsRead
import ch.protonmail.android.maildetail.domain.usecase.MarkMessageAsUnread
import ch.protonmail.android.maildetail.domain.usecase.MoveConversation
import ch.protonmail.android.maildetail.domain.usecase.MoveMessage
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationDetailActions
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationMessages
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationViewState
import ch.protonmail.android.maildetail.domain.usecase.ObserveMessageAttachmentStatus
import ch.protonmail.android.maildetail.domain.usecase.RelabelConversation
import ch.protonmail.android.maildetail.domain.usecase.ReportPhishingMessage
import ch.protonmail.android.maildetail.domain.usecase.SetMessageViewState
import ch.protonmail.android.maildetail.presentation.mapper.ConversationDetailMessageUiModelMapper
import ch.protonmail.android.maildetail.presentation.mapper.ConversationDetailMetadataUiModelMapper
import ch.protonmail.android.maildetail.presentation.mapper.MessageIdUiModelMapper
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMetadataState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.CollapseMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.DismissBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.DoNotAskLinkConfirmationAgain
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ExpandMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.LabelAsConfirmed
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.LabelAsToggleAction
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MarkUnread
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MessageBodyLinkClicked
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MoveToDestinationConfirmed
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MoveToDestinationSelected
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestContactActionsBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestConversationLabelAsBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestMoveToBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestScrollTo
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ScrollRequestCompleted
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ShowAllAttachmentsForMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.Star
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.Trash
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.UnStar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailsMessagesState
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.reducer.ConversationDetailReducer
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetailScreen
import ch.protonmail.android.maildetail.presentation.usecase.GetEmbeddedImageAvoidDuplicatedExecution
import ch.protonmail.android.maildetail.presentation.usecase.GetLabelAsBottomSheetData
import ch.protonmail.android.maildetail.presentation.usecase.GetMoreActionsBottomSheetData
import ch.protonmail.android.maildetail.presentation.usecase.OnMessageLabelAsConfirmed
import ch.protonmail.android.maildetail.presentation.usecase.PrintMessage
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabels
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.toUiModels
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetDecryptedMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageAttachment
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.domain.usecase.GetDecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageMoveToLocations
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.ContactActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MoveToBottomSheetState
import ch.protonmail.android.mailsettings.domain.usecase.privacy.ObservePrivacySettings
import ch.protonmail.android.mailsettings.domain.usecase.privacy.UpdateLinkConfirmationSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
class ConversationDetailViewModel @Inject constructor(
    observePrimaryUserId: ObservePrimaryUserId,
    private val messageIdUiModelMapper: MessageIdUiModelMapper,
    private val actionUiModelMapper: ActionUiModelMapper,
    private val conversationMessageMapper: ConversationDetailMessageUiModelMapper,
    private val conversationMetadataMapper: ConversationDetailMetadataUiModelMapper,
    private val markConversationAsUnread: MarkConversationAsUnread,
    private val moveConversation: MoveConversation,
    private val deleteConversations: DeleteConversations,
    private val relabelConversation: RelabelConversation,
    private val observeContacts: ObserveContacts,
    private val observeConversation: ObserveConversation,
    private val observeConversationMessages: ObserveConversationMessages,
    private val observeDetailActions: ObserveConversationDetailActions,
    private val getConversationMoveToLocations: GetConversationMoveToLocations,
    private val getMessageMoveToLocations: GetMessageMoveToLocations,
    private val observeMessageAttachmentStatus: ObserveMessageAttachmentStatus,
    private val getDownloadingAttachmentsForMessages: GetDownloadingAttachmentsForMessages,
    private val reducer: ConversationDetailReducer,
    private val starConversations: StarConversations,
    private val unStarConversations: UnStarConversations,
    private val savedStateHandle: SavedStateHandle,
    private val getDecryptedMessageBody: GetDecryptedMessageBody,
    private val markMessageAsRead: MarkMessageAsRead,
    private val setMessageViewState: SetMessageViewState,
    private val observeConversationViewState: ObserveConversationViewState,
    private val getAttachmentIntentValues: GetAttachmentIntentValues,
    private val getEmbeddedImageAvoidDuplicatedExecution: GetEmbeddedImageAvoidDuplicatedExecution,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val observePrivacySettings: ObservePrivacySettings,
    private val updateLinkConfirmationSetting: UpdateLinkConfirmationSetting,
    private val reportPhishingMessage: ReportPhishingMessage,
    private val isProtonCalendarInstalled: IsProtonCalendarInstalled,
    private val networkManager: NetworkManager,
    private val printMessage: PrintMessage,
    private val markMessageAsUnread: MarkMessageAsUnread,
    private val findContactByEmail: FindContactByEmail,
    private val getLabelAsBottomSheetData: GetLabelAsBottomSheetData,
    private val getMoreActionsBottomSheetData: GetMoreActionsBottomSheetData,
    private val onMessageLabelAsConfirmed: OnMessageLabelAsConfirmed,
    private val moveMessage: MoveMessage,
    @ObservableFlowScope private val observableFlowScope: CoroutineScope,
    @AppScope private val appScope: CoroutineScope
) : ViewModel() {

    private val primaryUserId: Flow<UserId> = observePrimaryUserId().filterNotNull()
    private val mutableDetailState = MutableStateFlow(initialState)
    private val conversationId = requireConversationId()
    private val initialScrollToMessageId = getInitialScrollToMessageId()
    private val filterByLocation = getFilterByLocation()
    private val observedAttachments = mutableListOf<AttachmentId>()
    val state: StateFlow<ConversationDetailState> = mutableDetailState.asStateFlow()

    init {
        Timber.d("Open detail screen for conversation ID: $conversationId")
        observeConversationMetadata(conversationId)
        observeConversationMessages(conversationId)
        observeBottomBarActions(conversationId)
        observePrivacySettings()
    }

    @Suppress("ComplexMethod")
    fun submit(action: ConversationDetailViewAction) {
        when (action) {
            is Star -> starConversation()
            is UnStar -> unStarConversation()
            is MarkUnread -> markAsUnread()
            is Trash -> moveConversationToTrash()
            is ConversationDetailViewAction.DeleteConfirmed -> handleDeleteConfirmed(action)
            is RequestMoveToBottomSheet -> showMoveToBottomSheet(action)
            is MoveToDestinationConfirmed -> onMoveToDestinationConfirmed(action.mailLabelText, action.messageId)
            is RequestConversationLabelAsBottomSheet -> showConversationLabelAsBottomSheet(action)
            is RequestContactActionsBottomSheet -> showContactActionsBottomSheetAndLoadData(action)
            is LabelAsConfirmed -> onLabelAsConfirmed(action)
            is ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet ->
                showMessageMoreActionsBottomSheet(action)
            is ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet ->
                showConversationMoreActionsBottomSheet(action)
            is ConversationDetailViewAction.RequestMessageLabelAsBottomSheet -> showMessageLabelAsBottomSheet(action)
            is ConversationDetailViewAction.RequestMessageMoveToBottomSheet -> showMessageMoveToBottomSheet(action)

            is ExpandMessage -> onExpandMessage(action.messageId)
            is CollapseMessage -> onCollapseMessage(action.messageId)
            is DoNotAskLinkConfirmationAgain -> onDoNotAskLinkConfirmationChecked()
            is ShowAllAttachmentsForMessage -> showAllAttachmentsForMessage(action.messageId)
            is ConversationDetailViewAction.OnAttachmentClicked -> {
                onOpenAttachmentClicked(action.messageId, action.attachmentId)
            }

            is ConversationDetailViewAction.ReportPhishing -> handleReportPhishing(action)
            is ConversationDetailViewAction.ReportPhishingConfirmed -> handleReportPhishingConfirmed(action)
            is ConversationDetailViewAction.OpenInProtonCalendar -> handleOpenInProtonCalendar(action)
            is ConversationDetailViewAction.Print -> handlePrint(action.context, action.messageId)
            is ConversationDetailViewAction.MarkMessageUnread -> handleMarkMessageUnread(action)
            is ConversationDetailViewAction.TrashMessage -> handleTrashMessage(action)
            is ConversationDetailViewAction.ArchiveMessage -> handleArchiveMessage(action)
            is ConversationDetailViewAction.MoveMessageToSpam -> handleMoveMessageToSpam(action)

            is ConversationDetailViewAction.ChangeVisibilityOfMessages -> handleChangeVisibilityOfMessages()

            is ConversationDetailViewAction.DeleteRequested,
            is ConversationDetailViewAction.DeleteDialogDismissed,
            is DismissBottomSheet,
            is MoveToDestinationSelected,
            is LabelAsToggleAction,
            is MessageBodyLinkClicked,
            is RequestScrollTo,
            is ScrollRequestCompleted,
            is ConversationDetailViewAction.ExpandOrCollapseMessageBody,
            is ConversationDetailViewAction.ShowEmbeddedImages,
            is ConversationDetailViewAction.LoadRemoteAndEmbeddedContent,
            is ConversationDetailViewAction.LoadRemoteContent,
            is ConversationDetailViewAction.ReportPhishingDismissed,
            is ConversationDetailViewAction.SwitchViewMode,
            is ConversationDetailViewAction.PrintRequested -> directlyHandleViewAction(action)
        }
    }

    fun loadEmbeddedImage(messageId: MessageId?, contentId: String) = messageId?.let {
        runBlocking {
            getEmbeddedImageAvoidDuplicatedExecution(
                userId = primaryUserId.first(),
                messageId = it,
                contentId = contentId,
                coroutineContext = viewModelScope.coroutineContext
            )
        }
    }

    private fun observePrivacySettings() {
        primaryUserId.flatMapLatest { userId ->
            observePrivacySettings(userId).mapLatest { either ->
                either.fold(
                    ifLeft = { Timber.e("Error getting Privacy Settings for user: $userId") },
                    ifRight = { privacySettings ->
                        mutableDetailState.emit(
                            mutableDetailState.value.copy(
                                requestLinkConfirmation = privacySettings.requestLinkConfirmation
                            )
                        )
                    }
                )
            }
        }.launchIn(observableFlowScope)
    }

    private fun observeConversationMetadata(conversationId: ConversationId) {
        primaryUserId.flatMapLatest { userId ->
            observeConversation(userId, conversationId)
                .mapLatest { either ->
                    either.fold(
                        ifLeft = {
                            if (it.isOfflineError()) {
                                ConversationDetailEvent.NoNetworkError
                            } else {
                                ConversationDetailEvent.ErrorLoadingConversation
                            }
                        },
                        ifRight = { ConversationDetailEvent.ConversationData(conversationMetadataMapper.toUiModel(it)) }
                    )
                }
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(observableFlowScope)
    }

    private fun observeConversationMessages(conversationId: ConversationId) {
        primaryUserId
            .flatMapLatest { userId ->
                combine(
                    observeContacts(userId),
                    observeConversationMessages(userId, conversationId).ignoreLocalErrors(),
                    observeConversationViewState()
                ) { contactsEither, messagesEither, conversationViewState ->
                    val contacts = contactsEither.getOrElse {
                        Timber.i("Failed getting contacts for displaying initials. Fallback to display name")
                        emptyList()
                    }
                    val conversationMessages = messagesEither.getOrElse {
                        return@combine ConversationDetailEvent.ErrorLoadingMessages
                    }
                    val messagesUiModels = buildMessagesUiModels(
                        userId = userId,
                        messages = conversationMessages.messages,
                        contacts = contacts,
                        currentViewState = conversationViewState
                    ).toImmutableList()

                    val initialScrollTo = initialScrollToMessageId
                        ?: conversationMessages.messageIdToOpen
                            .let { messageIdUiModelMapper.toUiModel(it) }
                    if (stateIsLoading() && allCollapsed(conversationViewState.messagesState)) {
                        ConversationDetailEvent.MessagesData(
                            messagesUiModels,
                            initialScrollTo,
                            filterByLocation,
                            conversationViewState.shouldHideMessagesBasedOnTrashFilter
                        )
                    } else {
                        val requestScrollTo = requestScrollToMessageId(conversationViewState.messagesState)
                        ConversationDetailEvent.MessagesData(
                            messagesUiModels,
                            requestScrollTo,
                            filterByLocation,
                            conversationViewState.shouldHideMessagesBasedOnTrashFilter
                        )
                    }
                }
            }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { event ->
                emitNewStateFrom(event)
            }
            .flowOn(ioDispatcher)
            .launchIn(observableFlowScope)
    }

    private fun stateIsLoading(): Boolean = state.value.messagesState == ConversationDetailsMessagesState.Loading

    private fun allCollapsed(viewState: Map<MessageId, InMemoryConversationStateRepository.MessageState>): Boolean =
        viewState.values.all { it == InMemoryConversationStateRepository.MessageState.Collapsed }

    private suspend fun buildMessagesUiModels(
        userId: UserId,
        messages: NonEmptyList<Message>,
        contacts: List<ContactMetadata.Contact>,
        currentViewState: InMemoryConversationStateRepository.MessagesState
    ): NonEmptyList<ConversationDetailMessageUiModel> {
        val messagesList = messages.map { message ->
            val existingMessageState = getExistingExpandedMessageUiState(message.messageId)

            when (val viewState = currentViewState.messagesState[message.messageId]) {
                is InMemoryConversationStateRepository.MessageState.Expanding ->
                    buildExpandingMessage(buildCollapsedMessage(message, contacts))

                is InMemoryConversationStateRepository.MessageState.Expanded -> {
                    buildExpandedMessage(
                        userId,
                        message,
                        existingMessageState,
                        contacts,
                        viewState.decryptedBody
                    )
                }

                else -> {
                    buildCollapsedMessage(message, contacts)
                }
            }
        }
        return messagesList
    }

    private fun getExistingExpandedMessageUiState(messageId: MessageId): ConversationDetailMessageUiModel.Expanded? {
        return when (val messagesState = state.value.messagesState) {
            is ConversationDetailsMessagesState.Data -> {
                messagesState.messages
                    .filterIsInstance<ConversationDetailMessageUiModel.Expanded>()
                    .firstOrNull { it.messageId.id == messageId.id }
            }

            else -> null
        }
    }

    private fun requestScrollToMessageId(
        conversationViewState: Map<MessageId, InMemoryConversationStateRepository.MessageState>
    ): MessageIdUiModel? {
        val expandedMessageIds = conversationViewState
            .filterValues { it is InMemoryConversationStateRepository.MessageState.Expanded }
            .keys

        val requestScrollTo = if (conversationViewState.size == 1 && expandedMessageIds.size == 1) {
            messageIdUiModelMapper.toUiModel(expandedMessageIds.first())
        } else {
            null
        }
        return requestScrollTo
    }

    private suspend fun buildCollapsedMessage(
        message: Message,
        contacts: List<ContactMetadata.Contact>
    ): ConversationDetailMessageUiModel.Collapsed = conversationMessageMapper.toUiModel(
        message,
        contacts
    )

    private fun buildExpandingMessage(
        collapsedMessage: ConversationDetailMessageUiModel.Collapsed
    ): ConversationDetailMessageUiModel.Expanding = conversationMessageMapper.toUiModel(
        collapsedMessage
    )

    private suspend fun buildExpandedMessage(
        userId: UserId,
        message: Message,
        existingMessageUiState: ConversationDetailMessageUiModel.Expanded?,
        contacts: List<ContactMetadata.Contact>,
        decryptedBody: DecryptedMessageBody
    ): ConversationDetailMessageUiModel.Expanded = conversationMessageMapper.toUiModel(
        userId,
        message,
        contacts,
        decryptedBody,
        existingMessageUiState
    )

    private fun observeBottomBarActions(conversationId: ConversationId) {
        primaryUserId.flatMapLatest { userId ->
            observeDetailActions(userId, conversationId).mapLatest { either ->
                either.fold(
                    ifLeft = { ConversationDetailEvent.ConversationBottomBarEvent(BottomBarEvent.ErrorLoadingActions) },
                    ifRight = { actions ->
                        val actionUiModels = actions.map { actionUiModelMapper.toUiModel(it) }.toImmutableList()
                        ConversationDetailEvent.ConversationBottomBarEvent(
                            BottomBarEvent.ShowAndUpdateActionsData(actionUiModels)
                        )
                    }
                )
            }
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(observableFlowScope)
    }

    private fun showMessageMoveToBottomSheet(
        initialEvent: ConversationDetailViewAction.RequestMessageMoveToBottomSheet
    ) {
        viewModelScope.launch {
            Timber.v("detail-actions: message move to bottom sheet requested")
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = filterByLocation ?: return@launch

            getMessageMoveToLocations(userId, labelId, listOf(initialEvent.messageId)).fold(
                ifLeft = {
                    Timber.e("detail-actions: failed to load the bottom-sheet actions: $it")
                },
                ifRight = { actions ->
                    Timber.v("detail-actions move to locations: $actions")
                    val event = actions.toConversationBottomSheetEvent(initialEvent.messageId)
                    emitNewStateFrom(event)
                }
            )
        }
    }

    private fun showMoveToBottomSheet(initialEvent: RequestMoveToBottomSheet) {
        viewModelScope.launch {
            Timber.v("detail-actions: move to bottom sheet requested")
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = filterByLocation ?: return@launch

            getConversationMoveToLocations(userId, labelId, listOf(conversationId)).fold(
                ifLeft = {
                    Timber.e("detail-actions: failed to load the bottom-sheet actions: $it")
                },
                ifRight = { actions ->
                    Timber.v("detail-actions move to locations: $actions")
                    val event = actions.toConversationBottomSheetEvent(null)
                    emitNewStateFrom(event)
                }
            )
        }
    }

    private fun List<MailLabel>.toConversationBottomSheetEvent(
        messageIdInConversation: MessageId?
    ): ConversationDetailEvent.ConversationBottomSheetEvent {
        val systemActions = this.filterIsInstance<MailLabel.System>()
        val customActions = this.filterIsInstance<MailLabel.Custom>()
        val mailLabels = MailLabels(systemActions, customActions, emptyList())

        return ConversationDetailEvent.ConversationBottomSheetEvent(
            MoveToBottomSheetState.MoveToBottomSheetEvent.ActionData(
                moveToDestinations = mailLabels.toUiModels().let {
                    it.folders + it.systemLabels
                }.toImmutableList(),
                messageIdInConversation = messageIdInConversation
            )
        )
    }

    private fun showContactActionsBottomSheetAndLoadData(action: RequestContactActionsBottomSheet) {
        viewModelScope.launch {
            emitNewStateFrom(action)

            val userId = primaryUserId.first()
            val contact = findContactByEmail(userId, action.participant.participantAddress)

            val event = ConversationDetailEvent.ConversationBottomSheetEvent(
                ContactActionsBottomSheetState.ContactActionsBottomSheetEvent.ActionData(
                    participant = Participant(
                        address = action.participant.participantAddress,
                        name = action.participant.participantName
                    ),
                    avatarUiModel = action.avatarUiModel,
                    contactId = contact?.id
                )
            )
            emitNewStateFrom(event)
        }
    }

    private fun showConversationLabelAsBottomSheet(initialEvent: ConversationDetailViewAction) {
        viewModelScope.launch {
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = filterByLocation ?: return@launch
            val labelAsData = getLabelAsBottomSheetData.forConversation(userId, labelId, conversationId)
            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(labelAsData))
        }
    }

    private fun showMessageLabelAsBottomSheet(
        initialEvent: ConversationDetailViewAction.RequestMessageLabelAsBottomSheet
    ) {
        viewModelScope.launch {
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = filterByLocation ?: return@launch
            val labelAsData = getLabelAsBottomSheetData.forMessage(userId, labelId, initialEvent.messageId)
            emitNewStateFrom(ConversationDetailEvent.MessageBottomSheetEvent(labelAsData))
        }
    }

    private fun onLabelAsConfirmed(operation: LabelAsConfirmed) {
        if (operation.messageId != null) {
            onMessageLabelAsConfirmed(operation.archiveSelected, operation.messageId)
        } else {
            onConversationLabelAsConfirmed(operation.archiveSelected)
        }
    }

    private fun onMessageLabelAsConfirmed(archiveSelected: Boolean, messageId: MessageId) {
        viewModelScope.launch {
            val userId = primaryUserId.first()

            val labelAsData =
                mutableDetailState.value.bottomSheetState?.contentState as? LabelAsBottomSheetState.Data
                    ?: throw IllegalStateException("BottomSheetState is not LabelAsBottomSheetState.Data")

            val operation =
                onMessageLabelAsConfirmed(
                    userId = userId,
                    messageId = messageId,
                    labelUiModelsWithSelectedState = labelAsData.labelUiModelsWithSelectedState,
                    archiveSelected = archiveSelected
                ).fold(
                    ifLeft = {
                        Timber.e("Relabel message failed: $it")
                        ConversationDetailEvent.ErrorLabelingConversation
                    },
                    ifRight = { LabelAsConfirmed(archiveSelected, messageId) }
                )
            emitNewStateFrom(operation)
        }
    }

    private fun onConversationLabelAsConfirmed(archiveSelected: Boolean) {
        viewModelScope.launch {
            val userId = primaryUserId.first()
            val messagesWithLabels = observeConversationMessages(userId, conversationId).first().onLeft {
                Timber.e("Error while observing conversation message when relabeling got confirmed: $it")
            }.getOrElse { null }

            if (messagesWithLabels == null) {
                Timber.e("Error while observing conversation message when relabeling got confirmed")
                return@launch
            }

            val labelAsData = mutableDetailState.value.bottomSheetState?.contentState as? LabelAsBottomSheetState.Data
                ?: throw IllegalStateException("BottomSheetState is not LabelAsBottomSheetState.Data")

            val updatedSelections = labelAsData.getLabelSelectionState()

            if (archiveSelected) {
                moveConversation(
                    userId = userId,
                    conversationId = conversationId,
                    labelId = SystemLabelId.Archive.labelId
                ).onLeft { Timber.e("Error while archiving conversation when relabeling got confirmed: $it") }
            }
            val operation = relabelConversation(
                userId = userId,
                conversationId = conversationId,
                currentSelections = LabelSelectionList(emptyList(), emptyList()),
                updatedSelections = updatedSelections
            ).fold(
                ifLeft = {
                    Timber.e("Error while relabeling conversation: $it")
                    ConversationDetailEvent.ErrorLabelingConversation
                },
                ifRight = { LabelAsConfirmed(archiveSelected, null) }
            )
            emitNewStateFrom(operation)
        }
    }

    private fun LabelAsBottomSheetState.Data.getLabelSelectionState(): LabelSelectionList {
        val selectedLabels = this.labelUiModelsWithSelectedState
            .filter { it.selectedState == LabelSelectedState.Selected }
            .map { it.labelUiModel.id.labelId }

        val partiallySelectedLabels = this.labelUiModelsWithSelectedState
            .filter { it.selectedState == LabelSelectedState.PartiallySelected }
            .map { it.labelUiModel.id.labelId }
        return LabelSelectionList(
            selectedLabels = selectedLabels,
            partiallySelectionLabels = partiallySelectedLabels
        )
    }

    private fun showMessageMoreActionsBottomSheet(
        initialEvent: ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet
    ) {
        viewModelScope.launch {
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = filterByLocation ?: return@launch
            val moreActions = getMoreActionsBottomSheetData.forMessage(userId, labelId, initialEvent.messageId)
                ?: return@launch
            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(moreActions))
        }
    }

    private fun showConversationMoreActionsBottomSheet(
        initialEvent: ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet
    ) {
        viewModelScope.launch {
            Timber.v("more-actions: requesting bottomsheet for convo")
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = filterByLocation ?: return@launch
            val moreActions = getMoreActionsBottomSheetData.forConversation(userId, labelId, conversationId)
                ?: return@launch

            Timber.v("more-actions: emitting convo bottom sheet event $moreActions")
            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(moreActions))
        }
    }


    private fun requireConversationId(): ConversationId {
        val conversationId = savedStateHandle.get<String>(ConversationDetailScreen.ConversationIdKey)
            ?: throw IllegalStateException("No Conversation id given")
        return ConversationId(conversationId)
    }

    private fun getInitialScrollToMessageId(): MessageIdUiModel? {
        val messageIdStr = savedStateHandle.get<String>(ConversationDetailScreen.ScrollToMessageIdKey)
        return messageIdStr?.let { if (it == "null") null else MessageIdUiModel(it) }
    }

    private fun getFilterByLocation(): LabelId? {
        val labelId = savedStateHandle.get<String>(ConversationDetailScreen.FilterByLocationKey)
        return labelId?.let { if (it == "null") null else LabelId(it) }
    }

    private suspend fun emitNewStateFrom(event: ConversationDetailOperation) {
        val newState: ConversationDetailState = reducer.newStateFrom(state.value.copy(), event)
        mutableDetailState.emit(newState)
    }

    private fun markAsUnread() {
        primaryUserId.mapLatest { userId ->
            markConversationAsUnread(userId, conversationId).fold(
                ifLeft = { ConversationDetailEvent.ErrorMarkingAsUnread },
                ifRight = { MarkUnread }
            )
        }.onEach(::emitNewStateFrom)
            .launchIn(viewModelScope)
    }

    private fun moveConversationToTrash() {
        primaryUserId.mapLatest { userId ->
            moveConversation(userId, conversationId, SystemLabelId.Trash.labelId).fold(
                ifLeft = { ConversationDetailEvent.ErrorMovingToTrash },
                ifRight = { Trash }
            )
        }.onEach(::emitNewStateFrom)
            .launchIn(viewModelScope)
    }

    private fun starConversation() {
        primaryUserId.mapLatest { userId ->
            starConversations(userId, listOf(conversationId)).fold(
                ifLeft = { ConversationDetailEvent.ErrorAddStar },
                ifRight = { Star }
            )
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun unStarConversation() {
        Timber.d("UnStar conversation clicked")
        primaryUserId.mapLatest { userId ->
            unStarConversations(userId, listOf(conversationId)).fold(
                ifLeft = { ConversationDetailEvent.ErrorRemoveStar },
                ifRight = { UnStar }
            )
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    @MissingRustApi
    // Need conversation model to expose "exclusive location" in a way that it can be mapped
    // back to deleteConversation as a labelId. Rust will perform checks to see "isDeletable"
    private fun handleDeleteConfirmed(action: ConversationDetailViewAction) {
        appScope.launch {
            val userId = primaryUserId.first()
            val conversation = observeConversation(userId, conversationId).first().getOrNull()
            if (conversation == null) {
                Timber.e("Failed to get conversation for deletion")
                emitNewStateFrom(ConversationDetailEvent.ErrorDeletingConversation)
                return@launch
            }

            val currentDeletableLabel: LabelId? = null

            if (currentDeletableLabel == null) {
                Timber.e("Failed to delete conversation: no applicable folder")
                emitNewStateFrom(ConversationDetailEvent.ErrorDeletingNoApplicableFolder)
                return@launch
            } else {
                // We manually cancel the Observer Scope since the following deletion calls cause all the observers
                // to emit, which could lead to race conditions as the observers re-insert the conversation and/or
                // the messages in the DB on late changes, making the entry still re-appear in the mailbox list.
                observableFlowScope.cancel()

                emitNewStateFrom(action)
                deleteConversations(userId, listOf(conversationId), currentDeletableLabel)
            }

        }
    }

    private fun directlyHandleViewAction(action: ConversationDetailViewAction) {
        viewModelScope.launch { emitNewStateFrom(action) }
    }

    private fun onMoveToDestinationConfirmed(mailLabelText: String, messageId: MessageId?) {
        if (messageId == null) {
            onConversationMoveToDestinationConfirmed(mailLabelText)
        } else {
            onMessageMoveToDestinationConfirmed(mailLabelText, messageId)
        }
    }

    private fun onConversationMoveToDestinationConfirmed(mailLabelText: String) {
        primaryUserId.mapLatest { userId ->
            val bottomSheetState = state.value.bottomSheetState?.contentState
            if (bottomSheetState is MoveToBottomSheetState.Data) {
                bottomSheetState.selected?.let { mailLabelUiModel ->
                    moveConversation(userId, conversationId, mailLabelUiModel.id.labelId).fold(
                        ifLeft = { ConversationDetailEvent.ErrorMovingConversation },
                        ifRight = { MoveToDestinationConfirmed(mailLabelText, null) }
                    )
                } ?: throw IllegalStateException("No destination selected")
            } else {
                ConversationDetailEvent.ErrorMovingConversation
            }
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun onMessageMoveToDestinationConfirmed(mailLabelText: String, messageId: MessageId) {
        primaryUserId.mapLatest { userId ->
            val bottomSheetState = state.value.bottomSheetState?.contentState
            if (bottomSheetState is MoveToBottomSheetState.Data) {
                bottomSheetState.selected?.let { mailLabelUiModel ->
                    moveMessage(userId, messageId, mailLabelUiModel.id.labelId).fold(
                        ifLeft = { ConversationDetailEvent.ErrorMovingMessage },
                        ifRight = { MoveToDestinationConfirmed(mailLabelText, messageId) }
                    )
                } ?: throw IllegalStateException("No destination selected")
            } else {
                ConversationDetailEvent.ErrorMovingMessage
            }
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun onExpandMessage(messageId: MessageIdUiModel) {
        viewModelScope.launch(ioDispatcher) {
            val domainMsgId = MessageId(messageId.id)
            setMessageViewState.expanding(domainMsgId)
            getDecryptedMessageBody(primaryUserId.first(), domainMsgId)
                .onRight {
                    observeAttachments(messageId, it.attachments)
                    markMessageAsRead(
                        primaryUserId.first(),
                        domainMsgId
                    )
                    setMessageViewState.expanded(domainMsgId, it)
                }
                .onLeft {
                    emitMessageBodyDecryptError(it, messageId)
                    setMessageViewState.collapsed(domainMsgId)
                }
                .getOrNull()
        }
    }

    private fun onCollapseMessage(messageId: MessageIdUiModel) {
        viewModelScope.launch { setMessageViewState.collapsed(MessageId(messageId.id)) }
    }

    private fun handleChangeVisibilityOfMessages() {
        viewModelScope.launch {
            setMessageViewState.switchTrashedMessagesFilter()
        }
    }

    private fun onDoNotAskLinkConfirmationChecked() {
        viewModelScope.launch { updateLinkConfirmationSetting(false) }
    }

    private suspend fun emitMessageBodyDecryptError(error: GetDecryptedMessageBodyError, messageId: MessageIdUiModel) {
        val errorState = when (error) {
            is GetDecryptedMessageBodyError.Data -> if (error.dataError.isOfflineError()) {
                ConversationDetailEvent.ErrorExpandingRetrievingMessageOffline(messageId)
            } else {
                ConversationDetailEvent.ErrorExpandingRetrieveMessageError(messageId)
            }

            is GetDecryptedMessageBodyError.Decryption ->
                ConversationDetailEvent.ErrorExpandingDecryptMessageError(messageId)
        }

        emitNewStateFrom(errorState)
    }

    private fun showAllAttachmentsForMessage(messageId: MessageIdUiModel) {
        val dataState = state.value.messagesState as? ConversationDetailsMessagesState.Data
        if (dataState == null) {
            Timber.e("Messages state is not data to perform show all attachments operation")
            return
        }
        dataState.messages.firstOrNull { it.messageId == messageId }
            ?.takeIf { it is ConversationDetailMessageUiModel.Expanded }
            ?.let { it as ConversationDetailMessageUiModel.Expanded }
            ?.let {
                val attachmentGroupUiModel = it.messageBodyUiModel.attachments
                val operation = ConversationDetailEvent.ShowAllAttachmentsForMessage(
                    messageId = messageId,
                    conversationDetailMessageUiModel = it.copy(
                        messageBodyUiModel = it.messageBodyUiModel.copy(
                            attachments = attachmentGroupUiModel?.copy(
                                limit = attachmentGroupUiModel.attachments.size
                            )
                        )
                    )
                )
                viewModelScope.launch { emitNewStateFrom(operation) }
            }
    }

    private suspend fun observeAttachments(messageId: MessageIdUiModel, attachments: List<MessageAttachment>) {
        val domainMsgId = MessageId(messageId.id)
        attachments.map { it.attachmentId }.filterNot { observedAttachments.contains(it) }.forEach { attachmentId ->
            primaryUserId.flatMapLatest { userId ->
                observedAttachments.add(attachmentId)
                observeMessageAttachmentStatus(userId, domainMsgId, attachmentId).mapLatest {
                    ConversationDetailEvent.AttachmentStatusChanged(messageId, attachmentId, it.status)
                }
            }.onEach { event ->
                emitNewStateFrom(event)
            }.launchIn(observableFlowScope)
        }
    }

    private fun onOpenAttachmentClicked(messageId: MessageIdUiModel, attachmentId: AttachmentId) {
        val domainMsgId = MessageId(messageId.id)
        viewModelScope.launch {
            if (isAttachmentDownloadInProgress().not()) {
                val userId = primaryUserId.first()
                getAttachmentIntentValues(userId, domainMsgId, attachmentId).fold(
                    ifLeft = {
                        Timber.d("Failed to download attachment: $it")
                        val event = when (it) {
                            is DataError.Local.OutOfMemory ->
                                ConversationDetailEvent.ErrorGettingAttachmentNotEnoughSpace

                            else -> ConversationDetailEvent.ErrorGettingAttachment
                        }
                        emitNewStateFrom(event)
                    },
                    ifRight = { emitNewStateFrom(ConversationDetailEvent.OpenAttachmentEvent(it)) }
                )
            } else {
                emitNewStateFrom(ConversationDetailEvent.ErrorAttachmentDownloadInProgress)
            }
        }
    }

    private suspend fun isAttachmentDownloadInProgress(): Boolean {
        val userId = primaryUserId.first()
        val messagesState = mutableDetailState.value.messagesState
        return if (messagesState is ConversationDetailsMessagesState.Data) {
            getDownloadingAttachmentsForMessages(userId, messagesState.messages.map { MessageId(it.messageId.id) })
                .isNotEmpty()
        } else false
    }

    private fun handleReportPhishingConfirmed(action: ConversationDetailViewAction.ReportPhishingConfirmed) {
        viewModelScope.launch {
            reportPhishingMessage(primaryUserId.first(), action.messageId)
                .onLeft { Timber.e("Error while reporting phishing message: $it") }
            emitNewStateFrom(action)
        }
    }

    private fun handleReportPhishing(action: ConversationDetailViewAction.ReportPhishing) {
        viewModelScope.launch {
            val operation = when (networkManager.networkStatus) {
                NetworkStatus.Disconnected -> ConversationDetailEvent.ReportPhishingRequested(
                    messageId = action.messageId,
                    isOffline = true
                )

                else -> ConversationDetailEvent.ReportPhishingRequested(messageId = action.messageId, isOffline = false)
            }
            emitNewStateFrom(operation)
        }
    }

    private fun handleOpenInProtonCalendar(action: ConversationDetailViewAction.OpenInProtonCalendar) {
        viewModelScope.launch {
            val isProtonCalendarInstalled = isProtonCalendarInstalled()
            if (isProtonCalendarInstalled) {
                val dataState = mutableDetailState.value.messagesState as? ConversationDetailsMessagesState.Data
                dataState?.messages?.mapNotNull { it as? ConversationDetailMessageUiModel.Expanded }
                    ?.first { it.messageId.id == action.messageId.id }
                    ?.let { messageUiModel -> handleOpenInProtonCalendar(messageUiModel) }
            } else {
                val intent = OpenProtonCalendarIntentValues.OpenProtonCalendarOnPlayStore
                emitNewStateFrom(ConversationDetailEvent.HandleOpenProtonCalendarRequest(intent))
            }
        }
    }

    @MissingRustApi
    // AddressId not being exposed through with rust Message (for both Message and Participants) resulting in the client
    // not having enough info to determine with "recipient" is the current user for which to open the invite.
    // Currently getting "toRecipients.first()" to keep the API unchanged, this won't work in several cases.
    private suspend fun handleOpenInProtonCalendar(messageUiModel: ConversationDetailMessageUiModel.Expanded) {
        val sender = messageUiModel.messageDetailHeaderUiModel.sender.participantAddress
        val recipient = messageUiModel.messageDetailHeaderUiModel.toRecipients.first().participantAddress
        val firstCalendarAttachment = messageUiModel.messageBodyUiModel
            .attachments
            ?.attachments
            ?.firstOrNull { uiModel -> uiModel.mimeType.split(";").any { it == "text/calendar" } }

        if (firstCalendarAttachment == null) return

        getAttachmentIntentValues(
            userId = primaryUserId.first(),
            messageId = MessageId(messageUiModel.messageId.id),
            attachmentId = AttachmentId(firstCalendarAttachment.attachmentId)
        ).fold(
            ifLeft = { Timber.d("Failed to download attachment: $it") },
            ifRight = {
                val intent = OpenProtonCalendarIntentValues.OpenIcsInProtonCalendar(it.uri, sender, recipient)
                emitNewStateFrom(ConversationDetailEvent.HandleOpenProtonCalendarRequest(intent))
            }
        )
    }

    private fun handlePrint(context: Context, messageId: MessageId) {
        val conversationState = state.value.conversationState
        val messagesState = state.value.messagesState
        if (
            conversationState is ConversationDetailMetadataState.Data &&
            messagesState is ConversationDetailsMessagesState.Data
        ) {
            messagesState.messages.find { it.messageId.id == messageId.id }?.let {
                if (it is ConversationDetailMessageUiModel.Expanded) {
                    printMessage(
                        context,
                        conversationState.conversationUiModel.subject,
                        it.messageDetailHeaderUiModel,
                        it.messageBodyUiModel,
                        it.expandCollapseMode,
                        this@ConversationDetailViewModel::loadEmbeddedImage
                    )
                }
            }
        }
    }

    private fun handleMarkMessageUnread(action: ConversationDetailViewAction.MarkMessageUnread) {
        viewModelScope.launch {
            markMessageAsUnread(primaryUserId.first(), action.messageId)
            onCollapseMessage(MessageIdUiModel(action.messageId.id))
            emitNewStateFrom(action)
        }
    }

    private fun handleTrashMessage(action: ConversationDetailViewAction.TrashMessage) {
        viewModelScope.launch {
            moveMessage(primaryUserId.first(), action.messageId, SystemLabelId.Trash.labelId)
            emitNewStateFrom(action)
        }
    }

    private fun handleArchiveMessage(action: ConversationDetailViewAction.ArchiveMessage) {
        viewModelScope.launch {
            moveMessage(primaryUserId.first(), action.messageId, SystemLabelId.Archive.labelId)
            emitNewStateFrom(action)
        }
    }

    private fun handleMoveMessageToSpam(action: ConversationDetailViewAction.MoveMessageToSpam) {
        viewModelScope.launch {
            moveMessage(primaryUserId.first(), action.messageId, SystemLabelId.Spam.labelId)
            emitNewStateFrom(action)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public override fun onCleared() {
        super.onCleared()
        observableFlowScope.cancel()
    }

    companion object {

        val initialState = ConversationDetailState.Loading
    }
}

/**
 * Filters [DataError.Local] from messages flow, as we don't want to show them to the user, because the fetch is being
 *  done on the conversation flow.
 */
private fun Flow<Either<DataError, ConversationMessages>>.ignoreLocalErrors():
    Flow<Either<DataError, ConversationMessages>> =
    filter { either ->
        either.fold(
            ifLeft = { error -> error !is DataError.Local },
            ifRight = { true }
        )
    }
