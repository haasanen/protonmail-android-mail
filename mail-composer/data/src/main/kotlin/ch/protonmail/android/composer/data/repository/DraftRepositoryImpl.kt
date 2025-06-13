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

package ch.protonmail.android.composer.data.repository

import arrow.core.Either
import ch.protonmail.android.composer.data.local.RustDraftDataSource
import ch.protonmail.android.composer.data.mapper.toDraftFields
import ch.protonmail.android.composer.data.mapper.toDraftFieldsWithSyncStatus
import ch.protonmail.android.composer.data.mapper.toEmbeddedImage
import ch.protonmail.android.composer.data.mapper.toScheduleSendOptions
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.DraftFieldsWithSyncStatus
import ch.protonmail.android.mailcomposer.domain.model.SaveDraftError
import ch.protonmail.android.mailcomposer.domain.model.ScheduleSendOptions
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import kotlin.time.Instant

class DraftRepositoryImpl @Inject constructor(
    private val draftDataSource: RustDraftDataSource,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : DraftRepository {

    override suspend fun getMessageId(): Either<DataError, MessageId> = draftDataSource.getMessageId()

    override fun getEmbeddedImage(contentId: String): Either<DataError, EmbeddedImage> =
        draftDataSource.getEmbeddedImage(contentId).map { it.toEmbeddedImage() }

    override suspend fun openDraft(userId: UserId, messageId: MessageId): Either<DataError, DraftFieldsWithSyncStatus> =
        draftDataSource.open(userId, messageId).map { it.toDraftFieldsWithSyncStatus() }

    override suspend fun createDraft(userId: UserId, action: DraftAction): Either<DataError, DraftFields> =
        draftDataSource.create(userId, action).map { it.toDraftFields() }

    override suspend fun discardDraft(userId: UserId, messageId: MessageId) = draftDataSource.discard(userId, messageId)

    override suspend fun saveSubject(subject: Subject): Either<SaveDraftError, Unit> =
        draftDataSource.saveSubject(subject)

    override suspend fun saveBody(body: DraftBody): Either<SaveDraftError, Unit> = draftDataSource.saveBody(body)

    override suspend fun updateToRecipient(recipients: List<Recipient>): Either<SaveDraftError, Unit> =
        draftDataSource.updateToRecipients(recipients)

    override suspend fun updateCcRecipient(recipients: List<Recipient>): Either<SaveDraftError, Unit> =
        draftDataSource.updateCcRecipients(recipients)

    override suspend fun updateBccRecipient(recipients: List<Recipient>): Either<SaveDraftError, Unit> =
        draftDataSource.updateBccRecipients(recipients)

    override suspend fun getScheduleSendOptions(): Either<DataError, ScheduleSendOptions> = withContext(ioDispatcher) {
        draftDataSource.getScheduleSendOptions().map { it.toScheduleSendOptions() }
    }

    override suspend fun send(): Either<DataError, Unit> = draftDataSource.send()

    override suspend fun scheduleSend(time: Instant): Either<DataError, Unit> =
        draftDataSource.scheduleSend(time.epochSeconds)

    override suspend fun undoSend(userId: UserId, messageId: MessageId): Either<DataError, Unit> =
        draftDataSource.undoSend(userId, messageId)


}
