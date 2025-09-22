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

package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.entity.ConversationDetailEntryPoint
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationMessages
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageId
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetMessagesInSameExclusiveLocation @Inject constructor(
    private val observeConversationMessages: ObserveConversationMessages
) {

    /**
     * Returns the list of messages in the given conversation matching the exclusive location of the provided messageId.
     * The provided `labelId` can be any existing label id, as it's just used as a workaround
     * to properly resolve the messages list.
     *
     * @param userId the user id.
     * @param conversationId the current conversation id.
     * @param messageId the reference message id.
     * @param labelId a valid label id to allow the message list to be resolved.
     */
    suspend operator fun invoke(
        userId: UserId,
        conversationId: ConversationId,
        messageId: MessageId,
        labelId: LabelId,
        entryPoint: ConversationDetailEntryPoint
    ): Either<DataError, List<Message>> = either {
        val messages = observeConversationMessages
            .invoke(userId, conversationId, labelId, entryPoint)
            .firstOrNull()
            ?.getOrNull()
            ?.messages
            ?: raise(DataError.Local.NoDataCached)

        val currentMessage = messages.firstOrNull { it.messageId == messageId }
        messages.filter { it.exclusiveLocation == currentMessage?.exclusiveLocation }
    }
}
