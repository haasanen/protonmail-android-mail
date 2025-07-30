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

package ch.protonmail.android.maildetail.presentation.previewdata

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpAnswer
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpButtonsUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpCalendarUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpOrganizerUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpStatusUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpWidgetUiModel

object RsvpWidgetPreviewData {

    val UnansweredWithMultipleParticipants = RsvpWidgetUiModel(
        title = TextUiModel.Text("Whispers of Tomorrow: An Evening of Unexpected Wonders"),
        date = TextUiModel.Text("Thu, 15 Jul"),
        time = TextUiModel.Text("14:30 - 15:30 "),
        isAttendanceOptional = true,
        buttons = RsvpButtonsUiModel.Shown(RsvpAnswer.Unanswered),
        calendar = RsvpCalendarUiModel(
            color = Color.Magenta,
            name = TextUiModel.Text("Work")
        ),
        recurrence = TextUiModel.Text("Weekly on Monday"),
        location = TextUiModel.Text("Room 234"),
        organizer = RsvpOrganizerUiModel(
            name = TextUiModel.Text("Samantha Peterson"),
            email = TextUiModel.Text("samantha@protonmail.com")
        ),
        attendees = listOf(
            RsvpAttendeeUiModel(
                answer = RsvpAnswer.Unanswered,
                name = TextUiModel.Text("You"),
                email = TextUiModel.Text("you@protonmail.com")
            ),
            RsvpAttendeeUiModel(
                answer = RsvpAnswer.Yes,
                name = TextUiModel.Text("Madison Long"),
                email = TextUiModel.Text("madison@protonmail.com")
            ),
            RsvpAttendeeUiModel(
                answer = RsvpAnswer.Maybe,
                name = null,
                email = TextUiModel.Text("email@protonmail.com")
            ),
            RsvpAttendeeUiModel(
                answer = RsvpAnswer.No,
                name = TextUiModel.Text("Olivia Reed"),
                email = TextUiModel.Text("olivia@protonmail.com")
            )
        ),
        status = null
    )

    val AnsweredWithOneParticipantAndStatus = RsvpWidgetUiModel(
        title = TextUiModel.Text("Inbox OKR Weekly"),
        date = TextUiModel.Text("Thu, 15 Jul"),
        time = TextUiModel.Text("14:30 - 15:30 "),
        isAttendanceOptional = true,
        buttons = RsvpButtonsUiModel.Shown(RsvpAnswer.Yes),
        calendar = RsvpCalendarUiModel(
            color = Color.Magenta,
            name = TextUiModel.Text("Work")
        ),
        recurrence = TextUiModel.Text("Weekly on Monday"),
        location = TextUiModel.Text("Room 234"),
        organizer = RsvpOrganizerUiModel(
            name = TextUiModel.Text("Samantha Peterson"),
            email = TextUiModel.Text("samantha@protonmail.com")
        ),
        attendees = listOf(
            RsvpAttendeeUiModel(
                answer = RsvpAnswer.Yes,
                name = TextUiModel.Text("You"),
                email = TextUiModel.Text("you@protonmail.com")
            )
        ),
        status = RsvpStatusUiModel(
            message = TextUiModel.Text("Happening now"),
            textColor = Color(0xFF1E574B),
            backgroundColor = Color(0xFFB1E1D4)
        )
    )
}
