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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.IntentShareInfo
import ch.protonmail.android.mailcommon.domain.model.decode
import ch.protonmail.android.mailcommon.domain.model.hasEmailData
import ch.protonmail.android.mailcommon.domain.network.NetworkManager
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.DraftFieldsWithSyncStatus
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.model.hasAnyRecipient
import ch.protonmail.android.mailcomposer.domain.usecase.CreateDraftForAction
import ch.protonmail.android.mailcomposer.domain.usecase.CreateEmptyDraft
import ch.protonmail.android.mailcomposer.domain.usecase.DeleteAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.DeleteInlineAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.DiscardDraft
import ch.protonmail.android.mailcomposer.domain.usecase.GetDraftId
import ch.protonmail.android.mailcomposer.domain.usecase.GetEmbeddedImage
import ch.protonmail.android.mailcomposer.domain.usecase.IsValidEmailAddress
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageAttachments
import ch.protonmail.android.mailcomposer.domain.usecase.OpenExistingDraft
import ch.protonmail.android.mailcomposer.domain.usecase.SendMessage
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithBody
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithSubject
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateRecipients
import ch.protonmail.android.mailcomposer.presentation.mapper.ParticipantMapper
import ch.protonmail.android.mailcomposer.presentation.model.ComposerAction
import ch.protonmail.android.mailcomposer.presentation.model.ComposerDraftState
import ch.protonmail.android.mailcomposer.presentation.model.ComposerEvent
import ch.protonmail.android.mailcomposer.presentation.model.ComposerOperation
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsState
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsStateManager
import ch.protonmail.android.mailcomposer.presentation.reducer.ComposerReducer
import ch.protonmail.android.mailcomposer.presentation.ui.ComposerScreen
import ch.protonmail.android.mailcomposer.presentation.usecase.AddAttachment
import ch.protonmail.android.mailcomposer.presentation.usecase.BuildDraftDisplayBody
import ch.protonmail.android.mailcontact.domain.usecase.GetContacts
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsChooseAttachmentSourceEnabled
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.takeIfNotEmpty
import timber.log.Timber

@Suppress("LargeClass", "LongParameterList", "TooManyFunctions", "UnusedPrivateMember")
@HiltViewModel(assistedFactory = ComposerViewModel.Factory::class)
class ComposerViewModel @AssistedInject constructor(
    private val storeDraftWithBody: StoreDraftWithBody,
    private val storeDraftWithSubject: StoreDraftWithSubject,
    private val updateRecipients: UpdateRecipients,
    private val getContacts: GetContacts,
    private val participantMapper: ParticipantMapper,
    private val reducer: ComposerReducer,
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
    @IsChooseAttachmentSourceEnabled private val chooseAttachmentSourceEnabled: Flow<Boolean>,
    observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    internal val subjectTextField = TextFieldState()

    private val actionMutex = Mutex()
    private val primaryUserId = observePrimaryUserId().filterNotNull()

    private val mutableState = MutableStateFlow(ComposerDraftState.initial())
    val state: StateFlow<ComposerDraftState> = mutableState

    val isChooseAttachmentSourceEnabled = chooseAttachmentSourceEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = false
    )

    init {
        viewModelScope.launch {
            if (!setupInitialState(savedStateHandle)) return@launch

            observeAttachments()
            observeComposerSubject()
            observeComposerRecipients()
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

            inputDraftId != null -> prefillWithExistingDraft(inputDraftId)
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
        emitNewStateFor(ComposerAction.OnCloseComposer)
    }

    private fun prefillForComposeToAction(recipients: List<RecipientUiModel>) {
        viewModelScope.launch {
            recipientsStateManager.updateRecipients(recipients, ContactSuggestionsField.TO)
        }
    }

    private suspend fun prefillForNewDraft() {
        // Emitting also for "empty draft" as now signature is returned with the init body, effectively
        // making this the same as other prefill cases (eg. "reply" or "fw")
        emitNewStateFor(ComposerEvent.OpenDraft)

        createEmptyDraft(primaryUserId())
            .onRight { draftFields ->
                emitNewStateFor(
                    ComposerEvent.PrefillDraftDataReceived(
                        draftUiModel = draftFields.toDraftUiModel(),
                        isDataRefreshed = true,
                        isBlockedSendingFromPmAddress = false,
                        isBlockedSendingFromDisabledAddress = false,
                        bodyShouldTakeFocus = false
                    )
                )
            }
            .onLeft { emitNewStateFor(ComposerEvent.ErrorLoadingDefaultSenderAddress) }
    }

    private suspend fun prefillForShareDraftAction(shareDraftAction: PrefillForShare) {
        val fileShareInfo = shareDraftAction.intentShareInfo.decode()

        fileShareInfo.attachmentUris.takeIfNotEmpty()?.let { rawUri ->
            Timber.w("composer: storing attachment not implemented")
            val uriList = rawUri.map { Uri.parse(it) }
            onAttachmentsAdded(uriList)
        }

        if (fileShareInfo.hasEmailData()) {
            val draftFields = prepareDraftFieldsFor(fileShareInfo)
            initComposerFields(draftFields)
            emitNewStateFor(
                ComposerEvent.PrefillDataReceivedViaShare(draftFields.toDraftUiModel())
            )
        }
    }

    private suspend fun prepareDraftFieldsFor(intentShareInfo: IntentShareInfo): DraftFields {
        val draftBody = DraftBody(intentShareInfo.emailBody ?: "")
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
        emitNewStateFor(ComposerEvent.OpenDraft)
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
                        ComposerEvent.PrefillDraftDataReceived(
                            draftUiModel = draftFields.toDraftUiModel(),
                            isDataRefreshed = true,
                            isBlockedSendingFromPmAddress = false,
                            isBlockedSendingFromDisabledAddress = false,
                            bodyShouldTakeFocus = focusDraftBody
                        )
                    )
                }
                .onLeft { emitNewStateFor(ComposerEvent.ErrorLoadingParentMessageData) }

            is PrefillForShare -> prefillForShareDraftAction(draftAction)
        }
    }

    private suspend fun prefillWithExistingDraft(inputDraftId: String) {
        Timber.d("Opening composer with $inputDraftId")
        emitNewStateFor(ComposerEvent.OpenDraft)

        openExistingDraft(primaryUserId(), MessageId(inputDraftId))
            .onRight { draftFieldsWithSyncStatus ->
                val draftFields = draftFieldsWithSyncStatus.draftFields
                initComposerFields(draftFields)
                emitNewStateFor(
                    ComposerEvent.PrefillDraftDataReceived(
                        draftUiModel = draftFields.toDraftUiModel(),
                        isDataRefreshed = draftFieldsWithSyncStatus is DraftFieldsWithSyncStatus.Remote,
                        isBlockedSendingFromPmAddress = false,
                        isBlockedSendingFromDisabledAddress = false,
                        bodyShouldTakeFocus = draftFields.hasAnyRecipient()
                    )
                )
            }
            .onLeft { emitNewStateFor(ComposerEvent.ErrorLoadingDraftData) }
    }

    private fun DraftFields.toDraftUiModel(): DraftUiModel {
        val messageBodyWithType = MessageBodyWithType(this.body.value, MimeTypeUiModel.Html)
        val draftDisplayBody = buildDraftDisplayBody(messageBodyWithType)

        return DraftUiModel(this, draftDisplayBody)
    }

    @Suppress("ComplexMethod")
    internal fun submit(action: ComposerAction) {
        viewModelScope.launch {
            actionMutex.withLock {
                when (action) {
                    is ComposerAction.AttachmentsAdded -> onAttachmentsAdded(action.uriList)
                    is ComposerAction.DraftBodyChanged -> onDraftBodyChanged(action)
                    is ComposerAction.SenderChanged -> TODO()
                    is ComposerAction.ChangeSenderRequested -> TODO()

                    is ComposerAction.OnAddAttachments -> emitNewStateFor(action)
                    is ComposerAction.OnCloseComposer -> emitNewStateFor(onCloseComposer(action))
                    is ComposerAction.OnSendMessage -> emitNewStateFor(handleOnSendMessage())
                    is ComposerAction.ConfirmSendingWithoutSubject -> emitNewStateFor(onSendMessage())
                    is ComposerAction.RejectSendingWithoutSubject -> emitNewStateFor(action)
                    is ComposerAction.RemoveAttachment -> onAttachmentsRemoved(action)
                    is ComposerAction.OnSetExpirationTimeRequested -> TODO()
                    is ComposerAction.ExpirationTimeSet -> TODO()
                    is ComposerAction.SendExpiringMessageToExternalRecipientsConfirmed -> emitNewStateFor(
                        onSendMessage()
                    )

                    is ComposerAction.DiscardDraft -> emitNewStateFor(action)
                    is ComposerAction.DiscardDraftConfirmed -> onDiscardDraftConfirmed(action)
                    is ComposerAction.RemoveInlineImage -> onInlineImageRemoved(action)
                    is ComposerAction.OnInlineImageActionsRequested -> emitNewStateFor(action)
                    is ComposerAction.OnAttachFromFiles -> emitNewStateFor(action)
                }
            }
        }
    }

    fun loadEmbeddedImage(contentId: String): EmbeddedImage? = getEmbeddedImage(contentId).getOrNull()

    private fun observeAttachments() {
        viewModelScope.launch {
            observeMessageAttachments().onEach { result ->
                result.onLeft {
                    Timber.e("Failed to observe message attachments: $it")
                    emitNewStateFor(ComposerEvent.ErrorLoadingDraftData)
                }.onRight { attachments ->
                    emitNewStateFor(ComposerEvent.OnAttachmentsUpdated(attachments))
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
                    emitNewStateFor(ComposerEvent.AddAttachmentError(it))
                }.onRight {
                    when (it) {
                        is AddAttachment.AddAttachmentResult.InlineAttachmentAdded ->
                            emitNewStateFor(ComposerEvent.InlineAttachmentAdded(it.cid))
                        AddAttachment.AddAttachmentResult.StandardAttachmentAdded -> Unit
                    }
                }
            }
        }
    }

    private fun onAttachmentsRemoved(action: ComposerAction.RemoveAttachment) {
        viewModelScope.launch {
            deleteAttachment(action.attachmentId)
                .onLeft {
                    Timber.e("Failed to delete attachment: $it")
                    emitNewStateFor(ComposerEvent.DeleteAttachmentError)
                }
        }
    }

    private fun onInlineImageRemoved(action: ComposerAction.RemoveInlineImage) {
        viewModelScope.launch {
            deleteInlineAttachment(action.contentId)
                .onLeft { Timber.w("Failed to delete inline attachment: ${action.contentId} reason: $it") }
                .onRight {
                    Timber.d("Inline attachment ${action.contentId} removed!")
                    emitNewStateFor(ComposerEvent.InlineAttachmentRemoved(action.contentId))
                }
        }
    }

    private suspend fun onCloseComposer(action: ComposerAction.OnCloseComposer): ComposerOperation {
        emitNewStateFor(ComposerEvent.OnMessageSending)

        return getDraftId().fold(
            ifLeft = { action },
            ifRight = { ComposerEvent.OnCloseWithDraftSaved(it) }
        )
    }

    private suspend fun handleOnSendMessage(): ComposerOperation {
        emitNewStateFor(ComposerEvent.OnMessageSending)

        if (subjectTextField.text.isBlank()) {
            return ComposerEvent.ConfirmEmptySubject
        }
        return onSendMessage()
    }

    private suspend fun onSendMessage(): ComposerOperation = sendMessage().fold(
        ifLeft = {
            Timber.w("composer: Send message failed. Error: $it")
            ComposerEvent.OnSendingError(TextUiModel(it.toString()))
        },
        ifRight = {
            return if (networkManager.isConnectedToNetwork()) {
                ComposerAction.OnSendMessage
            } else {
                ComposerEvent.OnSendMessageOffline
            }
        }
    )

    private fun onDiscardDraftConfirmed(action: ComposerAction.DiscardDraftConfirmed) {
        viewModelScope.launch {
            getDraftId().fold(
                ifLeft = { emitNewStateFor(ComposerEvent.ErrorDiscardingDraft) },
                ifRight = {
                    discardDraft(primaryUserId(), it)
                    emitNewStateFor(action)
                }
            )
        }
    }

    private suspend fun onSubjectChanged(subject: Subject) = storeDraftWithSubject(subject).onLeft {
        emitNewStateFor(ComposerEvent.ErrorStoringDraftSubject)
        savedStateHandle[ComposerScreen.HasSavedDraftKey] = true
    }

    private suspend fun onDraftBodyChanged(action: ComposerAction.DraftBodyChanged) {
        val updatedDraftBody = action.draftBody
        val draftDisplayBody = buildDraftDisplayBody(MessageBodyWithType(updatedDraftBody.value, MimeTypeUiModel.Html))
        emitNewStateFor(ComposerEvent.OnDraftBodyUpdated(updatedDraftBody, draftDisplayBody))

        storeDraftWithBody(
            updatedDraftBody
        ).onLeft { emitNewStateFor(ComposerEvent.ErrorStoringDraftBody) }

        savedStateHandle[ComposerScreen.HasSavedDraftKey] = true
    }

    private suspend fun primaryUserId() = primaryUserId.first()

    private fun currentSenderEmail() = SenderEmail(state.value.fields.sender.email)

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
            ifLeft = { emitNewStateFor(ComposerEvent.ErrorStoringDraftRecipients) },
            ifRight = { emitNewStateFor(ComposerEvent.RecipientsUpdated(recipientsStateManager.hasValidRecipients())) }
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

    private fun emitNewStateFor(operation: ComposerOperation) {
        val currentState = state.value
        mutableState.value = reducer.newStateFrom(currentState, operation)
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

    @AssistedFactory
    interface Factory {

        fun create(recipientsStateManager: RecipientsStateManager): ComposerViewModel
    }

    companion object {

        internal const val maxContactAutocompletionCount = 100
    }
}
