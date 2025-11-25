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

package ch.protonmail.android.mailmailbox.data.repository

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmailbox.data.local.SenderAddressDataSource
import ch.protonmail.android.mailmailbox.domain.repository.SenderAddressRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class SenderAddressRepositoryImpl @Inject constructor(
    val senderAddressDataSource: SenderAddressDataSource,
    val userSessionRepository: UserSessionRepository
) : SenderAddressRepository {

    override suspend fun observeUserHasValidSenderAddress(userId: UserId): Flow<Either<DataError, Boolean>> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("Trying to observe userSenderAddresses with null session; Failing.")
            return flowOf(DataError.Local.NoUserSession.left())
        }
        return senderAddressDataSource.observeUserHasValidSenderAddress(session)
    }
}
