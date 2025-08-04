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
package ch.protonmail.android.mailsnooze.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.data.mapper.LocalNonDefaultWeekStart
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.data.usecase.ExecuteWithUserSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import uniffi.proton_mail_uniffi.AvailableSnoozeActionsForConversationResult
import uniffi.proton_mail_uniffi.SnoozeActions
import uniffi.proton_mail_uniffi.availableSnoozeActionsForConversation
import javax.inject.Inject

class RustSnoozeDataSource @Inject constructor(
    private val executeWithUserSession: ExecuteWithUserSession,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getAvailableSnoozeActionsForConversation(
        userId: UserId,
        weekStart: LocalNonDefaultWeekStart,
        conversationIds: List<LocalConversationId>
    ): Either<DataError, SnoozeActions> = withContext(ioDispatcher) {
        executeWithUserSession(userId) { sessionWrapper ->
            when (
                val result = availableSnoozeActionsForConversation(
                    sessionWrapper.getRustUserSession(),
                    weekStart, conversationIds
                )
            ) {
                is AvailableSnoozeActionsForConversationResult.Error -> result.v1.toDataError().left()
                is AvailableSnoozeActionsForConversationResult.Ok -> result.v1.right()
            }
        }.map { right ->
            return@withContext right
        }
    }
}
