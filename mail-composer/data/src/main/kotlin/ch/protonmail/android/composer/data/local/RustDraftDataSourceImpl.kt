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

package ch.protonmail.android.composer.data.local

import java.time.Duration
import androidx.annotation.VisibleForTesting
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import ch.protonmail.android.composer.data.mapper.toDraftCreateMode
import ch.protonmail.android.composer.data.mapper.toLocalDraft
import ch.protonmail.android.composer.data.mapper.toLocalDraftWithSyncStatus
import ch.protonmail.android.composer.data.mapper.toPreviousScheduleSendTime
import ch.protonmail.android.composer.data.mapper.toSaveDraftError
import ch.protonmail.android.composer.data.mapper.toSingleRecipientEntry
import ch.protonmail.android.composer.data.mapper.toSingleRecipients
import ch.protonmail.android.composer.data.usecase.CreateRustDraft
import ch.protonmail.android.composer.data.usecase.DiscardRustDraft
import ch.protonmail.android.composer.data.usecase.OpenRustDraft
import ch.protonmail.android.composer.data.usecase.RustDraftUndoSend
import ch.protonmail.android.composer.data.worker.SendingStatusWorker
import ch.protonmail.android.composer.data.wrapper.AttachmentsWrapper
import ch.protonmail.android.composer.data.wrapper.ComposerRecipientListWrapper
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.mailcommon.data.mapper.LocalEmbeddedImageInfo
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.data.worker.Enqueuer
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.PreviousScheduleSendTime
import ch.protonmail.android.mailcomposer.domain.model.SaveDraftError
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMessageId
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.ComposerRecipientValidationCallback
import uniffi.proton_mail_uniffi.DraftMessageIdResult
import uniffi.proton_mail_uniffi.DraftScheduleSendOptions
import uniffi.proton_mail_uniffi.DraftScheduleSendOptionsResult
import uniffi.proton_mail_uniffi.EmbeddedAttachmentInfoResult
import uniffi.proton_mail_uniffi.VoidDraftSaveResult
import uniffi.proton_mail_uniffi.VoidDraftSendResult
import javax.inject.Inject

class RustDraftDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustDraft: CreateRustDraft,
    private val openRustDraft: OpenRustDraft,
    private val discardRustDraft: DiscardRustDraft,
    private val rustDraftUndoSend: RustDraftUndoSend,
    private val cancelScheduleSendMessage: RustCancelScheduleSendMessage,
    private val enqueuer: Enqueuer
) : RustDraftDataSource {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val draftWrapperMutableStateFlow = MutableStateFlow<DraftWrapper?>(null)

    private val draftWrapperStateFlow: StateFlow<DraftWrapper?> = draftWrapperMutableStateFlow.asStateFlow()

    private val recipientsUpdatedCallback = object : ComposerRecipientValidationCallback {
        override fun onUpdate() {
            Timber.d("rust-draft: recipients validation state updated...")
        }
    }

    override suspend fun getMessageId(): Either<DataError, MessageId> = withValidRustDraftWrapper {
        return@withValidRustDraftWrapper when (val result = it.messageId()) {
            is DraftMessageIdResult.Error -> result.v1.toDataError().left()
            is DraftMessageIdResult.Ok -> result.v1?.toMessageId()?.right() ?: DataError.Local.NoDraftId.left()
        }
    }

    override suspend fun open(userId: UserId, messageId: MessageId): Either<DataError, LocalDraftWithSyncStatus> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to open draft with null session; Failing.")
            return DataError.Local.Unknown.left()
        }

        Timber.d("rust-draft: Opening draft...")
        return openRustDraft(session, messageId.toLocalMessageId())
            .onRight {
                Timber.d("rust-draft: Draft opened successfully.")
                draftWrapperMutableStateFlow.value = it.draftWrapper
            }
            .map { it.toLocalDraftWithSyncStatus() }
    }

    override suspend fun create(userId: UserId, action: DraftAction): Either<DataError, LocalDraft> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to create draft with null session; Failing.")
            return DataError.Local.Unknown.left()
        }

        val draftCreateMode = action.toDraftCreateMode()
        if (draftCreateMode == null) {
            Timber.e("rust-draft: Trying to create draft with invalid create mode; Failing.")
            return DataError.Local.UnsupportedOperation.left()
        }

        return createRustDraft(session, draftCreateMode)
            .onRight { draftWrapperMutableStateFlow.value = it }
            .map { it.toLocalDraft() }
    }

    override suspend fun discard(userId: UserId, messageId: MessageId): Either<DataError, Unit> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to discard draft with null session; Failing.")
            return DataError.Local.Unknown.left()
        }

        return discardRustDraft(session, messageId.toLocalMessageId())
    }

    override suspend fun saveSubject(subject: Subject): Either<SaveDraftError, Unit> = withValidRustDraftWrapper {
        return@withValidRustDraftWrapper when (val result = it.setSubject(subject.value)) {
            is VoidDraftSaveResult.Error -> result.v1.toSaveDraftError().left()
            is VoidDraftSaveResult.Ok -> Unit.right()
        }
    }

    override suspend fun saveBody(body: DraftBody): Either<SaveDraftError, Unit> = withValidRustDraftWrapper {
        return@withValidRustDraftWrapper when (val result = it.setBody(body.value)) {
            is VoidDraftSaveResult.Error -> result.v1.toSaveDraftError().left()
            is VoidDraftSaveResult.Ok -> Unit.right()
        }
    }

    override suspend fun updateToRecipients(recipients: List<Recipient>): Either<SaveDraftError, Unit> =
        withValidRustDraftWrapper { draftWrapper ->
            val recipientsToWrapper = draftWrapper.recipientsTo()
            return@withValidRustDraftWrapper updateRecipients(recipientsToWrapper, recipients)
        }

    override suspend fun updateCcRecipients(recipients: List<Recipient>): Either<SaveDraftError, Unit> =
        withValidRustDraftWrapper { draftWrapper ->
            val recipientsCcWrapper = draftWrapper.recipientsCc()
            return@withValidRustDraftWrapper updateRecipients(recipientsCcWrapper, recipients)
        }

    override suspend fun updateBccRecipients(recipients: List<Recipient>): Either<SaveDraftError, Unit> =
        withValidRustDraftWrapper { draftWrapper ->
            val recipientsBccWrapper = draftWrapper.recipientsBcc()
            return@withValidRustDraftWrapper updateRecipients(recipientsBccWrapper, recipients)
        }

    // Will emit based on a mutableFlow which is updated by the callback above;
    // Requests again the data from rust library, maps it to the new entity and exposes to the view
    // RecipientEntity will probably be used also in LocalDraft to follow (to convey groups + Validation info to UI)
    override suspend fun observeRecipientsValidation(): Flow<List<RecipientEntityWithValidation>> = flowOf(emptyList())


    override suspend fun send(): Either<DataError, Unit> = withValidRustDraftWrapper {
        Timber.d("rust-draft: Sending draft...")
        return@withValidRustDraftWrapper when (val result = it.send()) {
            is VoidDraftSendResult.Error -> result.v1.toDataError().left()
            is VoidDraftSendResult.Ok -> {
                startSendingStatusWorker()
                Unit.right()
            }
        }
    }

    override suspend fun scheduleSend(timestamp: Long): Either<DataError, Unit> = withValidRustDraftWrapper {
        when (val result = it.scheduleSend(timestamp.toULong())) {
            is VoidDraftSendResult.Error -> result.v1.toDataError().left()
            VoidDraftSendResult.Ok -> Unit.right()
        }
    }

    override suspend fun undoSend(userId: UserId, messageId: MessageId): Either<DataError, Unit> {
        Timber.d("rust-draft: Undo sending draft...")
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to undo send with null session; Failing.")
            return DataError.Local.NoUserSession.left()
        }

        return rustDraftUndoSend(session, messageId.toLocalMessageId()).onRight {
            enqueuer.cancelWork(SendingStatusWorker.id(userId, messageId))
        }
    }

    override suspend fun cancelScheduleSendMessage(
        userId: UserId,
        messageId: MessageId
    ): Either<DataError, PreviousScheduleSendTime> {
        Timber.d("rust-draft: Cancels schedule send raft...")
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to cancel schedule send with null session; Failing.")
            return DataError.Local.NoUserSession.left()
        }

        return cancelScheduleSendMessage(session, messageId.toLocalMessageId()).map {
            it.toPreviousScheduleSendTime()
        }
    }

    override suspend fun attachmentList(): Either<DataError, AttachmentsWrapper> {
        val wrapper = draftWrapperStateFlow.filterNotNull().first()
        return wrapper.attachmentList().right()
    }

    override fun getEmbeddedImage(contentId: String): Either<DataError, LocalEmbeddedImageInfo> {
        val rustDraftWrapper: DraftWrapper = draftWrapperStateFlow.value
            ?: return DataError.Local.NoDataCached.left()

        return when (val result = rustDraftWrapper.embeddedImage(contentId)) {
            is EmbeddedAttachmentInfoResult.Error -> result.v1.toDataError().left()
            is EmbeddedAttachmentInfoResult.Ok -> result.v1.right()
        }
    }

    override fun getScheduleSendOptions(): Either<DataError, DraftScheduleSendOptions> {
        val rustDraftWrapper: DraftWrapper = draftWrapperStateFlow.value
            ?: return DataError.Local.NoDataCached.left()

        return when (val result = rustDraftWrapper.scheduleSendOptions()) {
            is DraftScheduleSendOptionsResult.Error -> result.v1.toDataError().left()
            is DraftScheduleSendOptionsResult.Ok -> result.v1.right()
        }
    }

    private fun updateRecipients(
        recipientsWrapper: ComposerRecipientListWrapper,
        updatedRecipients: List<Recipient>
    ): Either<SaveDraftError, Unit> = either {
        val currentRecipients = recipientsWrapper.recipients().toSingleRecipients()
        val recipientsToAdd = updatedRecipients.filterNot { updatedRecipient ->
            updatedRecipient.address in currentRecipients.map { it.address }
        }
        val recipientsToRemove = currentRecipients.filterNot { currentRecipient ->
            currentRecipient.address in updatedRecipients.map { it.address }
        }

        recipientsToAdd.forEach {
            recipientsWrapper.addSingleRecipient(it.toSingleRecipientEntry())
                .onLeft { error -> raise(error) }
        }

        recipientsToRemove.forEach {
            recipientsWrapper.removeSingleRecipient(it.toSingleRecipientEntry())
                .onLeft { error -> raise(error) }
        }
    }

    private suspend fun startSendingStatusWorker() {
        val userId = userSessionRepository.observePrimaryUserId().firstOrNull()
        if (userId == null) {
            Timber.e("rust-draft: Trying to start sending status worker with null userId; Failing.")
            return
        }

        val messageId = this.getMessageId()
            .onLeft { Timber.e("rust-draft: Failed to get messageId due to error: $it") }
            .getOrNull()


        if (messageId == null) {
            Timber.e("rust-draft: Trying to start sending status worker with null messageId; Failing.")
            return
        }

        Timber.d("rust-draft: Starting sending status worker...")
        enqueuer.enqueueUniqueWork<SendingStatusWorker>(
            userId = userId,
            workerId = SendingStatusWorker.id(userId, messageId),
            params = SendingStatusWorker.params(userId, messageId),
            backoffCriteria = Enqueuer.BackoffCriteria.DefaultLinear,
            initialDelay = InitialDelayForSendingStatusWorker
        )
    }

    private suspend fun <E, T> withValidRustDraftWrapper(
        closure: suspend (DraftWrapper) -> Either<E, T>
    ): Either<E, T> {
        val rustDraftWrapper: DraftWrapper = draftWrapperStateFlow.value
            ?: throw IllegalStateException("Attempting to access draft operations while no draft object exists")

        return closure(rustDraftWrapper)
    }

    companion object {

        val InitialDelayForSendingStatusWorker: Duration = Duration.ofMillis(10_000L)
    }
}
