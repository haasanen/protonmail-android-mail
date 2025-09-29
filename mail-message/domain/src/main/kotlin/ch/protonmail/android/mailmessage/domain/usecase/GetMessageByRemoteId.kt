/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.RemoteMessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class GetMessageByRemoteId @Inject constructor(
    private val messageRepository: MessageRepository
) {

    @OptIn(ExperimentalAtomicApi::class)
    suspend operator fun invoke(userId: UserId, messageId: RemoteMessageId): Either<DataError, Message> {
        val attempts = AtomicInt(0)

        while (attempts.addAndFetch(1) < MAX_RETRY_ATTEMPTS) {
            val message = getByRemoteId(userId, messageId)
            if (message.isRight()) {
                return message
            }
        }

        return getByRemoteId(userId, messageId)
    }

    private suspend fun getByRemoteId(userId: UserId, messageId: RemoteMessageId): Either<DataError, Message> =
        messageRepository.observeMessage(userId, messageId).first()
}

private const val MAX_RETRY_ATTEMPTS = 3
