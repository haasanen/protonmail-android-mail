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

package ch.protonmail.android.mailcomposer.domain.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.ChangeSenderError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.DraftFieldsWithSyncStatus
import ch.protonmail.android.mailcomposer.domain.model.OpenDraftError
import ch.protonmail.android.mailcomposer.domain.model.SaveDraftError
import ch.protonmail.android.mailcomposer.domain.model.ScheduleSendOptions
import ch.protonmail.android.mailcomposer.domain.model.SendDraftError
import ch.protonmail.android.mailcomposer.domain.model.SenderAddresses
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import me.proton.core.domain.entity.UserId
import kotlin.time.Instant

interface DraftRepository {

    suspend fun getMessageId(): Either<DataError, MessageId>
    fun loadImage(url: String): Either<DataError, MessageBodyImage>
    suspend fun openDraft(userId: UserId, messageId: MessageId): Either<OpenDraftError, DraftFieldsWithSyncStatus>
    suspend fun createDraft(userId: UserId, action: DraftAction): Either<OpenDraftError, DraftFields>
    suspend fun discardDraft(userId: UserId, messageId: MessageId): Either<DataError, Unit>
    suspend fun send(): Either<SendDraftError, Unit>
    suspend fun scheduleSend(time: Instant): Either<SendDraftError, Unit>
    suspend fun undoSend(userId: UserId, messageId: MessageId): Either<DataError, Unit>
    suspend fun saveSubject(subject: Subject): Either<SaveDraftError, Unit>
    suspend fun saveBody(body: DraftBody): Either<SaveDraftError, Unit>
    suspend fun updateToRecipient(recipients: List<Recipient>): Either<SaveDraftError, Unit>
    suspend fun updateCcRecipient(recipients: List<Recipient>): Either<SaveDraftError, Unit>
    suspend fun updateBccRecipient(recipients: List<Recipient>): Either<SaveDraftError, Unit>
    suspend fun getScheduleSendOptions(): Either<DataError, ScheduleSendOptions>
    suspend fun listSenderAddresses(): Either<DataError, SenderAddresses>
    suspend fun changeSender(sender: SenderEmail): Either<ChangeSenderError, Unit>
    suspend fun getBody(): Either<DataError, DraftBody>
}
