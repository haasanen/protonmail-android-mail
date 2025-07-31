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

import androidx.compose.ui.graphics.Color
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.domain.model.RsvpAttendance
import ch.protonmail.android.maildetail.domain.model.RsvpAttendee
import ch.protonmail.android.maildetail.domain.model.RsvpAttendeeStatus
import ch.protonmail.android.maildetail.domain.model.RsvpCalendar
import ch.protonmail.android.maildetail.domain.model.RsvpEventDetails
import ch.protonmail.android.maildetail.domain.model.RsvpOrganizer
import ch.protonmail.android.maildetail.domain.model.RsvpState
import ch.protonmail.android.maildetail.presentation.model.RsvpAnswer
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpCalendarUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpOrganizerUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpWidgetUiModel
import ch.protonmail.android.maildetail.presentation.usecase.FormatRsvpWidgetTime
import javax.inject.Inject

class RsvpWidgetUiModelMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val formatRsvpWidgetTime: FormatRsvpWidgetTime,
    private val rsvpStatusUiModelMapper: RsvpStatusUiModelMapper,
    private val rsvpButtonsUiModelMapper: RsvpButtonsUiModelMapper
) {

    fun toUiModel(eventDetails: RsvpEventDetails): RsvpWidgetUiModel {
        val currentAttendee = eventDetails.attendees[eventDetails.userAttendeeIdx]

        return RsvpWidgetUiModel(
            title = TextUiModel.Text(eventDetails.summary),
            dateTime = formatRsvpWidgetTime(eventDetails.occurrence, eventDetails.startsAt, eventDetails.endsAt),
            isAttendanceOptional = isAttendanceOptional(eventDetails.state),
            buttons = rsvpButtonsUiModelMapper.toUiModel(eventDetails.state, currentAttendee.status.toRsvpAnswer()),
            calendar = eventDetails.calendar.toUiModel(),
            recurrence = eventDetails.recurrence?.let { TextUiModel.Text(it) },
            location = eventDetails.location?.let { TextUiModel.Text(it) },
            organizer = eventDetails.organizer.toUiModel(),
            attendees = eventDetails.attendees.map { it.toUiModel() },
            status = rsvpStatusUiModelMapper.toUiModel(eventDetails.state)
        )
    }

    private fun isAttendanceOptional(state: RsvpState) = when (state) {
        is RsvpState.AnswerableInvite -> when (state.attendance) {
            RsvpAttendance.Optional -> true
            RsvpAttendance.Required -> false
        }
        else -> false
    }

    private fun RsvpCalendar.toUiModel() = RsvpCalendarUiModel(
        color = colorMapper.toColor(this.color).getOrElse { Color.Unspecified },
        name = TextUiModel.Text(this.name)
    )

    private fun RsvpOrganizer.toUiModel() = RsvpOrganizerUiModel(
        name = this.name?.let { TextUiModel.Text(it) },
        email = TextUiModel.Text(this.email)
    )

    private fun RsvpAttendee.toUiModel() = RsvpAttendeeUiModel(
        answer = this.status.toRsvpAnswer(),
        name = this.name?.let { TextUiModel.Text(it) },
        email = TextUiModel.Text(this.email)
    )

    private fun RsvpAttendeeStatus.toRsvpAnswer() = when (this) {
        RsvpAttendeeStatus.Unanswered -> RsvpAnswer.Unanswered
        RsvpAttendeeStatus.Maybe -> RsvpAnswer.Maybe
        RsvpAttendeeStatus.No -> RsvpAnswer.No
        RsvpAttendeeStatus.Yes -> RsvpAnswer.Yes
    }
}
