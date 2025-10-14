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

package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.mailconversation.domain.entity.HiddenMessagesBanner
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.HiddenMessagesBannerState
import javax.inject.Inject

class HiddenMessagesBannerReducer @Inject constructor() {

    fun newStateFrom(operation: ConversationDetailOperation.AffectingHiddenMessagesBanner) = when (operation) {
        is ConversationDetailEvent.ConversationData -> reduceConversationData(operation)
    }

    private fun reduceConversationData(operation: ConversationDetailEvent.ConversationData) = with(operation) {

        when {
            hiddenMessagesBanner != null -> {
                when (hiddenMessagesBanner) {
                    HiddenMessagesBanner.ContainsTrashedMessages -> HiddenMessagesBannerState.Shown(
                        message = R.string.trashed_messages_banner,
                        isSwitchTurnedOn = operation.showAllMessages
                    )
                    HiddenMessagesBanner.ContainsNonTrashedMessages -> HiddenMessagesBannerState.Shown(
                        message = R.string.non_trashed_messages_banner,
                        isSwitchTurnedOn = operation.showAllMessages
                    )
                }
            }
            else -> HiddenMessagesBannerState.Hidden
        }
    }
}
