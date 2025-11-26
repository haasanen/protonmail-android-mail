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
import ch.protonmail.android.mailconversation.domain.usecase.GetAllConversationBottomBarActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageDetailBottomBarActions
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetDetailBottomBarActions @Inject constructor(
    private val getAllConversationBottomBarActions: GetAllConversationBottomBarActions,
    private val getMessageDetailBottomBarActions: GetMessageDetailBottomBarActions
) {

    suspend operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        conversationId: ConversationId
    ): Either<DataError, List<Action>> = getAllConversationBottomBarActions(
        userId,
        labelId,
        conversationId
    ).map { actions -> actions.visibleActions }


    suspend operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        messageId: MessageId,
        themeOptions: MessageThemeOptions
    ): Either<DataError, List<Action>> = getMessageDetailBottomBarActions(userId, labelId, messageId, themeOptions)
        .map { actions -> actions.visibleActions }
}
