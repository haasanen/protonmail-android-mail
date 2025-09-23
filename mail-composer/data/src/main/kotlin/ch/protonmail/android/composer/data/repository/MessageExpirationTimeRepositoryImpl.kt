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
import ch.protonmail.android.composer.data.mapper.toMessageExpiration
import ch.protonmail.android.composer.data.mapper.toRecipientsNotSupportingExpiration
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.RecipientsNotSupportingExpiration
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationError
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.repository.MessageExpirationTimeRepository
import javax.inject.Inject

class MessageExpirationTimeRepositoryImpl @Inject constructor(
    private val draftDataSource: RustDraftDataSource
) : MessageExpirationTimeRepository {

    override suspend fun getMessageExpirationTime(): Either<DataError, MessageExpirationTime> =
        draftDataSource.getMessageExpiration().map { it.toMessageExpiration() }

    override suspend fun saveMessageExpirationTime(time: MessageExpirationTime): Either<MessageExpirationError, Unit> =
        draftDataSource.setMessageExpiration(time)

    override suspend fun validateSendWithExpirationTime(): Either<DataError, RecipientsNotSupportingExpiration> =
        draftDataSource.validateSendWithExpiration().map { it.toRecipientsNotSupportingExpiration() }

}
