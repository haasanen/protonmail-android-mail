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

package ch.protonmail.android.mailconversation.domain.usecase

import arrow.core.Either
import arrow.core.combine
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.usecase.ObserveCustomMailFolders
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetConversationMoveToLocations @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val observeCustomMailFolders: ObserveCustomMailFolders
) {

    suspend operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        conversationIds: List<ConversationId>
    ): Either<DataError, List<MailLabel>> {
        val systemLocations = conversationRepository.getSystemMoveToLocations(userId, labelId, conversationIds)
        val customLocations = observeCustomMailFolders(userId).firstOrNull() ?: emptyList<MailLabel.Custom>().right()

        return systemLocations.combine(
            customLocations,
            { systemError, _ -> systemError },
            { systemLabels, customLabels -> systemLabels + customLabels }
        )
    }
}
