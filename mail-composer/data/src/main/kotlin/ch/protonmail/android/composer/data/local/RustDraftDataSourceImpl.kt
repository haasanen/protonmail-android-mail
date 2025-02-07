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

import androidx.annotation.VisibleForTesting
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.mapper.toDraftCreateMode
import ch.protonmail.android.composer.data.mapper.toLocalDraft
import ch.protonmail.android.composer.data.mapper.toSingleRecipientEntry
import ch.protonmail.android.composer.data.usecase.CreateRustDraft
import ch.protonmail.android.composer.data.usecase.OpenRustDraft
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.VoidDraftSaveSendResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressWarnings("NotImplementedDeclaration")
class RustDraftDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustDraft: CreateRustDraft,
    private val openRustDraft: OpenRustDraft
) : RustDraftDataSource {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rustDraftWrapper: DraftWrapper? = null

    override suspend fun open(userId: UserId, messageId: MessageId): Either<DataError, LocalDraft> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-draft: Trying to open draft with null session; Failing.")
            return DataError.Local.Unknown.left()
        }

        return openRustDraft(session, messageId.toLocalMessageId())
            .onRight { rustDraftWrapper = it }
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
            .onRight { rustDraftWrapper = it }
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

    override suspend fun saveToRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsTo()
        return@withValidRustDraftWrapper recipientsWrapper.addSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun saveCcRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
        val recipientsWrapper = it.recipientsCc()
        return@withValidRustDraftWrapper recipientsWrapper.addSingleRecipient(recipient.toSingleRecipientEntry())
    }

    override suspend fun saveBccRecipient(recipient: Recipient): Either<DataError, Unit> = withValidRustDraftWrapper {
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


    private suspend fun withValidRustDraftWrapper(
        closure: suspend (DraftWrapper) -> Either<DataError, Unit>
    ): Either<DataError, Unit> {
        val rustDraftWrapper: DraftWrapper = rustDraftWrapper
            ?: return DataError.Local.SaveDraftError.NoRustDraftAvailable.left()

        return closure(rustDraftWrapper)
    }
}
