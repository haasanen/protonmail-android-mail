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

package ch.protonmail.android.mailconversation.domain.usecase

import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.domain.model.ViewMode
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import javax.inject.Inject

class IsExpandableLocation @Inject constructor(
    private val conversationsRepository: ConversationRepository,
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(viewMode: ViewMode) = when (viewMode) {
        ViewMode.ConversationGrouping -> conversationsRepository.supportsIncludeFilter()
        ViewMode.NoConversationGrouping -> messageRepository.supportsIncludeFilter()
    }
}
