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

package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.SendWithExpirationTimeResult
import ch.protonmail.android.mailcomposer.domain.repository.MessageExpirationTimeRepository
import javax.inject.Inject

class CanSendWithExpirationTime @Inject constructor(
    private val messageExpirationTimeRepository: MessageExpirationTimeRepository
) {

    suspend operator fun invoke(): Either<DataError, SendWithExpirationTimeResult> =
        messageExpirationTimeRepository.validateSendWithExpirationTime().map {
            when {
                it.unsupported.isNotEmpty() && it.supported.isEmpty() && it.unknown.isEmpty() ->
                    SendWithExpirationTimeResult.ExpirationUnsupportedForAll

                it.unsupported.isNotEmpty() ->
                    SendWithExpirationTimeResult.ExpirationUnsupportedForSome(it.unsupported)

                it.unknown.isNotEmpty() ->
                    SendWithExpirationTimeResult.ExpirationSupportUnknown(it.unknown)

                else -> SendWithExpirationTimeResult.CanSend
            }
        }
}
