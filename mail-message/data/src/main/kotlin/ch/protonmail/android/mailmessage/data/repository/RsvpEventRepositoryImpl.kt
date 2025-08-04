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

package ch.protonmail.android.mailmessage.data.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.local.RustRsvpEventDataSource
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toLocalRsvpAnswer
import ch.protonmail.android.mailmessage.data.mapper.toRsvpEvent
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import ch.protonmail.android.mailmessage.domain.repository.RsvpEventRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class RsvpEventRepositoryImpl @Inject constructor(
    private val rsvpEventDataSource: RustRsvpEventDataSource
) : RsvpEventRepository {

    override suspend fun identifyRsvp(userId: UserId, messageId: MessageId): Either<DataError, Boolean> =
        rsvpEventDataSource.identifyRsvp(userId, messageId.toLocalMessageId())

    override suspend fun getRsvpEvent(userId: UserId, messageId: MessageId): Either<DataError, RsvpEvent> =
        rsvpEventDataSource.getRsvpEvent(userId, messageId.toLocalMessageId()).map { it.toRsvpEvent() }

    override suspend fun answerRsvpEvent(
        userId: UserId,
        messageId: MessageId,
        answer: RsvpAnswer
    ): Either<DataError, Unit> =
        rsvpEventDataSource.answerRsvpEvent(userId, messageId.toLocalMessageId(), answer.toLocalRsvpAnswer())
}
