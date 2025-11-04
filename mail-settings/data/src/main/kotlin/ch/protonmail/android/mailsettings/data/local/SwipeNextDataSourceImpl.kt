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

package ch.protonmail.android.mailsettings.data.local

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.data.usecase.CreateRustCustomSettings
import ch.protonmail.android.mailsettings.data.wrapper.CustomSettingsWrapper
import ch.protonmail.android.mailsettings.domain.model.SwipeNextPreference
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class SwipeNextDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustCustomSettings: CreateRustCustomSettings
) : SwipeNextDataSource {

    override suspend fun getSwipeNext(userId: UserId): Either<DataError, SwipeNextPreference> {
        val wrapper = getCustomSettingsWrapper(userId)
            ?: return DataError.Local.NoUserSession.left()

        return wrapper.getSwipeToAdjacentConversation()
            .map { SwipeNextPreference(it) }
    }

    override suspend fun setSwipeNextEnabled(userId: UserId, enabled: Boolean): Either<DataError, Unit> {
        val wrapper = getCustomSettingsWrapper(userId)
            ?: return DataError.Local.NoUserSession.left()

        return wrapper.setSwipeToAdjacentConversation(enabled)
    }

    private suspend fun getCustomSettingsWrapper(userId: UserId): CustomSettingsWrapper? {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("mobile-signature: user session does not exist!")
            return null
        }
        return createRustCustomSettings(session)
    }
}
