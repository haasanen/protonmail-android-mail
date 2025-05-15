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

import java.util.concurrent.CopyOnWriteArrayList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.isOfflineError
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcontact.domain.usecase.FindContactByEmail
import ch.protonmail.android.mailconversation.domain.usecase.DeleteConversations
import ch.protonmail.android.mailconversation.domain.usecase.ObserveConversation
import ch.protonmail.android.mailconversation.domain.usecase.StarConversations
import ch.protonmail.android.mailconversation.domain.usecase.UnStarConversations
import ch.protonmail.android.maildetail.domain.model.OpenProtonCalendarIntentValues
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.maildetail.domain.usecase.GetAttachmentIntentValues
import ch.protonmail.android.maildetail.domain.usecase.GetDownloadingAttachmentsForMessages
import ch.protonmail.android.maildetail.domain.usecase.IsProtonCalendarInstalled
import ch.protonmail.android.maildetail.domain.usecase.MarkConversationAsRead
import ch.protonmail.android.maildetail.domain.usecase.MarkConversationAsUnread
import ch.protonmail.android.maildetail.domain.usecase.MarkMessageAsLegitimate
import ch.protonmail.android.maildetail.domain.usecase.MarkMessageAsRead
import ch.protonmail.android.maildetail.domain.usecase.MarkMessageAsUnread
import ch.protonmail.android.maildetail.domain.usecase.MessageViewStateCache
import ch.protonmail.android.maildetail.domain.usecase.MoveConversation
import ch.protonmail.android.maildetail.domain.usecase.MoveMessage
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationMessages
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationViewState
import ch.protonmail.android.maildetail.domain.usecase.ObserveDetailBottomBarActions
import ch.protonmail.android.maildetail.domain.usecase.ReportPhishingMessage
import ch.protonmail.android.maildetail.domain.usecase.UnblockSender
import ch.protonmail.android.maildetail.presentation.mapper.ConversationDetailMessageUiModelMapper
import ch.protonmail.android.maildetail.presentation.mapper.ConversationDetailMetadataUiModelMapper
import ch.protonmail.android.maildetail.presentation.mapper.MessageIdUiModelMapper
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.CollapseMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.DoNotAskLinkConfirmationAgain
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ExpandMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MarkUnread
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MessageBodyLinkClicked
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MoveToTrash
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestScrollTo
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ScrollRequestCompleted
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ShowAllAttachmentsForMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.Star
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.UnStar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailsMessagesState
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.reducer.ConversationDetailReducer
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetailScreen
import ch.protonmail.android.maildetail.presentation.usecase.GetEmbeddedImageAvoidDuplicatedExecution
import ch.protonmail.android.maildetail.presentation.usecase.GetMessagesInSameExclusiveLocation
import ch.protonmail.android.maildetail.presentation.usecase.GetMoreActionsBottomSheetData
import ch.protonmail.android.maildetail.presentation.usecase.ObservePrimaryUserAddress
import ch.protonmail.android.mailfeatureflags.domain.annotation.ComposerEnabled
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsItemId
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToItemId
import ch.protonmail.android.maillabel.presentation.model.MailLabelText
import ch.protonmail.android.mailmessage.domain.mapper.MessageBodyTransformationsMapper
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.AvatarImageState
import ch.protonmail.android.mailmessage.domain.model.AvatarImageStates
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetDecryptedMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformationsOverride
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.domain.usecase.DeleteMessages
import ch.protonmail.android.mailmessage.domain.usecase.GetDecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.usecase.LoadAvatarImage
import ch.protonmail.android.mailmessage.domain.usecase.ObserveAvatarImageStates
import ch.protonmail.android.mailmessage.domain.usecase.StarMessages
import ch.protonmail.android.mailmessage.domain.usecase.UnStarMessages
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.presentation.model.attachment.isExpandable
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.ContactActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MoveToBottomSheetState
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.usecase.privacy.ObservePrivacySettings
import ch.protonmail.android.mailsettings.domain.usecase.privacy.UpdateLinkConfirmationSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.NetworkManager
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
    private val markConversationAsRead: MarkConversationAsRead,
    private val markConversationAsUnread: MarkConversationAsUnread,
    private val moveConversation: MoveConversation,
    private val deleteConversations: DeleteConversations,
    private val observeConversation: ObserveConversation,
    private val observeConversationMessages: ObserveConversationMessages,
    private val observeDetailActions: ObserveDetailBottomBarActions,
    private val getDownloadingAttachmentsForMessages: GetDownloadingAttachmentsForMessages,
    private val reducer: ConversationDetailReducer,
    private val starConversations: StarConversations,
    private val unStarConversations: UnStarConversations,
    private val starMessages: StarMessages,
    private val unStarMessages: UnStarMessages,
    private val savedStateHandle: SavedStateHandle,
    private val getDecryptedMessageBody: GetDecryptedMessageBody,
    private val markMessageAsRead: MarkMessageAsRead,
    private val messageViewStateCache: MessageViewStateCache,
    private val observeConversationViewState: ObserveConversationViewState,
    private val getAttachmentIntentValues: GetAttachmentIntentValues,
    private val getEmbeddedImageAvoidDuplicatedExecution: GetEmbeddedImageAvoidDuplicatedExecution,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val observePrivacySettings: ObservePrivacySettings,
    private val updateLinkConfirmationSetting: UpdateLinkConfirmationSetting,
    private val reportPhishingMessage: ReportPhishingMessage,
    private val isProtonCalendarInstalled: IsProtonCalendarInstalled,
    private val networkManager: NetworkManager,
    private val markMessageAsUnread: MarkMessageAsUnread,
    private val findContactByEmail: FindContactByEmail,
    private val getMoreActionsBottomSheetData: GetMoreActionsBottomSheetData,
    private val moveMessage: MoveMessage,
    private val deleteMessages: DeleteMessages,
    private val observePrimaryUserAddress: ObservePrimaryUserAddress,
    private val loadAvatarImage: LoadAvatarImage,
    private val observeAvatarImageStates: ObserveAvatarImageStates,
    private val getMessagesInSameExclusiveLocation: GetMessagesInSameExclusiveLocation,
    private val markMessageAsLegitimate: MarkMessageAsLegitimate,
    private val unblockSender: UnblockSender,
    @ComposerEnabled val isComposerEnabled: Flow<Boolean>
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId()
        .stateIn(
            viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        .filterNotNull()

    private val mutableDetailState = MutableStateFlow(initialState)
    private val conversationId = requireConversationId()
    private val initialScrollToMessageId = getInitialScrollToMessageId()
    private val openedFromLocation = getOpenedFromLocation()
    private val attachmentsState = MutableStateFlow<Map<MessageId, List<AttachmentMetadata>>>(emptyMap())

    val state: StateFlow<ConversationDetailState> = mutableDetailState.asStateFlow()

    private val jobs = CopyOnWriteArrayList<Job>()

    init {
        Timber.d("Open detail screen for conversation ID: $conversationId")
        setupObservers()
    }

    private fun setupObservers() {
        jobs.addAll(
            listOf(
                observeConversationMetadata(conversationId),
                observeConversationMessages(conversationId),
                observeBottomBarActions(conversationId),
                observePrivacySettings(),
                observeAttachments()
            )
        )
    }

    private suspend fun stopAllJobs() {
        jobs.forEach { it.cancelAndJoin() }
        jobs.clear()
    }

    private suspend fun restartAllJobs() {
        stopAllJobs()
        setupObservers()
    }

    @Suppress("LongMethod", "ComplexMethod")
    fun submit(action: ConversationDetailViewAction) {
        when (action) {
            is Star -> starConversation()
            is UnStar -> unStarConversation()
            is ConversationDetailViewAction.MarkRead -> markAsRead()
            is MarkUnread -> markAsUnread()
            is MoveToTrash -> moveConversationToTrash()
            is ConversationDetailViewAction.MoveToArchive -> moveConversationToArchive()
            is ConversationDetailViewAction.MoveToSpam -> moveConversationToSpam()
            is ConversationDetailViewAction.DeleteConfirmed -> handleDeleteConfirmed(action)
            is ConversationDetailViewAction.DeleteMessageConfirmed -> handleDeleteMessageConfirmed(action)
            is ConversationDetailViewAction.RequestConversationMoveToBottomSheet ->
                requestConversationMoveToBottomSheet(action)

            is ConversationDetailViewAction.MoveToCompleted -> handleMoveToCompleted(action)
            is ConversationDetailViewAction.MoveToInbox -> moveConversationToInbox()
            is ConversationDetailViewAction.RequestConversationLabelAsBottomSheet ->
                requestConversationLabelAsBottomSheet(action)

            is ConversationDetailViewAction.RequestContactActionsBottomSheet ->
                showContactActionsBottomSheetAndLoadData(action)

            is ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet ->
                showMessageMoreActionsBottomSheet(action)

            is ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet ->
                showConversationMoreActionsBottomSheet(action)

            is ConversationDetailViewAction.RequestMessageLabelAsBottomSheet ->
                requestMessageLabelAsBottomSheet(action)

            is ConversationDetailViewAction.RequestMessageMoveToBottomSheet ->
                requestMessageMoveToBottomSheet(action)

            is ConversationDetailViewAction.LabelAsCompleted -> handleLabelAsCompleted(action)

            is ExpandMessage -> onExpandMessage(action.messageId)
            is CollapseMessage -> onCollapseMessage(action.messageId)
            is DoNotAskLinkConfirmationAgain -> onDoNotAskLinkConfirmationChecked()
            is ShowAllAttachmentsForMessage -> showAllAttachmentsForMessage(action.messageId)
            is ConversationDetailViewAction.OnAttachmentClicked -> {
                onOpenAttachmentClicked(action.attachmentId)
            }

            is ConversationDetailViewAction.ExpandOrCollapseAttachmentList -> {
                handleExpandOrCollapseAttachmentList(action.messageId)
            }

            is ConversationDetailViewAction.ReportPhishingConfirmed -> handleReportPhishingConfirmed(action)
            is ConversationDetailViewAction.OpenInProtonCalendar -> handleOpenInProtonCalendar(action)
            is ConversationDetailViewAction.MarkMessageUnread -> handleMarkMessageUnread(action)
            is ConversationDetailViewAction.MoveMessage -> handleMoveMessage(action)

            is ConversationDetailViewAction.StarMessage -> handleStarMessage(action)
            is ConversationDetailViewAction.UnStarMessage -> handleUnStarMessage(action)

            is ConversationDetailViewAction.ChangeVisibilityOfMessages -> handleChangeVisibilityOfMessages()

            is ConversationDetailViewAction.DeleteRequested,
            is ConversationDetailViewAction.DeleteDialogDismissed,
            is ConversationDetailViewAction.DeleteMessageRequested,
            is ConversationDetailViewAction.DismissBottomSheet,
            is MessageBodyLinkClicked,
            is RequestScrollTo,
            is ScrollRequestCompleted,
            is ConversationDetailViewAction.ReportPhishing,
            is ConversationDetailViewAction.ReportPhishingDismissed,
            is ConversationDetailViewAction.SwitchViewMode -> directlyHandleViewAction(action)

            is ConversationDetailViewAction.OnAvatarImageLoadRequested ->
                handleOnAvatarImageLoadRequested(action.avatar)

            is ConversationDetailViewAction.ShowEmbeddedImages -> viewModelScope.launch {
                setOrRefreshMessageBody(
                    messageId = action.messageId,
                    override = MessageBodyTransformationsOverride.LoadEmbeddedImages
                )
            }

            is ConversationDetailViewAction.ExpandOrCollapseMessageBody -> viewModelScope.launch {
                setOrRefreshMessageBody(
                    messageId = action.messageId,
                    override = MessageBodyTransformationsOverride.ToggleQuotedText
                )
            }

            is ConversationDetailViewAction.LoadRemoteAndEmbeddedContent -> viewModelScope.launch {
                setOrRefreshMessageBody(
                    messageId = action.messageId,
                    override = MessageBodyTransformationsOverride.LoadRemoteContentAndEmbeddedImages
                )
            }

            is ConversationDetailViewAction.LoadRemoteContent -> viewModelScope.launch {
                setOrRefreshMessageBody(
                    messageId = action.messageId,
                    override = MessageBodyTransformationsOverride.LoadRemoteContent
                )
            }

            is ConversationDetailViewAction.MarkMessageAsLegitimate -> handleMarkMessageAsLegitimate(action.messageId)

            is ConversationDetailViewAction.UnblockSender -> handleUnblockSender(action.messageId, action.email)
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

    private fun observePrivacySettings() = primaryUserId.flatMapLatest { userId ->
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
    }.launchIn(viewModelScope)

    private fun observeConversationMetadata(conversationId: ConversationId) = primaryUserId.flatMapLatest { userId ->
        observeConversation(userId, conversationId, openedFromLocation)
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
    }
        .onEach { event ->
            emitNewStateFrom(event)
        }
        .launchIn(viewModelScope)

    private fun observeConversationMessages(conversationId: ConversationId) = primaryUserId.flatMapLatest { userId ->
        combine(
            observeConversationMessages(userId, conversationId, openedFromLocation).ignoreLocalErrors(),
            observeConversationViewState(),
            observePrimaryUserAddress(),
            observeAvatarImageStates()
        ) { messagesEither, conversationViewState, primaryUserAddress, avatarImageStates ->
            val conversationMessages = messagesEither.getOrElse {
                return@combine if (it.isOfflineError()) {
                    ConversationDetailEvent.NoNetworkError
                } else {
                    ConversationDetailEvent.ErrorLoadingMessages
                }
            }
            val messagesUiModels = buildMessagesUiModels(
                messages = conversationMessages.messages,
                primaryUserAddress = primaryUserAddress,
                currentViewState = conversationViewState,
                avatarImageStates = avatarImageStates
            ).toImmutableList()

            val initialScrollTo = initialScrollToMessageId
                ?: conversationMessages.messageIdToOpen
                    .let { messageIdUiModelMapper.toUiModel(it) }
            if (stateIsLoading() && allCollapsed(conversationViewState.messagesState)) {
                ConversationDetailEvent.MessagesData(
                    messagesUiModels,
                    initialScrollTo,
                    openedFromLocation,
                    conversationViewState.shouldHideMessagesBasedOnTrashFilter
                )
            } else {
                val requestScrollTo = requestScrollToMessageId(conversationViewState.messagesState)
                ConversationDetailEvent.MessagesData(
                    messagesUiModels,
                    requestScrollTo,
                    openedFromLocation,
                    conversationViewState.shouldHideMessagesBasedOnTrashFilter
                )
            }
        }
    }
        .filterNotNull()
        .distinctUntilChanged()
        .flowOn(ioDispatcher)
        .onEach { event ->
            emitNewStateFrom(event)
        }
        .launchIn(viewModelScope)

    private fun stateIsLoading(): Boolean = state.value.messagesState == ConversationDetailsMessagesState.Loading

    private fun allCollapsed(viewState: Map<MessageId, InMemoryConversationStateRepository.MessageState>): Boolean =
        viewState.values.all { it == InMemoryConversationStateRepository.MessageState.Collapsed }

    private fun isSingleMessageConversation(): Boolean {
        return when (val messagesState = state.value.messagesState) {
            is ConversationDetailsMessagesState.Data -> {
                messagesState.messages.size == 1
            }

            else -> false
        }
    }

    private suspend fun buildMessagesUiModels(
        messages: NonEmptyList<Message>,
        primaryUserAddress: String?,
        currentViewState: InMemoryConversationStateRepository.MessagesState,
        avatarImageStates: AvatarImageStates
    ): NonEmptyList<ConversationDetailMessageUiModel> {
        val messagesList = messages.map { message ->
            val avatarImageState = avatarImageStates.getStateForAddress(message.sender.address)
            when (val viewState = currentViewState.messagesState[message.messageId]) {
                is InMemoryConversationStateRepository.MessageState.Expanding ->
                    buildExpandingMessage(
                        buildCollapsedMessage(message, avatarImageState, primaryUserAddress)
                    )

                is InMemoryConversationStateRepository.MessageState.Expanded -> {
                    buildExpandedMessage(
                        message,
                        avatarImageState,
                        primaryUserAddress,
                        viewState.decryptedBody
                    )
                }

                else -> {
                    buildCollapsedMessage(message, avatarImageState, primaryUserAddress)
                }
            }
        }
        return messagesList
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

    private fun buildCollapsedMessage(
        message: Message,
        avatarImageState: AvatarImageState,
        primaryUserAddress: String?
    ): ConversationDetailMessageUiModel.Collapsed = conversationMessageMapper.toUiModel(
        message = message,
        primaryUserAddress = primaryUserAddress,
        avatarImageState = avatarImageState
    )

    private fun buildExpandingMessage(
        collapsedMessage: ConversationDetailMessageUiModel.Collapsed
    ): ConversationDetailMessageUiModel.Expanding = conversationMessageMapper.toUiModel(
        collapsedMessage
    )

    private suspend fun buildExpandedMessage(
        message: Message,
        avatarImageState: AvatarImageState,
        primaryUserAddress: String?,
        decryptedBody: DecryptedMessageBody
    ): ConversationDetailMessageUiModel.Expanded = conversationMessageMapper.toUiModel(
        message,
        avatarImageState,
        primaryUserAddress,
        decryptedBody
    )

    private fun observeBottomBarActions(conversationId: ConversationId) = primaryUserId.flatMapLatest { userId ->
        val errorEvent = ConversationDetailEvent.ConversationBottomBarEvent(BottomBarEvent.ErrorLoadingActions)
        val labelId = openedFromLocation

        return@flatMapLatest observeDetailActions(userId, labelId, conversationId).mapLatest { either ->
            either.fold(
                ifLeft = { errorEvent },
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
    }.launchIn(viewModelScope)


    private fun showContactActionsBottomSheetAndLoadData(
        action: ConversationDetailViewAction.RequestContactActionsBottomSheet
    ) {
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

    private fun requestConversationMoveToBottomSheet(
        operation: ConversationDetailViewAction.RequestConversationMoveToBottomSheet
    ) {
        viewModelScope.launch {
            emitNewStateFrom(operation)

            val event = MoveToBottomSheetState.MoveToBottomSheetEvent.Ready(
                userId = primaryUserId.first(),
                currentLabel = openedFromLocation,
                itemIds = listOf(MoveToItemId(conversationId.id)),
                entryPoint = MoveToBottomSheetEntryPoint.Conversation
            )

            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(event))
        }
    }

    private fun requestMessageMoveToBottomSheet(
        operation: ConversationDetailViewAction.RequestMessageMoveToBottomSheet
    ) {
        viewModelScope.launch {
            emitNewStateFrom(operation)

            val userId = primaryUserId.first()
            val isLastMessageInLocation = isLastMessageInLocation(
                userId, conversationId,
                operation.messageId,
                openedFromLocation
            )

            val event = MoveToBottomSheetState.MoveToBottomSheetEvent.Ready(
                userId = primaryUserId.first(),
                currentLabel = openedFromLocation,
                itemIds = listOf(MoveToItemId(operation.messageId.id)),
                entryPoint = MoveToBottomSheetEntryPoint.Message(operation.messageId, isLastMessageInLocation)
            )

            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(event))
        }
    }

    private fun requestConversationLabelAsBottomSheet(
        operation: ConversationDetailViewAction.RequestConversationLabelAsBottomSheet
    ) {
        viewModelScope.launch {
            emitNewStateFrom(operation)

            val event = LabelAsBottomSheetState.LabelAsBottomSheetEvent.Ready(
                userId = primaryUserId.first(),
                currentLabel = openedFromLocation,
                itemIds = listOf(LabelAsItemId(conversationId.id)),
                entryPoint = LabelAsBottomSheetEntryPoint.Conversation
            )

            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(event))
        }
    }

    private fun requestMessageLabelAsBottomSheet(
        operation: ConversationDetailViewAction.RequestMessageLabelAsBottomSheet
    ) {
        viewModelScope.launch {
            val event = LabelAsBottomSheetState.LabelAsBottomSheetEvent.Ready(
                userId = primaryUserId.first(),
                currentLabel = openedFromLocation,
                itemIds = listOf(LabelAsItemId(operation.messageId.id)),
                entryPoint = LabelAsBottomSheetEntryPoint.Message(operation.messageId)
            )

            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(event))
        }
    }

    private fun handleLabelAsCompleted(operation: ConversationDetailViewAction.LabelAsCompleted) {
        viewModelScope.launch {
            val event = if (operation.wasArchived) {
                ConversationDetailEvent.ExitScreenWithMessage(operation)
            } else {
                operation
            }

            emitNewStateFrom(event)
        }
    }

    private fun handleMoveToCompleted(operation: ConversationDetailViewAction.MoveToCompleted) {
        viewModelScope.launch {
            val shouldExit = when (val entryPoint = operation.entryPoint) {
                is MoveToBottomSheetEntryPoint.Message -> entryPoint.isLastInCurrentLocation
                is MoveToBottomSheetEntryPoint.Conversation -> true
                else -> false
            }

            val event = if (shouldExit) ConversationDetailEvent.ExitScreenWithMessage(operation) else operation

            emitNewStateFrom(event)
        }
    }

    private fun showMessageMoreActionsBottomSheet(
        initialEvent: ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet
    ) {
        viewModelScope.launch {
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = openedFromLocation
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
            val labelId = openedFromLocation
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

    private fun getOpenedFromLocation(): LabelId {
        val labelId = savedStateHandle.get<String>(ConversationDetailScreen.OpenedFromLocationKey)
            ?: throw IllegalStateException("No opened from label id given")
        return LabelId(labelId)
    }

    private suspend fun emitNewStateFrom(event: ConversationDetailOperation) {
        val newState = reducer.newStateFrom(state.value, event)
        mutableDetailState.update { newState }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorMarkingAsRead,
                onRight = ConversationDetailEvent.ExitScreen
            ) { userId ->
                markConversationAsRead(userId, openedFromLocation, conversationId)
            }
        }
    }

    private fun markAsUnread() {
        viewModelScope.launch {
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorMarkingAsUnread,
                onRight = ConversationDetailEvent.ExitScreen
            ) { userId ->
                markConversationAsUnread(userId, openedFromLocation, conversationId)
            }
        }
    }

    private fun moveConversationToTrash() {
        viewModelScope.launch {
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorMovingToTrash,
                onRight = ConversationDetailEvent.ExitScreenWithMessage(MoveToTrash)
            ) { userId ->
                moveConversation(userId, conversationId, SystemLabelId.Trash)
            }
        }
    }

    private fun moveConversationToSpam() {
        viewModelScope.launch {
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorMovingConversation,
                onRight = ConversationDetailEvent.ExitScreenWithMessage(ConversationDetailViewAction.MoveToSpam)
            ) { userId ->
                moveConversation(userId, conversationId, SystemLabelId.Spam)
            }
        }
    }

    private fun moveConversationToArchive() {
        viewModelScope.launch {
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorMovingConversation,
                onRight = ConversationDetailEvent.ExitScreenWithMessage(
                    ConversationDetailViewAction.MoveToArchive
                )
            ) { userId ->
                moveConversation(userId, conversationId, SystemLabelId.Archive)
            }
        }
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
        primaryUserId.mapLatest { userId ->
            unStarConversations(userId, listOf(conversationId)).fold(
                ifLeft = { ConversationDetailEvent.ErrorRemoveStar },
                ifRight = { UnStar }
            )
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun moveConversationToInbox() {
        viewModelScope.launch {
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorMovingConversation,
                onRight = ConversationDetailViewAction.MoveToInbox
            ) { userId ->
                moveConversation(userId, conversationId, SystemLabelId.Inbox)
            }
        }
    }

    private fun handleDeleteMessageConfirmed(action: ConversationDetailViewAction.DeleteMessageConfirmed) {
        viewModelScope.launch {
            emitNewStateFrom(action)
            val currentLabelId = openedFromLocation

            deleteMessages(primaryUserId.first(), listOf(action.messageId), currentLabelId)
        }
    }

    private fun handleDeleteConfirmed(action: ConversationDetailViewAction) {
        viewModelScope.launch {
            // We manually cancel the observations since the following deletion calls cause all the observers
            // to emit, which could lead to race conditions as the observers re-insert the conversation and/or
            // the messages in the DB on late changes, making the entry still re-appear in the mailbox list.
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorDeletingConversation,
                onRight = ConversationDetailEvent.ExitScreenWithMessage(action)
            ) { _ ->
                deleteConversations(primaryUserId.first(), listOf(conversationId))
            }
        }
    }

    private fun directlyHandleViewAction(action: ConversationDetailViewAction) {
        viewModelScope.launch { emitNewStateFrom(action) }
    }

    private fun onExpandMessage(messageId: MessageIdUiModel) {
        viewModelScope.launch(ioDispatcher) {
            val domainMsgId = MessageId(messageId.id)
            messageViewStateCache.setExpanding(domainMsgId)
            setOrRefreshMessageBody(messageId)
        }
    }

    private suspend fun setOrRefreshMessageBody(
        messageId: MessageIdUiModel,
        override: MessageBodyTransformationsOverride? = null
    ) {
        val domainMsgId = MessageId(messageId.id)

        val currentTransformations = messageViewStateCache.getTransformations(domainMsgId)
            ?: MessageBodyTransformations.MessageDetailsDefaults

        val newTransformations = MessageBodyTransformationsMapper.applyOverride(currentTransformations, override)

        processMessageBody(primaryUserId.first(), domainMsgId, messageId, newTransformations)
    }

    private suspend fun processMessageBody(
        userId: UserId,
        domainMsgId: MessageId,
        uiMessageId: MessageIdUiModel,
        transformations: MessageBodyTransformations
    ) {
        getDecryptedMessageBody(userId, domainMsgId, transformations)
            .onRight { message ->
                messageViewStateCache.setExpanded(domainMsgId, message)
                messageViewStateCache.setTransformations(domainMsgId, transformations)

                if (message.attachments.isNotEmpty()) {
                    updateObservedAttachments(mapOf(domainMsgId to message.attachments))
                }

                if (message.isUnread) {
                    markMessageAsRead(userId, domainMsgId)
                }
            }
            .onLeft { error ->
                emitMessageBodyDecryptError(error, uiMessageId)
                messageViewStateCache.setCollapsed(domainMsgId)
            }
    }

    private fun onCollapseMessage(messageId: MessageIdUiModel) {
        viewModelScope.launch {
            messageViewStateCache.setCollapsed(MessageId(messageId.id))
            removeObservedAttachments(MessageId(messageId.id))
        }
    }

    private fun handleChangeVisibilityOfMessages() {
        viewModelScope.launch {
            messageViewStateCache.switchTrashedMessagesFilter()
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

    private fun handleExpandOrCollapseAttachmentList(messageId: MessageIdUiModel) {
        val dataState = state.value.messagesState as? ConversationDetailsMessagesState.Data
        if (dataState == null) {
            Timber.e("Messages state is not data to perform expand or collapse attachments")
            return
        }
        dataState.messages.firstOrNull { it.messageId == messageId }
            ?.takeIf { it is ConversationDetailMessageUiModel.Expanded }
            ?.let { it as ConversationDetailMessageUiModel.Expanded }
            ?.let {
                val attachmentGroupUiModel = it.messageBodyUiModel.attachments
                attachmentGroupUiModel?.let {
                    val expandCollapseMode = when {
                        attachmentGroupUiModel.isExpandable().not() -> AttachmentListExpandCollapseMode.NotApplicable
                        attachmentGroupUiModel.expandCollapseMode == AttachmentListExpandCollapseMode.Expanded ->
                            AttachmentListExpandCollapseMode.Collapsed

                        else -> AttachmentListExpandCollapseMode.Expanded
                    }
                    val operation = ConversationDetailEvent.AttachmentListExpandCollapseModeChanged(
                        messageId = messageId,
                        expandCollapseMode = expandCollapseMode
                    )
                    viewModelScope.launch { emitNewStateFrom(operation) }
                }
            }
    }

    private fun observeAttachments() = primaryUserId.flatMapLatest { userId ->
        attachmentsState.flatMapLatest { attachmentsMap ->
            flow {
                attachmentsMap.forEach { (messageId, attachments) ->
                    attachments.forEach { attachment ->
                        emit(messageId to attachment.attachmentId)
                    }
                }
            }

        }
    }
        .launchIn(viewModelScope)

    private fun onOpenAttachmentClicked(attachmentId: AttachmentId) {
        viewModelScope.launch {
            if (isAttachmentDownloadInProgress().not()) {
                val userId = primaryUserId.first()
                getAttachmentIntentValues(userId, attachmentId).fold(
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
            getDownloadingAttachmentsForMessages(
                userId,
                messagesState.messages.map { MessageId(it.messageId.id) }
            ).isNotEmpty()
        } else false
    }

    private fun updateObservedAttachments(attachments: Map<MessageId, List<AttachmentMetadata>>) {
        attachmentsState.update { it + attachments }
    }

    private fun removeObservedAttachments(messageId: MessageId) {
        attachmentsState.update { it - MessageId(messageId.id) }
    }

    private fun handleReportPhishingConfirmed(action: ConversationDetailViewAction.ReportPhishingConfirmed) {
        viewModelScope.launch {
            reportPhishingMessage(primaryUserId.first(), action.messageId)
                .onLeft { Timber.e("Error while reporting phishing message: $it") }
            emitNewStateFrom(action)
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
            ?.firstOrNull { uiModel -> uiModel.isCalendar }

        if (firstCalendarAttachment == null) return

        getAttachmentIntentValues(
            userId = primaryUserId.first(),
            attachmentId = AttachmentId(firstCalendarAttachment.id.value)
        ).fold(
            ifLeft = { Timber.d("Failed to download attachment: $it") },
            ifRight = {
                val intent = OpenProtonCalendarIntentValues.OpenIcsInProtonCalendar(it.uri, sender, recipient)
                emitNewStateFrom(ConversationDetailEvent.HandleOpenProtonCalendarRequest(intent))
            }
        )
    }

    private fun handleMarkMessageUnread(action: ConversationDetailViewAction.MarkMessageUnread) {
        viewModelScope.launch {
            if (isSingleMessageConversation()) {
                performSafeExitAction(
                    onLeft = ConversationDetailEvent.ErrorMarkingAsUnread,
                    onRight = ConversationDetailEvent.ExitScreen
                ) { _ ->
                    markMessageAsUnread(primaryUserId.first(), action.messageId)
                }
            } else {
                markMessageAsUnread(primaryUserId.first(), action.messageId)
                onCollapseMessage(MessageIdUiModel(action.messageId.id))
                emitNewStateFrom(action)
            }
        }
    }

    private fun handleMoveMessage(action: ConversationDetailViewAction.MoveMessage) = viewModelScope.launch {
        val mailLabelId = when (action) {
            is ConversationDetailViewAction.MoveMessage.CustomFolder -> MailLabelId.Custom.Folder(action.labelId)
            is ConversationDetailViewAction.MoveMessage.System -> MailLabelId.System(action.labelId.labelId)
        }

        handleMoveMessage(mailLabelId = mailLabelId, mailLabelText = action.mailLabelText, messageId = action.messageId)
    }

    private suspend fun handleMoveMessage(
        mailLabelId: MailLabelId,
        mailLabelText: MailLabelText,
        messageId: MessageId
    ) {
        val userId = primaryUserId.first()

        val isLastMessageInLocation = isLastMessageInLocation(
            userId,
            conversationId,
            messageId,
            mailLabelId.labelId
        )

        if (mailLabelId is MailLabelId.System) {
            moveMessage(userId, messageId, SystemLabelId.enumOf(mailLabelId.labelId.id)).getOrNull()
        } else {
            moveMessage(userId, messageId, mailLabelId.labelId).getOrNull()
        } ?: return emitNewStateFrom(ConversationDetailEvent.ErrorMovingMessage)

        val event = if (isLastMessageInLocation) {
            ConversationDetailEvent.LastMessageMoved(mailLabelText)
        } else {
            ConversationDetailEvent.MessageMoved(mailLabelText)
        }

        emitNewStateFrom(event)
    }

    private suspend fun isLastMessageInLocation(
        userId: UserId,
        conversationId: ConversationId,
        messageId: MessageId,
        labelId: LabelId
    ): Boolean {
        val messagesInSameLocation = getMessagesInSameExclusiveLocation(
            userId,
            conversationId,
            messageId,
            labelId
        ).getOrElse {
            Timber.d("Unable to determine the number of messages in the current location - $labelId")
            return true
        }

        return messagesInSameLocation.size == 1
    }

    private fun handleStarMessage(starAction: ConversationDetailViewAction.StarMessage) = viewModelScope.launch {
        starMessages(primaryUserId.first(), listOf(starAction.messageId))
        emitNewStateFrom(starAction)
    }

    private fun handleUnStarMessage(unStarAction: ConversationDetailViewAction.UnStarMessage) = viewModelScope.launch {
        unStarMessages(primaryUserId.first(), listOf(unStarAction.messageId))
        emitNewStateFrom(unStarAction)
    }

    private fun handleOnAvatarImageLoadRequested(avatarUiModel: AvatarUiModel) {
        (avatarUiModel as? AvatarUiModel.ParticipantAvatar)?.let { avatar ->
            viewModelScope.launch {
                loadAvatarImage(avatar.address, avatar.bimiSelector)
            }
        }
    }

    private fun handleMarkMessageAsLegitimate(messageId: MessageId) = viewModelScope.launch {
        markMessageAsLegitimate(
            userId = primaryUserId.first(),
            messageId = messageId
        ).fold(
            ifLeft = { Timber.e("Failed to mark message ${messageId.id} as legitimate") },
            ifRight = { setOrRefreshMessageBody(MessageIdUiModel(messageId.id)) }
        )
    }

    private fun handleUnblockSender(messageId: MessageIdUiModel, email: String) = viewModelScope.launch {
        viewModelScope.launch {
            unblockSender(
                userId = primaryUserId.first(),
                email = email
            ).fold(
                ifLeft = { Timber.e("Failed to unblock sender in message ${messageId.id}") },
                ifRight = { setOrRefreshMessageBody(messageId) }
            )
        }
    }

    /**
     * A helper function that allows to perform actions that eventually cause the user to leave the screen, while
     * still making sure that observers are not being triggered during the execution of the action.
     *
     * At start, all observer jobs are stopped. If the [action] completes with success, they are not resumed as the
     * [onRight] emitted operation is expected to cause a screen exit.
     *
     * In case the [action] fails, other than emitting [onLeft], the observation jobs will be restarted, as the user
     * will still be in the Conversation Details screen.
     */
    private suspend fun performSafeExitAction(
        onLeft: ConversationDetailOperation,
        onRight: ConversationDetailOperation,
        action: suspend (userId: UserId) -> Either<*, *>
    ) {
        stopAllJobs()

        val userId = primaryUserId.first()
        val event = action(userId).fold(
            ifLeft = {
                restartAllJobs()
                onLeft
            },
            ifRight = { onRight }
        )

        emitNewStateFrom(event)
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
