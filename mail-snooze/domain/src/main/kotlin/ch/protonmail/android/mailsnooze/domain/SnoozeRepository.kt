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

package ch.protonmail.android.mailsnooze.domain

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailsnooze.domain.model.SnoozeError
import ch.protonmail.android.mailsnooze.domain.model.SnoozeOption
import ch.protonmail.android.mailsnooze.domain.model.SnoozeTime
import ch.protonmail.android.mailsnooze.domain.model.SnoozeWeekStart
import ch.protonmail.android.mailsnooze.domain.model.UnsnoozeError
import me.proton.core.domain.entity.UserId

interface SnoozeRepository {

    suspend fun getAvailableSnoozeActions(
        userId: UserId,
        weekStart: SnoozeWeekStart = SnoozeWeekStart.MONDAY,
        conversationIds: List<ConversationId>
    ): Either<SnoozeError, List<SnoozeOption>>

    suspend fun snoozeConversation(
        userId: UserId,
        labelId: LabelId,
        conversationIds: List<ConversationId>,
        snoozeTime: SnoozeTime
    ): Either<SnoozeError, Unit>

    suspend fun unSnoozeConversation(
        userId: UserId,
        labelId: LabelId,
        conversationIds: List<ConversationId>
    ): Either<UnsnoozeError, Unit>
}
