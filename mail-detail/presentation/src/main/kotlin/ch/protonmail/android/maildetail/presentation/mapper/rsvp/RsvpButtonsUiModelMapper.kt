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

package ch.protonmail.android.maildetail.presentation.mapper.rsvp

import ch.protonmail.android.mailmessage.domain.model.RsvpProgress
import ch.protonmail.android.mailmessage.domain.model.RsvpState
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeAnswer
import ch.protonmail.android.maildetail.presentation.model.RsvpButtonsUiModel
import javax.inject.Inject

class RsvpButtonsUiModelMapper @Inject constructor() {

    fun toUiModel(state: RsvpState, attendeeAnswer: RsvpAttendeeAnswer?): RsvpButtonsUiModel {
        // When attendeeAnswer is null, the user is the organizer of the event
        if (attendeeAnswer == null) return RsvpButtonsUiModel.Hidden

        return when (state) {
            is RsvpState.AnswerableInvite -> when (state.progress) {
                RsvpProgress.Pending,
                RsvpProgress.Ongoing -> RsvpButtonsUiModel.Shown(attendeeAnswer)
                RsvpProgress.Ended -> RsvpButtonsUiModel.Hidden
            }
            else -> RsvpButtonsUiModel.Hidden
        }
    }
}
