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
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailattachments.domain.model.AttachmentOpenMode
import ch.protonmail.android.mailattachments.domain.usecase.GetAttachmentIntentValues
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.isOfflineError
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcommon.presentation.model.BottomBarTarget
import ch.protonmail.android.mailcontact.domain.usecase.FindContactByEmail
import ch.protonmail.android.mailconversation.domain.entity.ConversationDetailEntryPoint
import ch.protonmail.android.mailconversation.domain.entity.isOfflineError
import ch.protonmail.android.mailconversation.domain.usecase.DeleteConversations
import ch.protonmail.android.mailconversation.domain.usecase.ObserveConversation
import ch.protonmail.android.mailconversation.domain.usecase.StarConversations
import ch.protonmail.android.mailconversation.domain.usecase.UnStarConversations
import ch.protonmail.android.maildetail.domain.model.OpenProtonCalendarIntentValues
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.maildetail.domain.usecase.AnswerRsvpEvent
import ch.protonmail.android.maildetail.domain.usecase.BlockSender
import ch.protonmail.android.maildetail.domain.usecase.GetRsvpEvent
import ch.protonmail.android.maildetail.domain.usecase.IsMessageSenderBlocked
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
import ch.protonmail.android.maildetail.domain.usecase.UnsubscribeFromNewsletter
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
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.DoNotAskLinkConfirmationAgain
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ExpandMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MarkUnread
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MessageBodyLinkClicked
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MoveToInbox
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MoveToTrash
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestScrollTo
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ScrollRequestCompleted
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ShowAllAttachmentsForMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.Star
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.UnStar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailsMessagesState
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpWidgetUiModel
import ch.protonmail.android.maildetail.presentation.reducer.ConversationDetailReducer
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetailScreen
import ch.protonmail.android.maildetail.presentation.usecase.GetMessagesInSameExclusiveLocation
import ch.protonmail.android.maildetail.presentation.usecase.GetMoreActionsBottomSheetData
import ch.protonmail.android.maildetail.presentation.usecase.LoadImageAvoidDuplicatedExecution
import ch.protonmail.android.maildetail.presentation.usecase.ObservePrimaryUserAddress
import ch.protonmail.android.maildetail.presentation.usecase.print.PrintConfiguration
import ch.protonmail.android.maildetail.presentation.usecase.print.PrintMessage
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ResolveSystemLabelId
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsItemId
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToItemId
import ch.protonmail.android.maillabel.presentation.model.MailLabelText
import ch.protonmail.android.mailmessage.domain.mapper.MessageBodyTransformationsMapper
import ch.protonmail.android.mailmessage.domain.model.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.domain.model.AvatarImageState
import ch.protonmail.android.mailmessage.domain.model.AvatarImageStates
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformationsOverride
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageTheme
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.domain.usecase.CancelScheduleSendMessage
import ch.protonmail.android.mailmessage.domain.usecase.DeleteMessages
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageBodyWithClickableLinks
import ch.protonmail.android.mailmessage.domain.usecase.LoadAvatarImage
import ch.protonmail.android.mailmessage.domain.usecase.ObserveAvatarImageStates
import ch.protonmail.android.mailmessage.domain.usecase.StarMessages
import ch.protonmail.android.mailmessage.domain.usecase.UnStarMessages
import ch.protonmail.android.mailmessage.presentation.model.attachment.isExpandable
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.ContactActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MoveToBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.SnoozeSheetState
import ch.protonmail.android.mailsession.domain.usecase.ExecuteWhenOnline
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsRefreshSignal
import ch.protonmail.android.mailsettings.domain.usecase.privacy.ObservePrivacySettings
import ch.protonmail.android.mailsettings.domain.usecase.privacy.UpdateLinkConfirmationSetting
import ch.protonmail.android.mailsnooze.domain.SnoozeRepository
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeConversationId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
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
    private val reducer: ConversationDetailReducer,
    private val starConversations: StarConversations,
    private val unStarConversations: UnStarConversations,
    private val starMessages: StarMessages,
    private val unStarMessages: UnStarMessages,
    private val savedStateHandle: SavedStateHandle,
    private val getMessageBodyWithClickableLinks: GetMessageBodyWithClickableLinks,
    private val markMessageAsRead: MarkMessageAsRead,
    private val messageViewStateCache: MessageViewStateCache,
    private val observeConversationViewState: ObserveConversationViewState,
    private val getAttachmentIntentValues: GetAttachmentIntentValues,
    private val loadImageAvoidDuplicatedExecution: LoadImageAvoidDuplicatedExecution,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val observePrivacySettings: ObservePrivacySettings,
    private val updateLinkConfirmationSetting: UpdateLinkConfirmationSetting,
    private val reportPhishingMessage: ReportPhishingMessage,
    private val isProtonCalendarInstalled: IsProtonCalendarInstalled,
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
    private val blockSender: BlockSender,
    private val isMessageSenderBlocked: IsMessageSenderBlocked,
    private val cancelScheduleSendMessage: CancelScheduleSendMessage,
    private val printMessage: PrintMessage,
    private val getRsvpEvent: GetRsvpEvent,
    private val answerRsvpEvent: AnswerRsvpEvent,
    private val snoozeRepository: SnoozeRepository,
    private val unsubscribeFromNewsletter: UnsubscribeFromNewsletter,
    private val toolbarRefreshSignal: ToolbarActionsRefreshSignal,
    private val executeWhenOnline: ExecuteWhenOnline,
    private val resolveSystemLabelId: ResolveSystemLabelId
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId()
        .stateIn(
            viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        .filterNotNull()

    // Signal used to trigger a reload on connection change - as offline errors are treated as terminal ops.
    private val reloadSignal = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // Signal triggered when an offline state is detected.
    //
    // This VM has multiple observers that can trigger an offline state independently.
    // For this reason, we need to ensure that multiple emissions are counted as one, as the signal
    // will cause a screen reload to display the fetched data to the user.
    private val offlineErrorSignal = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val mutableDetailState = MutableStateFlow(initialState)
    private val conversationId = requireConversationId()
    private val initialScrollToMessageId = getInitialScrollToMessageId()
    private val openedFromLocation = getOpenedFromLocation()
    private val conversationEntryPoint = getEntryPoint()
    private val attachmentsState = MutableStateFlow<Map<MessageId, List<AttachmentMetadata>>>(emptyMap())
    private val showAllMessages = MutableStateFlow(false)
    val isSingleMessageModeEnabled = getIsSingleMessageMode()

    val state: StateFlow<ConversationDetailState> = mutableDetailState.asStateFlow()

    private val jobs = CopyOnWriteArrayList<Job>()

    init {
        Timber.d("Open detail screen for conversation ID: $conversationId")
        viewModelScope.launch {
            showAllMessages.value = resolveInitialShowAll()
            setupObservers()
        }

        setupOfflineObserver()
    }

    // This needs to go - see ET-4700
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

    private fun setupOfflineObserver() {
        offlineErrorSignal
            .take(1)
            .onEach {
                executeWhenOnline(primaryUserId.first()) {
                    viewModelScope.launch {
                        Timber.d("Triggering reload signal for conversation $conversationId")
                        reloadSignal.emit(Unit)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @Suppress("LongMethod", "ComplexMethod")
    fun submit(action: ConversationDetailViewAction) {
        when (action) {
            is Star -> handleStarAction()
            is UnStar -> handleUnStarAction()
            is ConversationDetailViewAction.MarkRead -> markAsRead()
            is MarkUnread -> handleMarkUnReadAction()
            is MoveToTrash -> handleTrashAction()
            is ConversationDetailViewAction.MoveToArchive -> handleArchiveAction()
            is ConversationDetailViewAction.MoveToSpam -> handleSpamAction()
            is ConversationDetailViewAction.DeleteConfirmed -> handleDeleteConfirmed(action)
            is ConversationDetailViewAction.DeleteMessageConfirmed -> handleDeleteMessageConfirmed(action)
            is ConversationDetailViewAction.RequestConversationMoveToBottomSheet ->
                handleRequestMoveToBottomSheetAction()

            is ConversationDetailViewAction.MoveToCompleted -> handleMoveToCompleted(action)
            is MoveToInbox -> handleMoveToInboxAction()
            is ConversationDetailViewAction.RequestConversationLabelAsBottomSheet ->
                handleRequestLabelAsBottomSheetAction()

            is ConversationDetailViewAction.RequestContactActionsBottomSheet ->
                showContactActionsBottomSheetAndLoadData(action)

            is ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet ->
                showMessageMoreActionsBottomSheet(action)

            is ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet ->
                handleRequestMoreBottomSheetAction(action)

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
                onOpenAttachmentClicked(action.openMode, action.attachmentId)
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

            is ConversationDetailViewAction.DeleteRequested -> handleDeleteRequestedAction()
            is ConversationDetailViewAction.DeleteDialogDismissed,
            is ConversationDetailViewAction.DeleteMessageRequested,
            is ConversationDetailViewAction.DismissBottomSheet,
            is MessageBodyLinkClicked,
            is RequestScrollTo,
            is ScrollRequestCompleted,
            is ConversationDetailViewAction.ReportPhishing,
            is ConversationDetailViewAction.ReportPhishingDismissed,
            is ConversationDetailViewAction.MarkMessageAsLegitimate,
            is ConversationDetailViewAction.MarkMessageAsLegitimateDismissed,
            is ConversationDetailViewAction.EditScheduleSendMessageDismissed,
            is ConversationDetailViewAction.EditScheduleSendMessageRequested -> directlyHandleViewAction(action)

            is ConversationDetailViewAction.SwitchViewMode -> handleSwitchViewMode(action)

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

            is ConversationDetailViewAction.MarkMessageAsLegitimateConfirmed ->
                handleMarkMessageAsLegitimateConfirmed(action)

            is ConversationDetailViewAction.UnblockSender -> handleUnblockSender(action)
            is ConversationDetailViewAction.BlockSender -> handleBlockSender(action)
            is ConversationDetailViewAction.EditScheduleSendMessageConfirmed -> handleEditScheduleSendMessage(action)
            is ConversationDetailViewAction.PrintMessage -> handlePrintMessage(action.context, action.messageId)
            is ConversationDetailViewAction.RetryRsvpEventLoading ->
                handleGetRsvpEvent(action.messageId, refresh = true)

            is ConversationDetailViewAction.AnswerRsvpEvent -> handleAnswerRsvpEvent(action.messageId, action.answer)
            ConversationDetailViewAction.OnUnsnoozeConversationRequested -> handleUnsnoozeMessage()
            is ConversationDetailViewAction.SnoozeDismissed -> handleSnoozeDismissedAction(action)
            is ConversationDetailViewAction.SnoozeCompleted -> handleSnoozeCompletedAction(action)
            is ConversationDetailViewAction.RequestSnoozeBottomSheet -> requestSnoozeBottomSheet()
            is ConversationDetailViewAction.UnsubscribeFromNewsletter ->
                handleUnsubscribeFromNewsletter(action.messageId)
        }
    }

    private fun handlePrintMessage(context: Context, messageId: MessageId) {
        val conversationState = state.value.conversationState
        val messagesState = state.value.messagesState

        viewModelScope.launch {
            if (
                conversationState is ConversationDetailMetadataState.Data &&
                messagesState is ConversationDetailsMessagesState.Data
            ) {
                messagesState.messages.find { it.messageId.id == messageId.id }?.let {
                    if (it is ConversationDetailMessageUiModel.Expanded) {
                        printMessage(
                            context = context,
                            subject = conversationState.conversationUiModel.subject,
                            messageHeader = it.messageDetailHeaderUiModel,
                            messageBody = it.messageBodyUiModel,
                            loadEmbeddedImage = this@ConversationDetailViewModel::loadImage,
                            printConfiguration = PrintConfiguration(
                                showRemoteContent = !it.messageBodyUiModel.shouldShowRemoteContentBanner,
                                showEmbeddedImages = !it.messageBodyUiModel.shouldShowEmbeddedImagesBanner
                            )
                        )
                    } else {
                        Timber.e("Can't print a message that is not expanded")
                    }
                    emitNewStateFrom(ConversationDetailViewAction.DismissBottomSheet)
                }
            }
        }
    }

    private fun handleSnoozeCompletedAction(action: ConversationDetailViewAction.SnoozeCompleted) =
        viewModelScope.launch {
            emitNewStateFrom(ConversationDetailEvent.ExitScreenWithMessage(action))
        }

    private fun handleSnoozeDismissedAction(action: ConversationDetailViewAction.SnoozeDismissed) =
        viewModelScope.launch {
            emitNewStateFrom(action)
        }

    private fun requestSnoozeBottomSheet() {
        viewModelScope.launch {
            val userId = primaryUserId.filterNotNull().first()
            val selectedLabelId = openedFromLocation
            val event = SnoozeSheetState.SnoozeOptionsBottomSheetEvent.Ready(
                userId = userId,
                labelId = selectedLabelId,
                itemIds = listOf(SnoozeConversationId(conversationId.id))
            )
            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(event))
        }
    }

    private fun handleEditScheduleSendMessage(action: ConversationDetailViewAction.EditScheduleSendMessageConfirmed) {
        viewModelScope.launch {
            emitNewStateFrom(action)
            cancelScheduleSendMessage(primaryUserId.first(), MessageId(action.messageId.id))
                .onLeft { error ->
                    if (error.isOfflineError()) {
                        emitNewStateFrom(ConversationDetailEvent.OfflineErrorCancellingScheduleSend(action.messageId))
                        return@launch
                    }
                    emitNewStateFrom(ConversationDetailEvent.ErrorCancellingScheduleSend(action.messageId))
                }
                .onRight { emitNewStateFrom(ConversationDetailEvent.ScheduleSendCancelled(action.messageId)) }
        }
    }

    private fun handleSwitchViewMode(action: ConversationDetailViewAction.SwitchViewMode) {
        viewModelScope.launch {
            val overrideTheme = when (action.overrideTheme) {
                MessageTheme.Light -> MessageBodyTransformationsOverride.ViewInLightMode(action.currentTheme)
                MessageTheme.Dark -> MessageBodyTransformationsOverride.ViewInDarkMode(action.currentTheme)
            }
            emitNewStateFrom(action)
            setOrRefreshMessageBody(MessageIdUiModel(action.messageId.id), overrideTheme)
        }

    }

    fun loadImage(messageId: MessageId?, url: String) = messageId?.let {
        runBlocking {
            loadImageAvoidDuplicatedExecution(
                userId = primaryUserId.first(),
                messageId = it,
                url = url,
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
    }
        .restartableOn(reloadSignal)
        .launchIn(viewModelScope)

    private fun observeConversationMetadata(conversationId: ConversationId) = primaryUserId.flatMapLatest { userId ->
        showAllMessages.flatMapLatest { showAllMessages ->
            observeConversation(
                userId,
                conversationId,
                openedFromLocation,
                conversationEntryPoint,
                showAllMessages
            ).mapLatest { either ->
                either.fold(
                    ifLeft = {
                        if (it.isOfflineError()) {
                            signalOfflineError()
                            ConversationDetailEvent.NoNetworkError
                        } else {
                            ConversationDetailEvent.ErrorLoadingConversation
                        }
                    },
                    ifRight = {
                        ConversationDetailEvent.ConversationData(
                            conversationMetadataMapper.toUiModel(it),
                            it.hiddenMessagesBanner,
                            showAllMessages
                        )
                    }
                )
            }
        }
    }
        .restartableOn(reloadSignal)
        .onEach { event ->
            emitNewStateFrom(event)
        }
        .launchIn(viewModelScope)

    @Suppress("LongMethod")
    private fun observeConversationMessages(conversationId: ConversationId) = primaryUserId.flatMapLatest { userId ->
        showAllMessages.flatMapLatest { showAllMessages ->
            combine(
                observeConversationMessages(
                    userId,
                    conversationId,
                    openedFromLocation,
                    conversationEntryPoint,
                    showAllMessages
                ),
                observeConversationViewState(),
                observePrimaryUserAddress(),
                observeAvatarImageStates()
            ) { messagesEither, conversationViewState, primaryUserAddress, avatarImageStates ->
                val conversationMessages = messagesEither.getOrElse {
                    return@combine if (it.isOfflineError()) {
                        signalOfflineError()
                        ConversationDetailEvent.NoNetworkError
                    } else {
                        ConversationDetailEvent.ErrorLoadingMessages
                    }
                }

                val displayMessages = when {
                    isSingleMessageModeEnabled -> {
                        val message = conversationMessages.filterMessage(initialScrollToMessageId)
                        if (message == null) {
                            Timber.tag("SingleMessageMode")
                                .w("single message requested, message is not in convo $initialScrollToMessageId")
                            return@combine ConversationDetailEvent.ErrorLoadingSingleMessage
                        }
                        message
                    }

                    else -> conversationMessages.messages
                }

                val messagesUiModels = buildMessagesUiModels(
                    messages = displayMessages,
                    primaryUserAddress = primaryUserAddress,
                    currentViewState = conversationViewState,
                    avatarImageStates = avatarImageStates
                ).toImmutableList()

                val initialScrollTo = initialScrollToMessageId
                    ?: conversationMessages.messageIdToOpen
                        .let { messageIdUiModelMapper.toUiModel(it) }
                if (stateIsLoadingOrOffline() && allCollapsed(conversationViewState.messagesState)) {
                    ConversationDetailEvent.MessagesData(
                        messagesUiModels,
                        initialScrollTo,
                        openedFromLocation
                    )
                } else {
                    val requestScrollTo = requestScrollToMessageId(conversationViewState.messagesState)
                    ConversationDetailEvent.MessagesData(
                        messagesUiModels,
                        requestScrollTo,
                        openedFromLocation
                    )
                }
            }
        }
    }
        .filterNotNull()
        .distinctUntilChanged()
        .flowOn(ioDispatcher)
        .restartableOn(reloadSignal)
        .onEach { event ->
            emitNewStateFrom(event)
        }
        .launchIn(viewModelScope)

    private fun ConversationMessages.filterMessage(idUiModel: MessageIdUiModel?) = this.messages
        .filter { it.messageId.id == idUiModel?.id }
        .toNonEmptyListOrNull()

    private fun stateIsLoadingOrOffline() = state.value.messagesState == ConversationDetailsMessagesState.Loading ||
        state.value.messagesState == ConversationDetailsMessagesState.Offline

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
            val attachmentListExpandCollapseMode = currentViewState.attachmentsListExpandCollapseMode[message.messageId]
            val rsvpEventState = currentViewState.rsvpEvents[message.messageId]
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
                        viewState.decryptedBody,
                        attachmentListExpandCollapseMode,
                        rsvpEventState
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
        decryptedBody: DecryptedMessageBody,
        attachmentListExpandCollapseMode: AttachmentListExpandCollapseMode?,
        rsvpEvent: InMemoryConversationStateRepository.RsvpEventState?
    ): ConversationDetailMessageUiModel.Expanded = conversationMessageMapper.toUiModel(
        message,
        avatarImageState,
        primaryUserAddress,
        decryptedBody,
        attachmentListExpandCollapseMode,
        rsvpEvent
    )

    @Suppress("LongMethod")
    private fun observeBottomBarActions(conversationId: ConversationId) = combine(
        primaryUserId,
        toolbarRefreshSignal.refreshEvents.onStart { emit(Unit) }
    ) { userId, _ ->
        userId
    }.flatMapLatest { userId ->
        val errorEvent = ConversationDetailEvent.ConversationBottomBarEvent(BottomBarEvent.ErrorLoadingActions)
        val offlineEvent = ConversationDetailEvent.ConversationBottomBarEvent(BottomBarEvent.Offline)
        val labelId = openedFromLocation
        val themeOptions = MessageThemeOptions(MessageTheme.Dark)

        if (isSingleMessageModeEnabled) {
            val messageId = initialScrollToMessageId?.let { MessageId(it.id) }
            if (messageId == null) {
                return@flatMapLatest flowOf(errorEvent)
            }
            observeDetailActions(userId, labelId, messageId, themeOptions).mapLatest { either ->
                either.fold(
                    ifLeft = {
                        if (it.isOfflineError()) {
                            offlineEvent
                        } else {
                            errorEvent
                        }
                    },
                    ifRight = { actions ->
                        val actionUiModels = actions.map { actionUiModelMapper.toUiModel(it) }.toImmutableList()
                        ConversationDetailEvent.ConversationBottomBarEvent(
                            BottomBarEvent.ShowAndUpdateActionsData(
                                BottomBarTarget.Message(messageId.id),
                                actionUiModels
                            )
                        )
                    }
                )
            }
        } else {
            observeDetailActions(userId, labelId, conversationId, conversationEntryPoint, showAllMessages.value)
                .mapLatest { either ->
                    either.fold(
                        ifLeft = {
                            if (it.isOfflineError()) {
                                offlineEvent
                            } else {
                                errorEvent
                            }
                        },
                        ifRight = { actions ->
                            val actionUiModels = actions.map { actionUiModelMapper.toUiModel(it) }.toImmutableList()
                            ConversationDetailEvent.ConversationBottomBarEvent(
                                BottomBarEvent.ShowAndUpdateActionsData(
                                    BottomBarTarget.Conversation, actionUiModels
                                )
                            )
                        }
                    )
                }
        }
    }
        .restartableOn(reloadSignal)
        .onEach { event ->
            emitNewStateFrom(event)
        }
        .launchIn(viewModelScope)


    private fun showContactActionsBottomSheetAndLoadData(
        action: ConversationDetailViewAction.RequestContactActionsBottomSheet
    ) {
        viewModelScope.launch {
            emitNewStateFrom(action)

            val userId = primaryUserId.first()
            val contact = findContactByEmail(userId, action.participant.participantAddress)

            val primaryUserAddress = observePrimaryUserAddress().first()
            val isPrimaryUserAddress = primaryUserAddress == action.participant.participantAddress

            // Rust does not provide an API to check if a sender is blocked. Therefore,
            // we get this data from message banners temporarily
            val senderBlocked = action.messageId?.let { isMessageSenderBlocked(userId, MessageId(it.id)) } ?: false

            val event = ConversationDetailEvent.ConversationBottomSheetEvent(
                ContactActionsBottomSheetState.ContactActionsBottomSheetEvent.ActionData(
                    participant = Participant(
                        address = action.participant.participantAddress,
                        name = action.participant.participantName
                    ),
                    avatarUiModel = action.avatarUiModel,
                    contactId = contact?.id,
                    origin = action.messageId?.let {
                        ContactActionsBottomSheetState.Origin.MessageDetails(
                            MessageId(action.messageId.id)
                        )
                    } ?: ContactActionsBottomSheetState.Origin.Unknown,
                    isSenderBlocked = senderBlocked,
                    isPrimaryUserAddress = isPrimaryUserAddress
                )
            )
            emitNewStateFrom(event)
        }
    }

    private fun handleRequestMoveToBottomSheetAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                requestMessageMoveToBottomSheet(
                    ConversationDetailViewAction.RequestMessageMoveToBottomSheet(messageId)
                )
            } else {
                requestConversationMoveToBottomSheet(
                    ConversationDetailViewAction.RequestConversationMoveToBottomSheet
                )
            }
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

    private fun handleRequestLabelAsBottomSheetAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                requestMessageLabelAsBottomSheet(
                    ConversationDetailViewAction.RequestMessageLabelAsBottomSheet(messageId)
                )
            } else {
                requestConversationLabelAsBottomSheet(
                    ConversationDetailViewAction.RequestConversationLabelAsBottomSheet
                )
            }
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
                is MoveToBottomSheetEntryPoint.Message ->
                    entryPoint.isLastInCurrentLocation || isSingleMessageModeEnabled

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

            val moreActions = getMoreActionsBottomSheetData.forMessage(
                userId,
                labelId,
                initialEvent.messageId,
                initialEvent.themeOptions,
                initialEvent.entryPoint
            )
                ?: return@launch
            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(moreActions))
        }
    }

    private fun handleRequestMoreBottomSheetAction(
        action: ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet
    ) {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                showMessageMoreActionsBottomSheet(
                    initialEvent = ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet(
                        messageId = messageId,
                        themeOptions = MessageThemeOptions(MessageTheme.Dark),
                        entryPoint = action.entryPoint
                    )
                )
            } else {
                showConversationMoreActionsBottomSheet(action)
            }
        }
    }

    private fun showConversationMoreActionsBottomSheet(
        initialEvent: ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet
    ) {
        viewModelScope.launch {
            Timber.d("more-actions: requesting bottomsheet for convo")
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labelId = openedFromLocation
            val moreActions = getMoreActionsBottomSheetData.forConversation(
                userId,
                labelId,
                conversationId,
                conversationEntryPoint,
                showAllMessages.value
            ) ?: return@launch

            emitNewStateFrom(ConversationDetailEvent.ConversationBottomSheetEvent(moreActions))
        }
    }

    private fun requireConversationId(): ConversationId {
        val conversationId = savedStateHandle.get<String>(ConversationDetailScreen.ConversationIdKey)
            ?: throw IllegalStateException("No Conversation id given")
        return ConversationId(conversationId)
    }

    private fun getIsSingleMessageMode(): Boolean {
        val value = savedStateHandle.get<String>(ConversationDetailScreen.IsSingleMessageMode)
        Timber.tag("SingleMessageMode").d("Show single message is: $value")
        return value.toBoolean()
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

    private fun getEntryPoint(): ConversationDetailEntryPoint {
        val value = savedStateHandle.get<String>(ConversationDetailScreen.ConversationDetailEntryPointNameKey)
            ?: throw IllegalStateException("No Entry point given")
        return ConversationDetailEntryPoint.valueOf(value)
    }

    private suspend fun emitNewStateFrom(event: ConversationDetailOperation) {
        val newState = reducer.newStateFrom(state.value, event)
        mutableDetailState.update { newState }
    }

    private fun handleMarkUnReadAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                handleMarkMessageUnread(ConversationDetailViewAction.MarkMessageUnread(messageId))
            } else {
                markAsUnread()
            }
        }
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

    private fun handleTrashAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                handleMoveMessage(ConversationDetailViewAction.MoveMessage.System.Trash(messageId))
            } else {
                moveConversationToTrash()
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

    private fun handleSpamAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                handleMoveMessage(ConversationDetailViewAction.MoveMessage.System.Spam(messageId))
            } else {
                moveConversationToSpam()
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

    private fun handleArchiveAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                handleMoveMessage(ConversationDetailViewAction.MoveMessage.System.Archive(messageId))
            } else {
                moveConversationToArchive()
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

    private fun handleStarAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                handleStarMessage(ConversationDetailViewAction.StarMessage(messageId))
            } else {
                starConversation()
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

    private fun handleUnStarAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                handleUnStarMessage(ConversationDetailViewAction.UnStarMessage(messageId))
            } else {
                unStarConversation()
            }
        }
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

    private fun handleMoveToInboxAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                handleMoveMessage(ConversationDetailViewAction.MoveMessage.System.Inbox(messageId))
            } else {
                moveConversationToInbox()
            }
        }
    }

    private fun moveConversationToInbox() {
        viewModelScope.launch {
            performSafeExitAction(
                onLeft = ConversationDetailEvent.ErrorMovingConversation,
                onRight = ConversationDetailEvent.ExitScreenWithMessage(MoveToInbox)
            ) { userId ->
                moveConversation(userId, conversationId, SystemLabelId.Inbox)
            }
        }
    }

    private fun handleDeleteRequestedAction() {
        viewModelScope.launch {
            if (isSingleMessageModeEnabled) {
                val messageId = initialScrollToMessageId?.let { MessageId(it.id) } ?: return@launch
                emitNewStateFrom(ConversationDetailViewAction.DeleteMessageRequested(messageId))
            } else {
                emitNewStateFrom(ConversationDetailViewAction.DeleteRequested)
            }
        }
    }

    private fun handleDeleteMessageConfirmed(action: ConversationDetailViewAction.DeleteMessageConfirmed) {
        viewModelScope.launch {
            emitNewStateFrom(action)
            val currentLabelId = openedFromLocation
            val userId = primaryUserId.first()

            val shouldExitScreen = isSingleMessageModeEnabled ||
                isLastMessageInLocation(
                    userId,
                    conversationId,
                    action.messageId,
                    openedFromLocation
                )

            deleteMessages(primaryUserId.first(), listOf(action.messageId), currentLabelId)
                .onLeft { ConversationDetailEvent.ErrorDeletingMessage }
                .onRight {
                    if (shouldExitScreen) {
                        val event = ConversationDetailEvent.ExitScreenWithMessage(
                            ConversationDetailEvent.LastMessageDeleted
                        )
                        emitNewStateFrom(event)
                    }
                }
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
        getMessageBodyWithClickableLinks(userId, domainMsgId, transformations)
            .onRight { message ->
                messageViewStateCache.setExpanded(domainMsgId, message)
                messageViewStateCache.setTransformations(domainMsgId, transformations)

                if (message.attachments.isNotEmpty()) {
                    updateObservedAttachments(mapOf(domainMsgId to message.attachments))
                }

                if (message.isUnread) {
                    markMessageAsRead(userId, domainMsgId)
                }

                if (message.hasCalendarInvite) {
                    handleGetRsvpEvent(domainMsgId, refresh = false)
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

    private fun handleChangeVisibilityOfMessages() = showAllMessages.update { showAllMessages.value.not() }

    private fun onDoNotAskLinkConfirmationChecked() {
        viewModelScope.launch { updateLinkConfirmationSetting(false) }
    }

    private suspend fun emitMessageBodyDecryptError(error: GetMessageBodyError, messageId: MessageIdUiModel) {
        val errorState = when (error) {
            is GetMessageBodyError.Data -> if (error.dataError.isOfflineError()) {
                ConversationDetailEvent.ErrorExpandingRetrievingMessageOffline(messageId)
            } else {
                ConversationDetailEvent.ErrorExpandingRetrieveMessageError(messageId)
            }

            is GetMessageBodyError.Decryption ->
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
                    viewModelScope.launch {
                        messageViewStateCache.updateAttachmentsExpandCollapseMode(
                            MessageId(messageId.id),
                            expandCollapseMode
                        )
                    }
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
        .restartableOn(reloadSignal)
        .launchIn(viewModelScope)

    private fun onOpenAttachmentClicked(openMode: AttachmentOpenMode, attachmentId: AttachmentId) {
        viewModelScope.launch {
            val userId = primaryUserId.first()
            getAttachmentIntentValues(userId, openMode, attachmentId).fold(
                ifLeft = {
                    Timber.d("Failed to download attachment: $it")
                    emitNewStateFrom(ConversationDetailEvent.ErrorGettingAttachment)
                },
                ifRight = { emitNewStateFrom(ConversationDetailEvent.OpenAttachmentEvent(it)) }
            )
        }
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

        if (firstCalendarAttachment == null) {
            getRsvpEventIntentValues(messageUiModel.messageRsvpWidgetUiModel).fold(
                ifLeft = {
                    emitNewStateFrom(ConversationDetailEvent.ErrorOpeningEventInCalendar)
                },
                ifRight = {
                    val intent = OpenProtonCalendarIntentValues.OpenUriInProtonCalendar(
                        it.eventId,
                        it.calendarId,
                        it.recurrenceId
                    )
                    emitNewStateFrom(ConversationDetailEvent.HandleOpenProtonCalendarRequest(intent))
                }
            )
        } else {
            getAttachmentIntentValues(
                userId = primaryUserId.first(),
                openMode = AttachmentOpenMode.Open,
                attachmentId = AttachmentId(firstCalendarAttachment.id.value)
            ).fold(
                ifLeft = {
                    Timber.d("Failed to download attachment: $it")
                    emitNewStateFrom(ConversationDetailEvent.ErrorOpeningEventInCalendar)
                },
                ifRight = {
                    val intent = OpenProtonCalendarIntentValues.OpenIcsInProtonCalendar(it.uri, sender, recipient)
                    emitNewStateFrom(ConversationDetailEvent.HandleOpenProtonCalendarRequest(intent))
                }
            )
        }
    }

    private fun getRsvpEventIntentValues(rsvpWidgetUiModel: RsvpWidgetUiModel): Either<Unit, RsvpEventIntentValues> {
        val event = when (rsvpWidgetUiModel) {
            is RsvpWidgetUiModel.Shown -> rsvpWidgetUiModel.event
            else -> null
        }
        return if (event?.eventId != null && event.calendar?.calendarId != null) {
            RsvpEventIntentValues(
                event.eventId.id,
                event.calendar.calendarId.id,
                event.startsAt
            ).right()
        } else {
            Unit.left()
        }
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

        val event = if (isLastMessageInLocation || isSingleMessageModeEnabled) {
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
            labelId,
            conversationEntryPoint,
            showAllMessages.value
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

    private fun handleMarkMessageAsLegitimateConfirmed(
        action: ConversationDetailViewAction.MarkMessageAsLegitimateConfirmed
    ) {
        viewModelScope.launch {
            markMessageAsLegitimate(
                userId = primaryUserId.first(),
                messageId = action.messageId
            ).fold(
                ifLeft = { Timber.e("Failed to mark message ${action.messageId.id} as legitimate") },
                ifRight = { setOrRefreshMessageBody(MessageIdUiModel(action.messageId.id)) }
            )
            emitNewStateFrom(action)
        }
    }

    private fun handleUnblockSender(action: ConversationDetailViewAction.UnblockSender) = viewModelScope.launch {
        viewModelScope.launch {
            unblockSender(
                userId = primaryUserId.first(),
                email = action.email
            ).fold(
                ifLeft = { Timber.e("Failed to unblock sender in message ${action.messageId?.id}") },
                ifRight = { action.messageId?.let { setOrRefreshMessageBody(it) } }
            )

            emitNewStateFrom(action)
        }
    }

    private fun handleBlockSender(action: ConversationDetailViewAction.BlockSender) = viewModelScope.launch {
        viewModelScope.launch {
            blockSender(
                userId = primaryUserId.first(),
                email = action.email
            ).fold(
                ifLeft = { Timber.e("Failed to block sender in message ${action.messageId?.id}") },
                ifRight = { action.messageId?.let { setOrRefreshMessageBody(it) } }
            )

            emitNewStateFrom(action)
        }
    }

    private fun handleGetRsvpEvent(messageId: MessageId, refresh: Boolean) {
        viewModelScope.launch {
            messageViewStateCache.updateRsvpEventLoading(messageId, refresh)
            getRsvpEvent(primaryUserId.first(), messageId).fold(
                ifLeft = { messageViewStateCache.updateRsvpEventError(messageId) },
                ifRight = { rsvpEvent ->
                    messageViewStateCache.updateRsvpEventShown(messageId, rsvpEvent)
                }
            )
        }
    }

    private fun handleAnswerRsvpEvent(messageId: MessageId, answer: RsvpAnswer) {
        viewModelScope.launch {
            messageViewStateCache.updateRsvpEventAnswering(messageId, answer)
            answerRsvpEvent(primaryUserId.first(), messageId, answer).onLeft {
                emitNewStateFrom(ConversationDetailEvent.ErrorAnsweringRsvpEvent)
            }
            handleGetRsvpEvent(messageId, refresh = false)
        }
    }

    private fun handleUnsnoozeMessage() {
        viewModelScope.launch {
            snoozeRepository.unSnoozeConversation(
                userId = primaryUserId.first(),
                labelId = openedFromLocation,
                conversationIds = listOf(conversationId)
            ).onLeft { error ->
                emitNewStateFrom(ConversationDetailEvent.ErrorUnsnoozing)
            }.onRight {
                emitNewStateFrom(
                    ConversationDetailEvent.ExitScreenWithMessage(
                        ConversationDetailEvent.UnsnoozeCompleted
                    )
                )
            }
        }
    }

    private fun handleUnsubscribeFromNewsletter(messageId: MessageId) {
        viewModelScope.launch {
            unsubscribeFromNewsletter(primaryUserId.first(), messageId).fold(
                ifLeft = {
                    Timber.e("Failed to unsubscribe from newsletter in message ${messageId.id}")
                    emitNewStateFrom(ConversationDetailEvent.ErrorUnsubscribingFromNewsletter)
                },
                ifRight = { setOrRefreshMessageBody(MessageIdUiModel(messageId.id)) }
            )
        }
    }

    /**
     * Resolves the initial state of the `showAllMessages` parameter.
     *
     * This is required as when the message is opened from the AllMail folder (not Almost All Mail)
     * the "Hidden messages" banner is not returned by Rust and the value should always be `true`.
     */
    private suspend fun resolveInitialShowAll(): Boolean {
        val label = resolveSystemLabelId(
            userId = primaryUserId.first(),
            labelId = getOpenedFromLocation()
        ).getOrNull() ?: return false

        return label == SystemLabelId.AllMail
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

    private suspend fun signalOfflineError() {
        offlineErrorSignal.emit(Unit)
    }

    private fun <T> Flow<T>.restartableOn(signal: Flow<Unit>): Flow<T> = signal.onStart { emit(Unit) }
        .flatMapLatest { this@restartableOn }

    private companion object {

        val initialState = ConversationDetailState.Loading
    }

    data class RsvpEventIntentValues(val eventId: String, val calendarId: String, val recurrenceId: Long)
}
