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
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateBccRecipients
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateCcRecipients
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateToRecipients
import ch.protonmail.android.mailcomposer.presentation.mapper.ContactSuggestionsMapper
import ch.protonmail.android.mailcomposer.presentation.mapper.ParticipantMapper
import ch.protonmail.android.mailcomposer.presentation.model.ComposerAction
import ch.protonmail.android.mailcomposer.presentation.model.ComposerDraftState
import ch.protonmail.android.mailcomposer.presentation.model.ComposerEvent
import ch.protonmail.android.mailcomposer.presentation.model.ComposerOperation
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcomposer.presentation.reducer.ComposerReducer
import ch.protonmail.android.mailcomposer.presentation.ui.ComposerScreen
import ch.protonmail.android.mailcomposer.presentation.usecase.BuildDraftDisplayBody
import ch.protonmail.android.mailcomposer.presentation.usecase.FormatMessageSendingError
import ch.protonmail.android.mailcontact.domain.DeviceContactsSuggestionsPrompt
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.usecase.GetContactSuggestions
import ch.protonmail.android.mailcontact.domain.usecase.GetContacts
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.MimeTypeUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.idlingresources.ComposerIdlingResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
import javax.inject.Inject
import kotlin.time.Duration

@Suppress("LongParameterList", "TooManyFunctions", "UnusedPrivateMember")
@HiltViewModel
class ComposerViewModel @Inject constructor(
    private val storeDraftWithBody: StoreDraftWithBody,
    private val storeDraftWithSubject: StoreDraftWithSubject,
    private val updateToRecipients: UpdateToRecipients,
    private val updateCcRecipients: UpdateCcRecipients,
    private val updateBccRecipients: UpdateBccRecipients,
    private val getContacts: GetContacts,
    private val getContactSuggestions: GetContactSuggestions,
    private val deviceContactsSuggestionsPrompt: DeviceContactsSuggestionsPrompt,
    private val participantMapper: ParticipantMapper,
    private val contactSuggestionsMapper: ContactSuggestionsMapper,
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
    savedStateHandle: SavedStateHandle,
    observePrimaryUserId: ObservePrimaryUserId,
    provideNewDraftId: ProvideNewDraftId
) : ViewModel() {


    internal val subjectTextField = TextFieldState()

    private val actionMutex = Mutex()
    private val primaryUserId = observePrimaryUserId().filterNotNull()

    private val getContactSuggestionsJobs = mutableMapOf<ContactSuggestionsField, Job>()
    private val mutableState = MutableStateFlow(
        ComposerDraftState.initial(
            MessageId(savedStateHandle.get<String>(ComposerScreen.DraftMessageIdKey) ?: provideNewDraftId().id)
        )
    )
    val state: StateFlow<ComposerDraftState> = mutableState

    init {
        val inputDraftId = savedStateHandle.get<String>(ComposerScreen.DraftMessageIdKey)
        val draftAction = savedStateHandle.get<String>(ComposerScreen.SerializedDraftActionKey)
            ?.deserialize<DraftAction>()

        when {
            inputDraftId != null -> prefillWithExistingDraft(inputDraftId)
            draftAction != null -> prefillForDraftAction(draftAction)
            else -> prefillForNewDraft()
        }

        observeAttachments()
        observeSendingError()
        observeDeviceContactsSuggestionsPromptEnabled()

        // Avoid observing unimplemented features as that causes warnings reports to Sentry.
//        observeMessageAttachments()
//        observeMessagePassword()
//        observeMessageExpirationTime()
    }

    private fun prefillForComposeToAction(recipients: List<RecipientUiModel>) {
        viewModelScope.launch {
            emitNewStateFor(onToChanged(recipients))
        }
    }

    private fun prefillForNewDraft() {
        viewModelScope.launch {
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
    }

    @MissingRustApi
    // Storing of attachments not implemented
    private fun prefillForShareDraftAction(shareDraftAction: DraftAction.PrefillForShare) {
        val fileShareInfo = shareDraftAction.intentShareInfo.decode()

        viewModelScope.launch {
            fileShareInfo.attachmentUris.takeIfNotEmpty()?.let { uris ->
                Timber.w("composer: storing attachment not implemented")
                emitNewStateFor(ComposerEvent.ErrorAttachmentsExceedSizeLimit)
            }

            if (fileShareInfo.hasEmailData()) {
                emitNewStateFor(
                    ComposerEvent.PrefillDataReceivedViaShare(
                        prepareDraftFieldsFor(fileShareInfo).toDraftUiModel()
                    )
                )
            }
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
    private fun prefillForDraftAction(draftAction: DraftAction) {
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
            is DraftAction.ReplyAll -> viewModelScope.launch {
                Timber.d("composer: prefilling for reply / fw action")
                createDraftForAction(primaryUserId(), draftAction)
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
                    .onLeft { emitNewStateFor(ComposerEvent.ErrorLoadingParentMessageData) }
            }

            is DraftAction.PrefillForShare -> prefillForShareDraftAction(draftAction)
        }
    }

    @MissingRustApi
    // isDataRefresh param of event is hardcoded to true
    private fun prefillWithExistingDraft(inputDraftId: String) {
        Timber.d("Opening composer with $inputDraftId / ${currentMessageId()}")
        emitNewStateFor(ComposerEvent.OpenExistingDraft(currentMessageId()))

        viewModelScope.launch {
            openExistingDraft(primaryUserId(), MessageId(inputDraftId))
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
                .onLeft { emitNewStateFor(ComposerEvent.ErrorLoadingDraftData) }
        }
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
                    is ComposerAction.SubjectChanged -> emitNewStateFor(onSubjectChanged(action))
                    is ComposerAction.ChangeSenderRequested -> TODO()
                    is ComposerAction.RecipientsToChanged -> emitNewStateFor(onToChanged(action.recipients))
                    is ComposerAction.RecipientsCcChanged -> emitNewStateFor(onCcChanged(action.recipients))
                    is ComposerAction.RecipientsBccChanged -> emitNewStateFor(onBccChanged(action.recipients))
                    is ComposerAction.ContactSuggestionTermChanged -> onSearchTermChanged(
                        action.searchTerm,
                        action.suggestionsField
                    )

                    is ComposerAction.ContactSuggestionSelected -> handleContactSuggestionSelected(action)

                    is ComposerAction.ContactSuggestionsDismissed -> emitNewStateFor(action)
                    is ComposerAction.DeviceContactsPromptDenied -> onDeviceContactsPromptDenied()
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

    @Suppress("FunctionMaxLength")
    private fun observeDeviceContactsSuggestionsPromptEnabled() {
        viewModelScope.launch {
            emitNewStateFor(
                ComposerEvent.OnIsDeviceContactsSuggestionsPromptEnabled(
                    deviceContactsSuggestionsPrompt.getPromptEnabled()
                )
            )
        }
    }

    fun validateEmailAddress(emailAddress: String): Boolean = isValidEmailAddress(emailAddress)

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

    private fun onDeviceContactsPromptDenied() {
        viewModelScope.launch {
            deviceContactsSuggestionsPrompt.setPromptDisabled()
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

    private suspend fun onSubjectChanged(action: ComposerAction.SubjectChanged): ComposerOperation =
        storeDraftWithSubject(action.subject).fold(
            ifLeft = {
                Timber.e("Store draft ${currentMessageId()} with new subject ${action.subject} failed")
                ComposerEvent.ErrorStoringDraftSubject
            },
            ifRight = { action }
        )

    private suspend fun onDraftBodyChanged(action: ComposerAction.DraftBodyChanged) {
        emitNewStateFor(ComposerAction.DraftBodyChanged(action.draftBody))

        storeDraftWithBody(
            action.draftBody
        ).onLeft { emitNewStateFor(ComposerEvent.ErrorStoringDraftBody) }
    }

    private suspend fun primaryUserId() = primaryUserId.first()

    private fun currentSubject() = Subject(state.value.fields.subject)

    private fun currentDraftBody() = DraftBody(state.value.fields.body)

    private fun currentSenderEmail() = SenderEmail(state.value.fields.sender.email)

    private fun currentMessageId() = state.value.fields.draftId

    private suspend fun currentValidRecipientsTo(): RecipientsTo {
        val contacts = contactsOrEmpty()
        return RecipientsTo(
            state.value.fields.to.filterIsInstance<RecipientUiModel.Valid>().map {
                participantMapper.recipientUiModelToParticipant(it, contacts)
            }
        )
    }

    private suspend fun currentValidRecipientsCc(): RecipientsCc {
        val contacts = contactsOrEmpty()
        return RecipientsCc(
            state.value.fields.cc.filterIsInstance<RecipientUiModel.Valid>().map {
                participantMapper.recipientUiModelToParticipant(it, contacts)
            }
        )
    }

    private suspend fun currentValidRecipientsBcc(): RecipientsBcc {
        val contacts = contactsOrEmpty()
        return RecipientsBcc(
            state.value.fields.bcc.filterIsInstance<RecipientUiModel.Valid>().map {
                participantMapper.recipientUiModelToParticipant(it, contacts)
            }
        )
    }

    private suspend fun contactsOrEmpty() = getContacts(primaryUserId()).getOrElse { emptyList() }

    private suspend fun handleContactSuggestionSelected(action: ComposerAction.ContactSuggestionSelected) {

        val recipientEmail = when (action.contact) {
            is ContactSuggestionUiModel.Contact -> action.contact.email
            is ContactSuggestionUiModel.ContactGroup -> {
                action.contact.emails
                    .joinToString(separator = "\n")
            }
        }

        val recipient = when {
            validateEmailAddress(recipientEmail) -> RecipientUiModel.Valid(recipientEmail)
            else -> RecipientUiModel.Invalid(recipientEmail)
        }

        val updateEvent = when (action.suggestionsField) {
            ContactSuggestionsField.TO -> {
                val updatedToRecipients = state.value.fields.to + listOf(recipient)
                onToChanged(updatedToRecipients)
            }
            ContactSuggestionsField.CC -> {
                val updatedCcRecipients = state.value.fields.cc + listOf(recipient)
                onCcChanged(updatedCcRecipients)
            }
            ContactSuggestionsField.BCC -> {
                val updatedBccRecipients = state.value.fields.bcc + listOf(recipient)
                onBccChanged(updatedBccRecipients)
            }
        }
        emitNewStateFor(updateEvent)
        emitNewStateFor(action)
    }

    private suspend fun onToChanged(toRecipients: List<RecipientUiModel>): ComposerOperation {
        val contacts = contactsOrEmpty()
        return toRecipients.filterIsInstance<RecipientUiModel.Valid>().let { validRecipients ->
            updateToRecipients(
                currentValidRecipientsTo().value,
                validRecipients.map { participantMapper.recipientUiModelToParticipant(it, contacts) }
            ).fold(
                ifLeft = { ComposerEvent.ErrorStoringDraftRecipients },
                ifRight = { ComposerEvent.UpdateToRecipients(toRecipients) }
            )
        }
    }

    private suspend fun onCcChanged(ccRecipients: List<RecipientUiModel>): ComposerOperation {
        val contacts = contactsOrEmpty()
        return ccRecipients.filterIsInstance<RecipientUiModel.Valid>().let { validRecipients ->
            updateCcRecipients(
                currentValidRecipientsCc().value,
                validRecipients.map { participantMapper.recipientUiModelToParticipant(it, contacts) }
            ).fold(
                ifLeft = { ComposerEvent.ErrorStoringDraftRecipients },
                ifRight = { ComposerEvent.UpdateCcRecipients(ccRecipients) }
            )
        }
    }

    private suspend fun onBccChanged(bccRecipients: List<RecipientUiModel>): ComposerOperation {
        val contacts = contactsOrEmpty()
        return bccRecipients.filterIsInstance<RecipientUiModel.Valid>().let { validRecipients ->
            updateBccRecipients(
                currentValidRecipientsBcc().value,
                validRecipients.map { participantMapper.recipientUiModelToParticipant(it, contacts) }
            ).fold(
                ifLeft = { ComposerEvent.ErrorStoringDraftRecipients },
                ifRight = { ComposerEvent.UpdateBccRecipients(bccRecipients) }
            )
        }
    }

    private fun onSearchTermChanged(searchTerm: String, suggestionsField: ContactSuggestionsField) {

        // cancel previous search Job for this [suggestionsField] type
        getContactSuggestionsJobs[suggestionsField]?.cancel()

        if (searchTerm.isNotBlank()) {
            getContactSuggestionsJobs[suggestionsField] = viewModelScope.launch {
                getContactSuggestions(primaryUserId(), ContactSuggestionQuery(searchTerm)).map { contacts ->
                    val contactsLimited = contacts.take(maxContactAutocompletionCount)
                    val suggestions = contactSuggestionsMapper.toUiModel(contactsLimited)

                    emitNewStateFor(
                        ComposerEvent.UpdateContactSuggestions(
                            searchTerm = searchTerm,
                            contactSuggestions = suggestions,
                            suggestionsField = suggestionsField
                        )
                    )
                }
            }
        } else {
            emitNewStateFor(
                ComposerAction.ContactSuggestionsDismissed(suggestionsField)
            )
        }

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

    companion object {

        internal const val maxContactAutocompletionCount = 100
    }
}
