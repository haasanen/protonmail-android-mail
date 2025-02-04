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

package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class StoreDraftWithRecipients @Inject constructor(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        to: List<Recipient>? = null,
        cc: List<Recipient>? = null,
        bcc: List<Recipient>? = null
    ): Either<DataError, Unit> = either {
        to?.forEach { toRecipient ->
            draftRepository.saveToRecipient(userId, messageId, toRecipient)
                .onLeft { raise(it) }
        }
        cc?.forEach { ccRecipient ->
            draftRepository.saveCcRecipient(userId, messageId, ccRecipient)
                .onLeft { raise(it) }
        }
        bcc?.forEach { bccRecipient ->
            draftRepository.saveBccRecipient(userId, messageId, bccRecipient)
                .onLeft { raise(it) }
        }
    }

}
