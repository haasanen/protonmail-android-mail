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

package ch.protonmail.android.mailcomposer.presentation.viewmodel

import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.getOrElse
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.IntentShareInfo
import ch.protonmail.android.mailcommon.domain.model.decode
import ch.protonmail.android.mailcommon.domain.model.hasEmailData
import ch.protonmail.android.mailcommon.domain.network.NetworkManager
import ch.protonmail.android.mailcomposer.domain.model.AttachmentDeleteError
import ch.protonmail.android.mailcomposer.domain.model.ChangeSenderError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.DraftFieldsWithSyncStatus
import ch.protonmail.android.mailcomposer.domain.model.OpenDraftError
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.model.hasAnyRecipient
import ch.protonmail.android.mailcomposer.domain.usecase.ChangeSenderAddress
import ch.protonmail.android.mailcomposer.domain.usecase.CreateDraftForAction
import ch.protonmail.android.mailcomposer.domain.usecase.CreateEmptyDraft
import ch.protonmail.android.mailcomposer.domain.usecase.DeleteAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.DeleteInlineAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.DiscardDraft
import ch.protonmail.android.mailcomposer.domain.usecase.GetDraftId
import ch.protonmail.android.mailcomposer.domain.usecase.GetEmbeddedImage
import ch.protonmail.android.mailcomposer.domain.usecase.GetSenderAddresses
import ch.protonmail.android.mailcomposer.domain.usecase.IsValidEmailAddress
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageAttachments
import ch.protonmail.android.mailcomposer.domain.usecase.OpenExistingDraft
import ch.protonmail.android.mailcomposer.domain.usecase.ScheduleSendMessage
import ch.protonmail.android.mailcomposer.domain.usecase.SendMessage
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithBody
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithSubject
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateRecipients
import ch.protonmail.android.mailcomposer.presentation.mapper.ParticipantMapper
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.model.ComposerStates
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsState
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsStateManager
import ch.protonmail.android.mailcomposer.presentation.model.SenderUiModel
import ch.protonmail.android.mailcomposer.presentation.model.operations.ComposerAction
import ch.protonmail.android.mailcomposer.presentation.model.operations.ComposerStateEvent
import ch.protonmail.android.mailcomposer.presentation.model.operations.CompositeEvent
import ch.protonmail.android.mailcomposer.presentation.model.operations.EffectsEvent
import ch.protonmail.android.mailcomposer.presentation.model.operations.MainEvent
import ch.protonmail.android.mailcomposer.presentation.reducer.ComposerStateReducer
import ch.protonmail.android.mailcomposer.presentation.ui.ComposerScreen
import ch.protonmail.android.mailcomposer.presentation.usecase.AddAttachment
import ch.protonmail.android.mailcomposer.presentation.usecase.BuildDraftDisplayBody
import ch.protonmail.android.mailcomposer.presentation.usecase.GetFormattedScheduleSendOptions
import ch.protonmail.android.mailcontact.domain.usecase.GetContacts
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.DraftAction.Compose
import ch.protonmail.android.mailmessage.domain.model.DraftAction.ComposeToAddresses
import ch.protonmail.android.mailmessage.domain.model.DraftAction.Forward
import ch.protonmail.android.mailmessage.domain.model.DraftAction.PrefillForShare
import ch.protonmail.android.mailmessage.domain.model.DraftAction.Reply
import ch.protonmail.android.mailmessage.domain.model.DraftAction.ReplyAll
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.MimeTypeUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.takeIfNotEmpty
import timber.log.Timber
import kotlin.time.Instant

@Suppress("LargeClass", "LongParameterList", "TooManyFunctions", "UnusedPrivateMember")
@HiltViewModel(assistedFactory = ComposerViewModel.Factory::class)
class ComposerViewModel @AssistedInject constructor(
    private val storeDraftWithBody: StoreDraftWithBody,
    private val storeDraftWithSubject: StoreDraftWithSubject,
    private val updateRecipients: UpdateRecipients,
    private val getContacts: GetContacts,
    private val participantMapper: ParticipantMapper,
    private val composerStateReducer: ComposerStateReducer,
    private val isValidEmailAddress: IsValidEmailAddress,
    private val observeMessageAttachments: ObserveMessageAttachments,
    private val sendMessage: SendMessage,
    private val networkManager: NetworkManager,
    private val addAttachment: AddAttachment,
    private val deleteAttachment: DeleteAttachment,
    private val deleteInlineAttachment: DeleteInlineAttachment,
    private val openExistingDraft: OpenExistingDraft,
    private val createEmptyDraft: CreateEmptyDraft,
    private val createDraftForAction: CreateDraftForAction,
    private val buildDraftDisplayBody: BuildDraftDisplayBody,
    @Assisted private val recipientsStateManager: RecipientsStateManager,
    private val discardDraft: DiscardDraft,
    private val getDraftId: GetDraftId,
    private val savedStateHandle: SavedStateHandle,
    private val getEmbeddedImage: GetEmbeddedImage,
    private val getFormattedScheduleSendOptions: GetFormattedScheduleSendOptions,
    private val scheduleSend: ScheduleSendMessage,
    private val getSenderAddresses: GetSenderAddresses,
    private val changeSenderAddress: ChangeSenderAddress,
    observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    internal val subjectTextField = TextFieldState()

    private val primaryUserId = observePrimaryUserId().filterNotNull()
    private val composerActionsChannel = Channel<ComposerAction>(Channel.BUFFERED)

    private val mutableComposerStates = MutableStateFlow(
        ComposerStates(
            main = ComposerState.Main.initial(),
            attachments = ComposerState.Attachments.initial(),
            accessories = ComposerState.Accessories.initial(),
            effects = ComposerState.Effects.initial()
        )
    )

    internal val composerStates = mutableComposerStates.asStateFlow()


    init {
        viewModelScope.launch {
            if (!setupInitialState(savedStateHandle)) return@launch

            observeAttachments()
            observeComposerSubject()
            observeComposerRecipients()
            processActions()
        }
    }

    private suspend fun setupInitialState(savedStateHandle: SavedStateHandle): Boolean {
        val inputDraftId = savedStateHandle.get<String>(ComposerScreen.DraftMessageIdKey)
        val draftAction = savedStateHandle.get<String>(ComposerScreen.SerializedDraftActionKey)
            ?.deserialize<DraftAction>()

        val restoredHandle = savedStateHandle.get<Boolean>(ComposerScreen.HasSavedDraftKey) == true

        when {
            restoredHandle -> {
                onComposerRestored()
                return false
            }

            inputDraftId != null -> prefillWithExistingDraft(inputDraftId).onLeft {
                return false
            }

            draftAction != null -> prefillForDraftAction(draftAction)
            else -> prefillForNewDraft()
        }
        return true
    }

    private fun observeComposerRecipients() {
        recipientsStateManager.recipients
            .onEach { recipients -> onRecipientsChanged(recipients) }
            .launchIn(viewModelScope)
    }

    private fun observeComposerSubject() {
        snapshotFlow { subjectTextField.text }
            .onEach { onSubjectChanged(Subject(it.toString().stripNewLines())) }
            .launchIn(viewModelScope)
    }

    private fun onComposerRestored() {
        // This is hit when process death occurs and the user could be in an inconsistent state:
        // Theoretically we can restore the draft from local storage, but it's not guaranteed that its content is
        // up to date and we don't know if it should overwrite the remote state.
        Timber.tag("ComposerViewModel").d("Restored Composer instance - navigating back.")
        emitNewStateFor(EffectsEvent.ComposerControlEvent.OnCloseRequest)
    }

    private fun prefillForComposeToAction(recipients: List<RecipientUiModel>) {
        viewModelScope.launch {
            recipientsStateManager.updateRecipients(recipients, ContactSuggestionsField.TO)
        }
    }

    private suspend fun prefillForNewDraft() {
        // Emitting also for "empty draft" as now signature is returned with the init body, effectively
        // making this the same as other prefill cases (eg. "reply" or "fw")
        emitNewStateFor(MainEvent.InitialLoadingToggled)

        createEmptyDraft(primaryUserId())
            .onRight { draftFields ->
                emitNewStateFor(
                    CompositeEvent.DraftContentReady(
                        draftUiModel = draftFields.toDraftUiModel(),
                        isDataRefreshed = true,
                        bodyShouldTakeFocus = false
                    )
                )
            }
            .onLeft { emitNewStateFor(EffectsEvent.LoadingEvent.OnSenderAddressLoadingFailed) }
    }

    private suspend fun prefillForShareDraftAction(shareDraftAction: PrefillForShare) {
        val fileShareInfo = shareDraftAction.intentShareInfo.decode()

        fileShareInfo.attachmentUris.takeIfNotEmpty()?.let { rawUri ->
            Timber.w("composer: storing attachment not implemented")
            val uriList = rawUri.map { it.toUri() }
            onAttachmentsAdded(uriList)
        }

        if (fileShareInfo.hasEmailData()) {
            val draftFields = prefillDraftFieldsFromShareInfo(fileShareInfo)
            initComposerFields(draftFields)

            // Needs to trigger a save on Rust side
            onDraftBodyChanged(draftFields.body)

            emitNewStateFor(
                CompositeEvent.DraftContentReady(
                    draftUiModel = draftFields.toDraftUiModel(), isDataRefreshed = true, bodyShouldTakeFocus = false
                )
            )
        }
    }

    private suspend fun prefillDraftFieldsFromShareInfo(intentShareInfo: IntentShareInfo): DraftFields {
        val draftBody = DraftBody(
            // Temporarily concatenate the shared text + the initial Rust body (to include the signature if present)
            buildString {
                append(intentShareInfo.emailBody ?: "")
                appendLine()
                append(mutableComposerStates.value.main.fields.body)
            }
        )
        val subject = Subject(intentShareInfo.emailSubject ?: "")
        val recipientsTo = RecipientsTo(
            intentShareInfo.emailRecipientTo.takeIfNotEmpty()?.map {
                participantMapper.recipientUiModelToParticipant(RecipientUiModel.Valid(it), contactsOrEmpty())
            } ?: emptyList()
        )

        val recipientsCc = RecipientsCc(
            intentShareInfo.emailRecipientCc.takeIfNotEmpty()?.map {
                participantMapper.recipientUiModelToParticipant(RecipientUiModel.Valid(it), contactsOrEmpty())
            } ?: emptyList()
        )

        val recipientsBcc = RecipientsBcc(
            intentShareInfo.emailRecipientBcc.takeIfNotEmpty()?.map {
                participantMapper.recipientUiModelToParticipant(RecipientUiModel.Valid(it), contactsOrEmpty())
            } ?: emptyList()
        )

        return DraftFields(
            currentSenderEmail(),
            subject,
            draftBody,
            recipientsTo,
            recipientsCc,
            recipientsBcc
        )
    }

    @MissingRustApi
    // hardcoding values for isBlockedSendingFromPmAddress / isBlockedSendingFromDisabledAddress
    private suspend fun prefillForDraftAction(draftAction: DraftAction) {
        Timber.d("Opening composer for draft action $draftAction")
        emitNewStateFor(MainEvent.InitialLoadingToggled)
        val focusDraftBody = draftAction is Reply || draftAction is ReplyAll
        when (draftAction) {
            Compose -> prefillForNewDraft()
            is ComposeToAddresses -> {
                Timber.d("composer: prefilling for compose To")
                prefillForNewDraft()
                prefillForComposeToAction(draftAction.extractRecipients())
            }

            is Forward,
            is Reply,
            is ReplyAll -> createDraftForAction(primaryUserId(), draftAction)
                .onRight { draftFields ->
                    initComposerFields(draftFields)
                    emitNewStateFor(
                        CompositeEvent.DraftContentReady(
                            draftUiModel = draftFields.toDraftUiModel(),
                            isDataRefreshed = true,
                            bodyShouldTakeFocus = focusDraftBody
                        )
                    )
                }
                .onLeft { emitNewStateFor(EffectsEvent.LoadingEvent.OnParentLoadingFailed) }

            is PrefillForShare -> {
                prefillForNewDraft()
                prefillForShareDraftAction(draftAction)
            }
        }
    }

    private suspend fun prefillWithExistingDraft(
        inputDraftId: String
    ): Either<OpenDraftError, DraftFieldsWithSyncStatus> {
        Timber.d("Opening composer with $inputDraftId")
        emitNewStateFor(MainEvent.InitialLoadingToggled)

        return openExistingDraft(primaryUserId(), MessageId(inputDraftId))
            .onRight { draftFieldsWithSyncStatus ->
                val draftFields = draftFieldsWithSyncStatus.draftFields
                initComposerFields(draftFields)
                emitNewStateFor(
                    CompositeEvent.DraftContentReady(
                        draftUiModel = draftFields.toDraftUiModel(),
                        isDataRefreshed = draftFieldsWithSyncStatus is DraftFieldsWithSyncStatus.Remote,
                        bodyShouldTakeFocus = draftFields.hasAnyRecipient()
                    )
                )
            }
            .onLeft { emitNewStateFor(EffectsEvent.DraftEvent.OnDraftLoadingFailed) }
    }

    private fun DraftFields.toDraftUiModel(): DraftUiModel {
        val messageBodyWithType = MessageBodyWithType(this.body.value, MimeTypeUiModel.Html)
        val draftDisplayBody = buildDraftDisplayBody(messageBodyWithType)

        return DraftUiModel(this, draftDisplayBody)
    }

    internal fun submit(action: ComposerAction) {
        viewModelScope.launch {
            composerActionsChannel.send(action)
            logViewModelAction(action, "Enqueued")
        }
    }

    private suspend fun processActions() {
        composerActionsChannel.consumeEach { action ->
            logViewModelAction(action, "Executing")
            when (action) {
                is ComposerAction.ChangeSender -> onChangeSenderRequested()
                is ComposerAction.SetSenderAddress -> onChangeSender(action.sender)

                is ComposerAction.OpenExpirationSettings -> TODO()

                is ComposerAction.SetMessageExpiration -> TODO()

                is ComposerAction.AddAttachmentsRequested ->
                    emitNewStateFor(EffectsEvent.AttachmentEvent.OnAttachFromOptionsRequest)

                is ComposerAction.OpenCameraPicker ->
                    emitNewStateFor(EffectsEvent.AttachmentEvent.OnAddFromCameraRequest)

                is ComposerAction.OpenPhotosPicker -> emitNewStateFor(EffectsEvent.AttachmentEvent.OnAddMediaRequest)
                is ComposerAction.OpenFilePicker -> emitNewStateFor(EffectsEvent.AttachmentEvent.OnAddFileRequest)

                is ComposerAction.AddFileAttachments -> onFileAttachmentsAdded(action.uriList)
                is ComposerAction.AddAttachments -> onAttachmentsAdded(action.uriList)
                is ComposerAction.RemoveAttachment -> onAttachmentsRemoved(action.attachmentId)
                is ComposerAction.RemoveInlineAttachment -> onInlineImageRemoved(action.contentId)
                is ComposerAction.InlineImageActionsRequested ->
                    emitNewStateFor(EffectsEvent.AttachmentEvent.OnInlineImageActionsRequested)

                is ComposerAction.CloseComposer -> onCloseComposer()
                is ComposerAction.SendMessage -> handleOnSendMessage()

                is ComposerAction.CancelSendWithNoSubject ->
                    emitNewStateFor(EffectsEvent.SendEvent.OnCancelSendNoSubject)

                is ComposerAction.ConfirmSendWithNoSubject -> onSendMessage()

                is ComposerAction.CancelSendExpirationSetToExternal -> TODO()

                is ComposerAction.ConfirmSendExpirationSetToExternal -> onSendMessage()

                is ComposerAction.ClearSendingError -> TODO()

                is ComposerAction.DraftBodyChanged -> onDraftBodyChanged(action.draftBody)
                is ComposerAction.DiscardDraftRequested ->
                    emitNewStateFor(EffectsEvent.DraftEvent.OnDiscardDraftRequested)

                is ComposerAction.DiscardDraftConfirmed -> onDiscardDraftConfirmed()
                is ComposerAction.OnScheduleSendRequested -> onScheduleSendRequested()
                is ComposerAction.OnScheduleSend -> handleOnScheduleSendMessage(action.time)
                is ComposerAction.AcknowledgeAttachmentErrors -> handleConfirmAttachmentErrors(action)
            }
            logViewModelAction(action, "Completed.")
        }
    }

    private suspend fun onChangeSender(sender: SenderUiModel) {
        val newSender = SenderEmail(sender.email)

        changeSenderAddress(newSender)
            .onLeft { error ->
                when (error) {
                    is ChangeSenderError.AddressCanNotSend,
                    is ChangeSenderError.AddressDisabled,
                    is ChangeSenderError.AddressNotFound ->
                        emitNewStateFor(EffectsEvent.ErrorEvent.OnAddressNotValidForSending)

                    is ChangeSenderError.Other -> emitNewStateFor(EffectsEvent.ErrorEvent.OnChangeSenderFailure)
                    ChangeSenderError.RefreshBodyError -> emitNewStateFor(EffectsEvent.ErrorEvent.OnRefreshBodyFailed)
                }

            }
            .onRight { bodyWithNewSignature ->
                val draftDisplayBody = buildDraftDisplayBody(
                    MessageBodyWithType(bodyWithNewSignature.value, MimeTypeUiModel.Html)
                )
                emitNewStateFor(CompositeEvent.UserChangedSender(newSender, draftDisplayBody))
            }
    }

    private suspend fun onChangeSenderRequested() {
        getSenderAddresses()
            .onLeft {
                emitNewStateFor(EffectsEvent.ErrorEvent.OnGetAddressesError)
            }
            .onRight { senderAddresses ->
                val addresses = senderAddresses.addresses.map { SenderUiModel(it.value) }
                emitNewStateFor(CompositeEvent.SenderAddressesListReady(addresses))
            }
    }


    private fun handleConfirmAttachmentErrors(action: ComposerAction.AcknowledgeAttachmentErrors) {
        val attachmentsWithError = action.attachmentsWithError
        if (attachmentsWithError.isEmpty()) {
            Timber.w("composer: No attachments with error to handle")
            return
        }

        viewModelScope.launch {
            var errorDeletingAttachments: AttachmentDeleteError? = null
            attachmentsWithError.forEach { attachmentId ->
                deleteAttachment(attachmentId)
                    .onLeft {
                        Timber.e("Failed to delete attachment: $it")
                        errorDeletingAttachments = it
                    }
            }

            errorDeletingAttachments?.let {
                emitNewStateFor(EffectsEvent.AttachmentEvent.RemoveAttachmentError(it))
            }
        }
    }

    fun loadEmbeddedImage(contentId: String): EmbeddedImage? = getEmbeddedImage(contentId).getOrNull()

    private suspend fun onScheduleSendRequested() {
        getFormattedScheduleSendOptions()
            .onLeft { emitNewStateFor(EffectsEvent.ErrorEvent.OnGetScheduleSendOptionsError) }
            .onRight { emitNewStateFor(CompositeEvent.ScheduleSendOptionsReady(it)) }
    }

    private fun observeAttachments() {
        viewModelScope.launch {
            observeMessageAttachments().onEach { result ->
                result.onLeft {
                    Timber.e("Failed to observe message attachments: $it")
                    emitNewStateFor(EffectsEvent.AttachmentEvent.OnLoadAttachmentsFailed)
                }.onRight { attachments ->
                    emitNewStateFor(CompositeEvent.AttachmentListChanged(attachments))
                }
            }.launchIn(this)
        }
    }

    private fun validateEmailAddress(emailAddress: String): Boolean = isValidEmailAddress(emailAddress)

    private fun onAttachmentsAdded(uriList: List<Uri>) {
        viewModelScope.launch {
            uriList.forEach { uri ->
                addAttachment(uri).onLeft {
                    Timber.e("Failed to add attachment: $it")
                    emitNewStateFor(EffectsEvent.AttachmentEvent.AddAttachmentError(it))
                }.onRight {
                    when (it) {
                        is AddAttachment.AddAttachmentResult.InlineAttachmentAdded ->
                            emitNewStateFor(EffectsEvent.AttachmentEvent.InlineAttachmentAdded(it.cid))

                        AddAttachment.AddAttachmentResult.StandardAttachmentAdded -> Unit
                    }
                }
            }
        }
    }

    private fun onFileAttachmentsAdded(uriList: List<Uri>) {
        viewModelScope.launch {
            uriList.forEach { uri ->
                addAttachment.forcingStandardDisposition(uri).onLeft {
                    Timber.e("Failed to add standard attachment: $it")
                    emitNewStateFor(EffectsEvent.AttachmentEvent.AddAttachmentError(it))
                }
            }
        }
    }

    private fun onAttachmentsRemoved(attachmentId: AttachmentId) {
        viewModelScope.launch {
            deleteAttachment(attachmentId)
                .onLeft {
                    Timber.e("Failed to delete attachment: $it")
                    emitNewStateFor(EffectsEvent.AttachmentEvent.RemoveAttachmentError(it))
                }
        }
    }

    private fun onInlineImageRemoved(contentId: String) {
        viewModelScope.launch {
            deleteInlineAttachment(contentId)
                .onLeft { Timber.w("Failed to delete inline attachment: $contentId reason: $it") }
                .onRight {
                    Timber.d("Inline attachment $contentId removed!")
                    emitNewStateFor(EffectsEvent.AttachmentEvent.InlineAttachmentRemoved(contentId))
                }
        }
    }

    private suspend fun onCloseComposer() {
        emitNewStateFor(MainEvent.CoreLoadingToggled)

        val event = getDraftId().fold(
            ifLeft = { EffectsEvent.ComposerControlEvent.OnCloseRequest },
            ifRight = { EffectsEvent.ComposerControlEvent.OnCloseRequestWithDraft(it) }
        )
        emitNewStateFor(event)
    }

    private suspend fun handleOnSendMessage() {
        emitNewStateFor(MainEvent.CoreLoadingToggled)

        if (subjectTextField.text.isBlank()) {
            emitNewStateFor(CompositeEvent.OnSendWithEmptySubject)
            return
        }
        onSendMessage()
    }

    private suspend fun onSendMessage() {
        sendMessage().fold(
            ifLeft = {
                Timber.w("composer: Send message failed. Error: $it")
                emitNewStateFor(EffectsEvent.ErrorEvent.OnSendMessageError)
            },
            ifRight = {
                if (networkManager.isConnectedToNetwork()) {
                    emitNewStateFor(EffectsEvent.SendEvent.OnSendMessage)
                } else {
                    emitNewStateFor(EffectsEvent.SendEvent.OnOfflineSendMessage)
                }
            }
        )
    }

    private suspend fun handleOnScheduleSendMessage(time: Instant) {
        emitNewStateFor(MainEvent.CoreLoadingToggled)

        onScheduleSend(time)
    }

    private suspend fun onScheduleSend(time: Instant) = scheduleSend(time)
        .onLeft { emitNewStateFor(EffectsEvent.ErrorEvent.OnSendMessageError) }
        .onRight {
            if (networkManager.isConnectedToNetwork()) {
                emitNewStateFor(EffectsEvent.SendEvent.OnScheduleSendMessage)
            } else {
                emitNewStateFor(EffectsEvent.SendEvent.OnOfflineScheduleSendMessage)
            }
        }

    private fun onDiscardDraftConfirmed() {
        viewModelScope.launch {
            getDraftId()
                .onLeft { emitNewStateFor(EffectsEvent.ComposerControlEvent.OnCloseRequestWithDraftDiscarded) }
                .onRight {
                    discardDraft(primaryUserId(), it)
                        .onLeft { emitNewStateFor(EffectsEvent.ErrorEvent.OnDiscardDraftError) }
                        .onRight { emitNewStateFor(EffectsEvent.ComposerControlEvent.OnCloseRequestWithDraftDiscarded) }
                }
        }
    }

    private suspend fun onSubjectChanged(subject: Subject) = storeDraftWithSubject(subject)
        .onRight { savedStateHandle[ComposerScreen.HasSavedDraftKey] = true }
        .onLeft { emitNewStateFor(EffectsEvent.ErrorEvent.OnStoreSubjectError) }

    private suspend fun onDraftBodyChanged(updatedDraftBody: DraftBody) {
        val draftDisplayBody = buildDraftDisplayBody(MessageBodyWithType(updatedDraftBody.value, MimeTypeUiModel.Html))
        emitNewStateFor(MainEvent.OnDraftBodyUpdated(updatedDraftBody, draftDisplayBody))

        storeDraftWithBody(updatedDraftBody)
            .onLeft { emitNewStateFor(EffectsEvent.ErrorEvent.OnStoreBodyError) }

        savedStateHandle[ComposerScreen.HasSavedDraftKey] = true
    }

    private suspend fun primaryUserId() = primaryUserId.first()

    private fun currentSenderEmail() = SenderEmail(composerStates.value.main.fields.sender.email)

    private suspend fun contactsOrEmpty() = getContacts(primaryUserId()).getOrElse { emptyList() }

    private suspend fun onRecipientsChanged(recipients: RecipientsState) {
        val contacts = contactsOrEmpty()

        fun List<RecipientUiModel>.toRecipients(): List<Recipient> = this
            .filterIsInstance<RecipientUiModel.Valid>()
            .map { uiModel ->
                participantMapper.recipientUiModelToParticipant(uiModel, contacts)
            }

        updateRecipients(
            recipients.toRecipients.toRecipients(),
            recipients.ccRecipients.toRecipients(),
            recipients.bccRecipients.toRecipients()
        ).fold(
            ifRight = { emitNewStateFor(MainEvent.RecipientsChanged(recipientsStateManager.hasValidRecipients())) },
            ifLeft = { emitNewStateFor(EffectsEvent.ErrorEvent.OnStoreRecipientError) }
        )

        savedStateHandle[ComposerScreen.HasSavedDraftKey] = true
    }

    private fun initComposerFields(draftFields: DraftFields) {
        subjectTextField.replaceText(draftFields.subject.value)
        recipientsStateManager.setFromParticipants(
            toRecipients = draftFields.recipientsTo.value,
            ccRecipients = draftFields.recipientsCc.value,
            bccRecipients = draftFields.recipientsBcc.value
        )
    }

    private fun emitNewStateFor(event: ComposerStateEvent) {
        mutableComposerStates.update { composerStateReducer.reduceNewState(it, event) }
    }

    private fun ComposeToAddresses.extractRecipients(): List<RecipientUiModel> {
        return this.recipients.map { recipient ->
            when {
                validateEmailAddress(recipient) -> RecipientUiModel.Valid(recipient)
                else -> RecipientUiModel.Invalid(recipient)
            }
        }
    }

    private fun String.stripNewLines() = this.replace("[\n\r]".toRegex(), " ")

    private fun TextFieldState.replaceText(text: String, resetRange: Boolean = false) {
        clearText()
        edit {
            append(text)
            if (resetRange) selection = TextRange.Zero
        }
    }

    private fun logViewModelAction(action: ComposerAction, message: String) {
        Timber
            .tag("ComposerViewModel")
            .d("Action ${action::class.java.simpleName} ${System.identityHashCode(action)} - $message")
    }

    @AssistedFactory
    interface Factory {

        fun create(recipientsStateManager: RecipientsStateManager): ComposerViewModel
    }

    companion object {

        internal const val maxContactAutocompletionCount = 100
    }
}
