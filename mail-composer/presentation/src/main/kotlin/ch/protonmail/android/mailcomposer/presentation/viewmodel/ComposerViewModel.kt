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

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.IntentShareInfo
import ch.protonmail.android.mailcommon.domain.model.decode
import ch.protonmail.android.mailcommon.domain.model.hasEmailData
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.usecase.AddAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.ClearMessageSendingError
import ch.protonmail.android.mailcomposer.domain.usecase.CreateDraftForAction
import ch.protonmail.android.mailcomposer.domain.usecase.CreateEmptyDraft
import ch.protonmail.android.mailcomposer.domain.usecase.DeleteAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.GetExternalRecipients
import ch.protonmail.android.mailcomposer.domain.usecase.IsValidEmailAddress
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageAttachments
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessagePassword
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageSendingError
import ch.protonmail.android.mailcomposer.domain.usecase.OpenExistingDraft
import ch.protonmail.android.mailcomposer.domain.usecase.ProvideNewDraftId
import ch.protonmail.android.mailcomposer.domain.usecase.SaveMessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.usecase.SendMessage
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithBody
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithSubject
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateRecipients2
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
import ch.protonmail.android.mailcomposer.presentation.usecase.BuildDraftDisplayBody
import ch.protonmail.android.mailcomposer.presentation.usecase.FormatMessageSendingError
import ch.protonmail.android.mailcontact.domain.usecase.GetContacts
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.MimeTypeUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.idlingresources.ComposerIdlingResource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.proton.core.network.domain.NetworkManager
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.takeIfNotEmpty
import timber.log.Timber
import kotlin.time.Duration

@Suppress("LongParameterList", "TooManyFunctions", "UnusedPrivateMember")
@HiltViewModel(assistedFactory = ComposerViewModel.Factory::class)
class ComposerViewModel @AssistedInject constructor(
    private val storeDraftWithBody: StoreDraftWithBody,
    private val storeDraftWithSubject: StoreDraftWithSubject,
    private val updateRecipients2: UpdateRecipients2,
    private val getContacts: GetContacts,
    private val participantMapper: ParticipantMapper,
    private val reducer: ComposerReducer,
    private val isValidEmailAddress: IsValidEmailAddress,
    private val composerIdlingResource: ComposerIdlingResource,
    private val observeMessageAttachments: ObserveMessageAttachments,
    private val observeMessageSendingError: ObserveMessageSendingError,
    private val clearMessageSendingError: ClearMessageSendingError,
    private val formatMessageSendingError: FormatMessageSendingError,
    private val sendMessage: SendMessage,
    private val networkManager: NetworkManager,
    private val addAttachment: AddAttachment,
    private val deleteAttachment: DeleteAttachment,
    private val observeMessagePassword: ObserveMessagePassword,
    private val saveMessageExpirationTime: SaveMessageExpirationTime,
    private val observeMessageExpirationTime: ObserveMessageExpirationTime,
    private val getExternalRecipients: GetExternalRecipients,
    private val openExistingDraft: OpenExistingDraft,
    private val createEmptyDraft: CreateEmptyDraft,
    private val createDraftForAction: CreateDraftForAction,
    private val buildDraftDisplayBody: BuildDraftDisplayBody,
    @Assisted private val recipientsStateManager: RecipientsStateManager,
    savedStateHandle: SavedStateHandle,
    observePrimaryUserId: ObservePrimaryUserId,
    provideNewDraftId: ProvideNewDraftId
) : ViewModel() {


    internal val subjectTextField = TextFieldState()

    private val actionMutex = Mutex()
    private val primaryUserId = observePrimaryUserId().filterNotNull()

    private val mutableState = MutableStateFlow(
        ComposerDraftState.initial(
            MessageId(savedStateHandle.get<String>(ComposerScreen.DraftMessageIdKey) ?: provideNewDraftId().id)
        )
    )
    val state: StateFlow<ComposerDraftState> = mutableState

    init {
        viewModelScope.launch {
            if (!setupInitialState(savedStateHandle)) return@launch

            observeAttachments()
            observeSendingError()
            observeComposerSubject()
            observeComposerRecipients()

            // Avoid observing unimplemented features as that causes warnings reports to Sentry.
//        observeMessageAttachments()
//        observeMessagePassword()
//        observeMessageExpirationTime()
        }
    }

    private suspend fun setupInitialState(savedStateHandle: SavedStateHandle): Boolean {
        val inputDraftId = savedStateHandle.get<String>(ComposerScreen.DraftMessageIdKey)
        val draftAction = savedStateHandle.get<String>(ComposerScreen.SerializedDraftActionKey)
            ?.deserialize<DraftAction>()

        when {
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

    private fun prefillForComposeToAction(recipients: List<RecipientUiModel>) {
        viewModelScope.launch {
            recipientsStateManager.updateRecipients(recipients, ContactSuggestionsField.TO)
        }
    }

    private suspend fun prefillForNewDraft() {
        createEmptyDraft(primaryUserId())
            .onRight { draftFields ->
                emitNewStateFor(
                    ComposerEvent.PrefillDraftDataReceived(
                        draftUiModel = draftFields.toDraftUiModel(),
                        isDataRefreshed = true,
                        isBlockedSendingFromPmAddress = false,
                        isBlockedSendingFromDisabledAddress = false
                    )
                )
            }
            .onLeft { emitNewStateFor(ComposerEvent.ErrorLoadingDefaultSenderAddress) }
    }

    @MissingRustApi
    // Storing of attachments not implemented
    private suspend fun prefillForShareDraftAction(shareDraftAction: DraftAction.PrefillForShare) {
        val fileShareInfo = shareDraftAction.intentShareInfo.decode()

        fileShareInfo.attachmentUris.takeIfNotEmpty()?.let { uris ->
            Timber.w("composer: storing attachment not implemented")
            emitNewStateFor(ComposerEvent.ErrorAttachmentsExceedSizeLimit)
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
        Timber.d("Opening composer for draft action $draftAction / ${currentMessageId()}")
        emitNewStateFor(ComposerEvent.OpenWithMessageAction(currentMessageId(), draftAction))

        when (draftAction) {
            DraftAction.Compose -> prefillForNewDraft()
            is DraftAction.ComposeToAddresses -> {
                Timber.d("composer: prefilling for compose To")
                prefillForNewDraft()
                prefillForComposeToAction(draftAction.extractRecipients())
            }

            is DraftAction.Forward,
            is DraftAction.Reply,
            is DraftAction.ReplyAll -> createDraftForAction(primaryUserId(), draftAction)
                .onRight { draftFields ->
                    initComposerFields(draftFields)
                    emitNewStateFor(
                        ComposerEvent.PrefillDraftDataReceived(
                            draftUiModel = draftFields.toDraftUiModel(),
                            isDataRefreshed = true,
                            isBlockedSendingFromPmAddress = false,
                            isBlockedSendingFromDisabledAddress = false
                        )
                    )
                }
                .onLeft { emitNewStateFor(ComposerEvent.ErrorLoadingParentMessageData) }

            is DraftAction.PrefillForShare -> prefillForShareDraftAction(draftAction)
        }
    }

    @MissingRustApi
    // isDataRefresh param of event is hardcoded to true
    private suspend fun prefillWithExistingDraft(inputDraftId: String) {
        Timber.d("Opening composer with $inputDraftId / ${currentMessageId()}")
        emitNewStateFor(ComposerEvent.OpenExistingDraft(currentMessageId()))

        openExistingDraft(primaryUserId(), MessageId(inputDraftId))
            .onRight { draftFields ->
                initComposerFields(draftFields)
                emitNewStateFor(
                    ComposerEvent.PrefillDraftDataReceived(
                        draftUiModel = draftFields.toDraftUiModel(),
                        isDataRefreshed = true,
                        isBlockedSendingFromPmAddress = false,
                        isBlockedSendingFromDisabledAddress = false
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

    override fun onCleared() {
        super.onCleared()
        composerIdlingResource.clear()
    }

    @Suppress("ComplexMethod")
    internal fun submit(action: ComposerAction) {
        viewModelScope.launch {
            actionMutex.withLock {
                composerIdlingResource.increment()
                when (action) {
                    is ComposerAction.AttachmentsAdded -> onAttachmentsAdded(action)
                    is ComposerAction.DraftBodyChanged -> onDraftBodyChanged(action)
                    is ComposerAction.SenderChanged -> TODO()
                    is ComposerAction.ChangeSenderRequested -> TODO()

                    is ComposerAction.OnAddAttachments -> emitNewStateFor(action)
                    is ComposerAction.OnCloseComposer -> emitNewStateFor(onCloseComposer(action))
                    is ComposerAction.OnSendMessage -> emitNewStateFor(handleOnSendMessage(action))
                    is ComposerAction.ConfirmSendingWithoutSubject -> emitNewStateFor(onSendMessage(action))
                    is ComposerAction.RejectSendingWithoutSubject -> emitNewStateFor(action)
                    is ComposerAction.RemoveAttachment -> onAttachmentsRemoved(action)
                    is ComposerAction.OnSetExpirationTimeRequested -> TODO()
                    is ComposerAction.ExpirationTimeSet -> TODO()
                    is ComposerAction.SendExpiringMessageToExternalRecipientsConfirmed -> emitNewStateFor(
                        onSendMessage(action)
                    )
                }
                composerIdlingResource.decrement()
            }
        }
    }

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

    private fun observeSendingError() {
        primaryUserId
            .flatMapLatest { userId -> observeMessageSendingError(userId, currentMessageId()) }
            .onEach {
                formatMessageSendingError(it)?.run {
                    emitNewStateFor(ComposerEvent.OnSendingError(TextUiModel.Text(this)))
                }
            }
            .launchIn(viewModelScope)
    }

    @MissingRustApi
    // Message password not implemented
    private fun observeMessagePassword() {
        primaryUserId
            .flatMapLatest { userId -> observeMessagePassword(userId, currentMessageId()) }
            .onEach { emitNewStateFor(ComposerEvent.OnMessagePasswordUpdated(it)) }
            .launchIn(viewModelScope)
    }

    @MissingRustApi
    // Message expiration not implemented
    private fun observeMessageExpirationTime() {
        primaryUserId
            .flatMapLatest { userId -> observeMessageExpirationTime(userId, currentMessageId()) }
            .onEach { emitNewStateFor(ComposerEvent.OnMessageExpirationTimeUpdated(it)) }
            .launchIn(viewModelScope)
    }

    private fun validateEmailAddress(emailAddress: String): Boolean = isValidEmailAddress(emailAddress)

    fun clearSendingError() {
        viewModelScope.launch {
            clearMessageSendingError(primaryUserId(), currentMessageId()).onLeft {
                Timber.e("Failed to clear SendingError: $it")
            }
        }
    }

    private fun onAttachmentsAdded(action: ComposerAction.AttachmentsAdded) {
        viewModelScope.launch {
            action.uriList.forEach {
                addAttachment(it)
                    .onLeft { Timber.e("Failed to add attachment: $it") }
            }
        }
    }

    private fun onAttachmentsRemoved(action: ComposerAction.RemoveAttachment) {
        viewModelScope.launch {
            deleteAttachment(action.attachmentId)
                .onLeft { Timber.e("Failed to delete attachment: $it") }
        }
    }

    private suspend fun onCloseComposer(action: ComposerAction.OnCloseComposer): ComposerOperation {
        val draftFields = buildDraftFields()
        return when {
            draftFields.areBlank() -> action

            else -> ComposerEvent.OnCloseWithDraftSaved
        }
    }

    private suspend fun handleOnSendMessage(action: ComposerAction.OnSendMessage): ComposerOperation {
        val draftFields = buildDraftFields()
        val isMessageExpirationSet = state.value.messageExpiresIn != Duration.ZERO
        val isMessagePasswordSet = state.value.isMessagePasswordSet
        val externalRecipients = draftFields.let {
            getExternalRecipients(primaryUserId(), it.recipientsTo, it.recipientsCc, it.recipientsBcc)
        }
        val isClearTextExpiringMessageToExternal = isMessageExpirationSet &&
            isMessagePasswordSet.not() &&
            externalRecipients.isNotEmpty()

        return when {
            draftFields.haveBlankSubject() -> ComposerEvent.ConfirmEmptySubject
            isClearTextExpiringMessageToExternal -> {
                ComposerEvent.ConfirmSendExpiringMessageToExternalRecipients(externalRecipients)
            }

            else -> onSendMessage(action)
        }
    }

    private suspend fun onSendMessage(action: ComposerOperation): ComposerOperation {
        val draftFields = buildDraftFields()
        return when {
            draftFields.areBlank() -> action
            else -> {
                viewModelScope.launch {
                    withContext(NonCancellable) {
                        sendMessage(primaryUserId())
                    }
                }

                if (networkManager.isConnectedToNetwork()) {
                    ComposerAction.OnSendMessage
                } else {
                    ComposerEvent.OnSendMessageOffline
                }
            }
        }
    }

    private suspend fun buildDraftFields() = DraftFields(
        currentSenderEmail(),
        currentSubject(),
        currentDraftBody(),
        currentValidRecipientsTo(),
        currentValidRecipientsCc(),
        currentValidRecipientsBcc()
    )

    private suspend fun onSubjectChanged(subject: Subject) = storeDraftWithSubject(subject)
        .onLeft {
            Timber.e("Store draft ${currentMessageId()} with new subject $subject failed")
            emitNewStateFor(ComposerEvent.ErrorStoringDraftSubject)
        }

    private suspend fun onDraftBodyChanged(action: ComposerAction.DraftBodyChanged) {
        emitNewStateFor(ComposerAction.DraftBodyChanged(action.draftBody))

        storeDraftWithBody(
            action.draftBody
        ).onLeft { emitNewStateFor(ComposerEvent.ErrorStoringDraftBody) }
    }

    private suspend fun primaryUserId() = primaryUserId.first()

    private fun currentSubject() = Subject(subjectTextField.text.toString())

    private fun currentDraftBody() = DraftBody(state.value.fields.body)

    private fun currentSenderEmail() = SenderEmail(state.value.fields.sender.email)

    private fun currentMessageId() = state.value.fields.draftId

    private suspend fun currentValidRecipientsTo(): RecipientsTo {
        val contacts = contactsOrEmpty()
        return RecipientsTo(
            recipientsStateManager.recipients.value.toRecipients
                .filterIsInstance<RecipientUiModel.Valid>()
                .map { participantMapper.recipientUiModelToParticipant(it, contacts) }
        )
    }

    private suspend fun currentValidRecipientsCc(): RecipientsCc {
        val contacts = contactsOrEmpty()
        return RecipientsCc(
            recipientsStateManager.recipients.value.ccRecipients
                .filterIsInstance<RecipientUiModel.Valid>()
                .map { participantMapper.recipientUiModelToParticipant(it, contacts) }
        )
    }

    private suspend fun currentValidRecipientsBcc(): RecipientsBcc {
        val contacts = contactsOrEmpty()
        return RecipientsBcc(
            recipientsStateManager.recipients.value.bccRecipients
                .filterIsInstance<RecipientUiModel.Valid>()
                .map { participantMapper.recipientUiModelToParticipant(it, contacts) }
        )
    }

    private suspend fun contactsOrEmpty() = getContacts(primaryUserId()).getOrElse { emptyList() }

    private suspend fun onRecipientsChanged(recipients: RecipientsState) {
        val contacts = contactsOrEmpty()

        fun List<RecipientUiModel>.toRecipients(): List<Recipient> = this
            .filterIsInstance<RecipientUiModel.Valid>()
            .map { uiModel ->
                participantMapper.recipientUiModelToParticipant(uiModel, contacts)
            }

        updateRecipients2(
            recipients.toRecipients.toRecipients(),
            recipients.ccRecipients.toRecipients(),
            recipients.bccRecipients.toRecipients()
        ).fold(
            ifLeft = { emitNewStateFor(ComposerEvent.ErrorStoringDraftRecipients) },
            ifRight = { emitNewStateFor(ComposerEvent.RecipientsUpdated(recipientsStateManager.hasValidRecipients())) }
        )
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

    private fun DraftAction.ComposeToAddresses.extractRecipients(): List<RecipientUiModel> {
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
