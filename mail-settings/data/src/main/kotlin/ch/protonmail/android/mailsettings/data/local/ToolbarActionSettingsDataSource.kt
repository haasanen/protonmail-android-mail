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
import ch.protonmail.android.mailcommon.data.mapper.LocalMobileAction
import ch.protonmail.android.mailcommon.domain.model.DataError
import me.proton.core.domain.entity.UserId

interface ToolbarActionSettingsDataSource {

    suspend fun getListActions(userId: UserId): Either<DataError, List<LocalMobileAction>>
    suspend fun getAllListActions(): List<LocalMobileAction>
    suspend fun updateListActions(userId: UserId, actions: List<LocalMobileAction>): Either<DataError, Unit>

    suspend fun getConversationActions(userId: UserId): Either<DataError, List<LocalMobileAction>>
    suspend fun getAllConversationActions(): List<LocalMobileAction>
    suspend fun updateConversationActions(userId: UserId, actions: List<LocalMobileAction>): Either<DataError, Unit>

    suspend fun getMessageActions(userId: UserId): Either<DataError, List<LocalMobileAction>>
    suspend fun getAllMessageActions(): List<LocalMobileAction>
    suspend fun updateMessageActions(userId: UserId, actions: List<LocalMobileAction>): Either<DataError, Unit>
}
