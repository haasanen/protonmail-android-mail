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

import arrow.core.Either
import ch.protonmail.android.composer.data.wrapper.AttachmentsWrapper
import ch.protonmail.android.mailcommon.data.mapper.LocalEmbeddedImageInfo
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.SaveDraftError
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

interface RustDraftDataSource {

    suspend fun getMessageId(): Either<DataError, MessageId>
    suspend fun open(userId: UserId, messageId: MessageId): Either<DataError, LocalDraftWithSyncStatus>
    suspend fun create(userId: UserId, action: DraftAction): Either<DataError, LocalDraft>
    suspend fun discard(userId: UserId, messageId: MessageId): Either<DataError, Unit>
    suspend fun saveSubject(subject: Subject): Either<SaveDraftError, Unit>
    suspend fun saveBody(body: DraftBody): Either<SaveDraftError, Unit>
    suspend fun observeRecipientsValidation(): Flow<List<RecipientEntityWithValidation>>
    suspend fun send(): Either<DataError, Unit>
    suspend fun undoSend(userId: UserId, messageId: MessageId): Either<DataError, Unit>
    suspend fun attachmentList(): Either<DataError, AttachmentsWrapper>
    suspend fun updateToRecipients(recipients: List<Recipient>): Either<SaveDraftError, Unit>
    suspend fun updateCcRecipients(recipients: List<Recipient>): Either<SaveDraftError, Unit>
    suspend fun updateBccRecipients(recipients: List<Recipient>): Either<SaveDraftError, Unit>
    suspend fun getEmbeddedImage(contentId: String): Either<DataError, LocalEmbeddedImageInfo>
}
