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

    override suspend fun save(userId: UserId, messageId: MessageId): Either<DataError, Unit> = draftDataSource.save()

    override suspend fun saveSubject(
        userId: UserId,
        messageId: MessageId,
        subject: Subject
    ): Either<DataError, Unit> = draftDataSource.saveSubject(subject)

    override suspend fun saveBody(
        userId: UserId,
        messageId: MessageId,
        body: DraftBody
    ): Either<DataError, Unit> = draftDataSource.saveBody(body)

    override suspend fun saveToRecipient(
        userId: UserId,
        messageId: MessageId,
        recipient: Recipient
    ): Either<DataError, Unit> = draftDataSource.saveToRecipient(recipient)

    override suspend fun saveCcRecipient(
        userId: UserId,
        messageId: MessageId,
        recipient: Recipient
    ): Either<DataError, Unit> = draftDataSource.saveCcRecipient(recipient)

    override suspend fun saveBccRecipient(
        userId: UserId,
        messageId: MessageId,
        recipient: Recipient
    ): Either<DataError, Unit> = draftDataSource.saveBccRecipient(recipient)
}
