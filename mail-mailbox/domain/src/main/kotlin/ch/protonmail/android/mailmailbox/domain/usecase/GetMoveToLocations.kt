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

package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.Either
import arrow.core.combine
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.toMailLabelCustom
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ViewMode
import javax.inject.Inject

class GetMoveToLocations @Inject constructor(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val labelRepository: LabelRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        mailboxItemIds: List<MailboxItemId>,
        viewMode: ViewMode
    ): Either<DataError, List<MailLabel>> {
        val systemActions = when (viewMode) {
            ViewMode.ConversationGrouping -> {
                val conversationIds = mailboxItemIds.map { ConversationId(it.value) }
                conversationRepository.getSystemMoveToLocations(userId, labelId, conversationIds)
            }

            ViewMode.NoConversationGrouping -> {
                val messageIds = mailboxItemIds.map { MessageId(it.value) }
                messageRepository.getSystemMoveToLocations(userId, labelId, messageIds)
            }
        }

        val customActions = either<DataError, List<MailLabel>> {
            val customFolders = labelRepository.observeCustomFolders(userId).firstOrNull()
                ?: emptyList()
            customFolders.toMailLabelCustom()
        }

        return systemActions.combine(
            customActions,
            { systemError, _ -> systemError },
            { systemLabels, customLabels -> systemLabels + customLabels }
        )
    }

}
