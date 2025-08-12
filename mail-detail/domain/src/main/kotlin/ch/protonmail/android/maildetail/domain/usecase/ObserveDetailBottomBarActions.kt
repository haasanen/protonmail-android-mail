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

package ch.protonmail.android.maildetail.domain.usecase

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.usecase.ObserveAllConversationBottomBarActions
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsSnoozeEnabled
import ch.protonmail.android.maillabel.domain.model.LabelId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ObserveDetailBottomBarActions @Inject constructor(
    private val observeAllConversationBottomBarActions: ObserveAllConversationBottomBarActions,
    @IsSnoozeEnabled private val isSnoozeEnabled: Flow<Boolean>
) {

    operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        conversationId: ConversationId
    ): Flow<Either<DataError, List<Action>>> = observeAllConversationBottomBarActions(userId, labelId, conversationId)
        .map { eitherResult ->
            eitherResult.map { allBottomBarActions ->
                allBottomBarActions.visibleActions.removeSnoozeIfNecessary(isSnoozeEnabled.first())
            }
        }

    private fun List<Action>.removeSnoozeIfNecessary(snoozeEnabled: Boolean) = if (!snoozeEnabled) {
        this.removeSnooze()
    } else {
        this
    }

    private fun List<Action>.removeSnooze(): List<Action> {
        val mutableActions = this.toMutableList()
        mutableActions.remove(Action.Snooze)
        return mutableActions
    }
}
