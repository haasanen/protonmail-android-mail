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

package ch.protonmail.android.mailupselling.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class HasInAppNotificationsEnabled @Inject constructor(
    private val sessionRepo: UserSessionRepository
) {

    suspend operator fun invoke(userId: UserId): Either<DataError, Boolean> = either {
        val settings = sessionRepo.getUserSettings(userId) ?: raise(DataError.Local.NoDataCached)
        val hasSettingEnabled = settings.news and (1 shl 14) != 0 // 1 << 14
        hasSettingEnabled
    }
}
