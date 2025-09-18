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

package ch.protonmail.android.mailsession.domain.usecase

import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ObservePrimaryUserIdWithValidSession @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val userSessionRepository: UserSessionRepository
) {

    operator fun invoke(): Flow<UserId?> = observePrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            userSessionRepository.observeUserSessionAvailable(userId)
        }
}
