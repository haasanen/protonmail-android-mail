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
import arrow.core.right
import ch.protonmail.android.composer.data.mapper.toDraftCreateMode
import ch.protonmail.android.composer.data.mapper.toLocalDraft
import ch.protonmail.android.composer.data.mapper.toSingleRecipientEntry
import ch.protonmail.android.composer.data.usecase.CreateRustDraft
import ch.protonmail.android.composer.data.usecase.OpenRustDraft
import ch.protonmail.android.composer.data.usecase.RustDraftUndoSend
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.mailcommon.data.worker.Enqueuer
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.composer.data.worker.SendingStatusWorker
import ch.protonmail.android.composer.data.wrapper.AttachmentsWrapper
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
import uniffi.proton_mail_uniffi.VoidDraftSaveSendResult
import javax.inject.Inject

class RustDraftDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustDraft: CreateRustDraft,
    private val openRustDraft: OpenRustDraft,
    private val rustDraftUndoSend: RustDraftUndoSend,
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

    override suspend fun open(userId: UserId, messageId: MessageId): Either<DataError, LocalDraft> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to open draft with null session; Failing.")
            return DataError.Local.Unknown.left()
        }

        Timber.d("rust-draft: Opening draft...")
        return openRustDraft(session, messageId.toLocalMessageId())
            .onRight {
                Timber.d("rust-draft: Draft opened successfully.")
                draftWrapperMutableStateFlow.value = it
            }
            .map { it.toLocalDraft() }
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

    override suspend fun save(): Either<DataError, Unit> = withValidRustDraftWrapper {
        return@withValidRustDraftWrapper when (val result = it.save()) {
            is VoidDraftSaveSendResult.Error -> result.v1.toDataError().left()
            VoidDraftSaveSendResult.Ok -> Unit.right()
        }
    }

    override suspend fun saveSubject(subject: Subject): Either<DataError, Unit> = withValidRustDraftWrapper {
        return@withValidRustDraftWrapper when (val result = it.setSubject(subject.value)) {
            is VoidDraftSaveSendResult.Error -> result.v1.toDataError().left()
            VoidDraftSaveSendResult.Ok -> save()
        }
    }

    override suspend fun saveBody(body: DraftBody): Either<DataError, Unit> = withValidRustDraftWrapper {
        return@withValidRustDraftWrapper when (val result = it.setBody(body.value)) {
            is VoidDraftSaveSendResult.Error -> result.v1.toDataError().left()
            VoidDraftSaveSendResult.Ok -> save()
        }
    }

    override suspend fun addToRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsTo()
        recipientsWrapper.registerCallback(recipientsUpdatedCallback)
        return@withValidRustDraftWrapper recipientsWrapper.addSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun addCcRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsCc()
        return@withValidRustDraftWrapper recipientsWrapper.addSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun addBccRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsBcc()
        return@withValidRustDraftWrapper recipientsWrapper.addSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun removeToRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsTo()
        return@withValidRustDraftWrapper recipientsWrapper.removeSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun removeCcRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsCc()
        return@withValidRustDraftWrapper recipientsWrapper.removeSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun removeBccRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsBcc()
        return@withValidRustDraftWrapper recipientsWrapper.removeSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun observeRecipientsValidation(): Flow<List<RecipientEntityWithValidation>> =
        // Will emit based on a mutableFlow which is updated by the callback above;
        // Requests again the data from rust library, maps it to the new entity and exposes to the view
        // RecipientEntity will probably be used also in LocalDraft to follow (to convey groups + Validation info to UI)
        flowOf(emptyList())


    override suspend fun send(): Either<DataError, Unit> = withValidRustDraftWrapper {
        Timber.d("rust-draft: Sending draft...")
        return@withValidRustDraftWrapper when (val result = it.send()) {
            is VoidDraftSaveSendResult.Error -> result.v1.toDataError().left()
            VoidDraftSaveSendResult.Ok -> {
                startSendingStatusWorker()
                Unit.right()
            }
        }
    }

    override suspend fun undoSend(userId: UserId, messageId: MessageId): Either<DataError, Unit> {
        Timber.d("rust-draft: Undo sending draft...")
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to undo send with null session; Failing.")
            return DataError.Local.Unknown.left()
        }

        return rustDraftUndoSend(session, messageId.toLocalMessageId()).onRight {
            enqueuer.cancelWork(SendingStatusWorker.id(userId, messageId))
        }

    }

    override suspend fun attachmentList(): Either<DataError, AttachmentsWrapper> {
        val wrapper = draftWrapperStateFlow.filterNotNull().first()
        return wrapper.attachmentList().right()
    }

    private suspend fun startSendingStatusWorker() {
        val userId = userSessionRepository.observePrimaryUserId().firstOrNull()
        if (userId == null) {
            Timber.e("rust-draft: Trying to start sending status worker with null userId; Failing.")
            return
        }

        val messageId = when (val messageIdResult = draftWrapperMutableStateFlow.value?.messageId()) {
            is DraftMessageIdResult.Ok -> messageIdResult.v1?.toMessageId()
            is DraftMessageIdResult.Error -> {
                Timber.e("rust-draft: Failed to get messageId due to error: ${messageIdResult.v1}")
                null
            }
            null -> {
                Timber.e("rust-draft: messageId() returned null.")
                null
            }
        }

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

    private suspend fun withValidRustDraftWrapper(
        closure: suspend (DraftWrapper) -> Either<DataError, Unit>
    ): Either<DataError, Unit> {
        val rustDraftWrapper: DraftWrapper = draftWrapperStateFlow.value
            ?: run {
                Timber.w("Attempting to access draft operations while not draft object exists")
                return DataError.Local.SaveDraftError.NoRustDraftAvailable.left()
            }

        return closure(rustDraftWrapper)
    }

    companion object {

        val InitialDelayForSendingStatusWorker: Duration = Duration.ofMillis(10_000L)
    }
}
