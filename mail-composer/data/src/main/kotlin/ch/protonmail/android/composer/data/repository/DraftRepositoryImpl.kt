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
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class DraftRepositoryImpl @Inject constructor(
    private val draftDataSource: RustDraftDataSource
) : DraftRepository {

    override suspend fun openDraft(userId: UserId, messageId: MessageId): Either<DataError, DraftFields> =
        draftDataSource.open(userId, messageId).map { it.toDraftFields() }

    override suspend fun createDraft(userId: UserId, action: DraftAction): Either<DataError, DraftFields> =
        draftDataSource.create(userId, action).map { it.toDraftFields() }

    override suspend fun save(): Either<DataError, Unit> = draftDataSource.save()

    override suspend fun saveSubject(subject: Subject): Either<DataError, Unit> = draftDataSource.saveSubject(subject)

    override suspend fun saveBody(body: DraftBody): Either<DataError, Unit> = draftDataSource.saveBody(body)

    override suspend fun addToRecipient(recipient: Recipient): Either<DataError, Unit> =
        draftDataSource.addToRecipient(recipient)

    override suspend fun addCcRecipient(recipient: Recipient): Either<DataError, Unit> =
        draftDataSource.addCcRecipient(recipient)

    override suspend fun addBccRecipient(recipient: Recipient): Either<DataError, Unit> =
        draftDataSource.addBccRecipient(recipient)

    override suspend fun removeToRecipient(recipient: Recipient): Either<DataError, Unit> =
        draftDataSource.removeToRecipient(recipient)

    override suspend fun removeCcRecipient(recipient: Recipient): Either<DataError, Unit> =
        draftDataSource.removeCcRecipient(recipient)

    override suspend fun removeBccRecipient(recipient: Recipient): Either<DataError, Unit> =
        draftDataSource.removeBccRecipient(recipient)

    override suspend fun updateToRecipient(recipients: List<Recipient>): Either<DataError, Unit> =
        draftDataSource.updateToRecipients(recipients)

    override suspend fun updateCcRecipient(recipients: List<Recipient>): Either<DataError, Unit> =
        draftDataSource.updateCcRecipients(recipients)

    override suspend fun updateBccRecipient(recipients: List<Recipient>): Either<DataError, Unit> =
        draftDataSource.updateBccRecipients(recipients)

    override suspend fun send(): Either<DataError, Unit> = draftDataSource.send()

    override suspend fun undoSend(userId: UserId, messageId: MessageId): Either<DataError, Unit> =
        draftDataSource.undoSend(userId, messageId)


}
