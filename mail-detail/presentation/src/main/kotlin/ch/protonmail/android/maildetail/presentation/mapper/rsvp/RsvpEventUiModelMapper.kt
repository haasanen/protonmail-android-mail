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
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendance
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendee
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendeeStatus
import ch.protonmail.android.mailmessage.domain.model.RsvpCalendar
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import ch.protonmail.android.mailmessage.domain.model.RsvpOrganizer
import ch.protonmail.android.mailmessage.domain.model.RsvpState
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeAnswer
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpCalendarUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpEventUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpOrganizerUiModel
import ch.protonmail.android.maildetail.presentation.usecase.FormatRsvpWidgetTime
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import javax.inject.Inject

class RsvpEventUiModelMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val formatRsvpWidgetTime: FormatRsvpWidgetTime,
    private val rsvpStatusUiModelMapper: RsvpStatusUiModelMapper,
    private val rsvpButtonsUiModelMapper: RsvpButtonsUiModelMapper
) {

    fun toUiModel(eventDetails: RsvpEvent, answer: RsvpAnswer? = null): RsvpEventUiModel {
        // When currentAttendee is null, the user is the organizer of the event
        val currentAttendee = eventDetails.userAttendeeIdx?.let {
            eventDetails.attendees[it]
        }

        return RsvpEventUiModel(
            eventId = eventDetails.eventId,
            startsAt = eventDetails.startsAt,
            title = eventDetails.getEventTitle(),
            dateTime = formatRsvpWidgetTime(eventDetails.occurrence, eventDetails.startsAt, eventDetails.endsAt),
            isAttendanceOptional = isAttendanceOptional(eventDetails.state),
            buttons = rsvpButtonsUiModelMapper.toUiModel(
                eventDetails.state,
                getRsvpAnswer(answer, currentAttendee?.status),
                isAnsweringInProgress = answer != null
            ),
            calendar = eventDetails.calendar?.toUiModel(),
            recurrence = eventDetails.recurrence?.let { TextUiModel.Text(it) },
            location = eventDetails.location?.let { TextUiModel.Text(it) },
            organizer = eventDetails.organizer.toUiModel(),
            attendees = eventDetails.attendees.mapIndexed { index, rsvpAttendee ->
                rsvpAttendee.toUiModel(isUserAttendee = index == eventDetails.userAttendeeIdx)
            },
            status = rsvpStatusUiModelMapper.toUiModel(eventDetails.state)
        )
    }

    private fun RsvpEvent.getEventTitle() =
        summary?.let { TextUiModel.Text(it) } ?: TextUiModel.TextRes(R.string.rsvp_widget_no_title)

    private fun isAttendanceOptional(state: RsvpState) = when (state) {
        is RsvpState.AnswerableInvite -> when (state.attendance) {
            RsvpAttendance.Optional -> true
            RsvpAttendance.Required -> false
        }
        else -> false
    }

    private fun getRsvpAnswer(answerInProgress: RsvpAnswer?, status: RsvpAttendeeStatus?) =
        answerInProgress?.toRsvpAttendeeAnswer() ?: status?.toRsvpAttendeeAnswer()

    private fun RsvpCalendar.toUiModel() = RsvpCalendarUiModel(
        calendarId = this.id,
        color = colorMapper.toColor(this.color).getOrElse { Color.Unspecified },
        name = TextUiModel.Text(this.name)
    )

    private fun RsvpOrganizer.toUiModel() = RsvpOrganizerUiModel(
        name = this.name?.let { TextUiModel.Text(it) },
        email = TextUiModel.Text(this.email)
    )

    private fun RsvpAttendee.toUiModel(isUserAttendee: Boolean) = RsvpAttendeeUiModel(
        answer = this.status.toRsvpAttendeeAnswer(),
        name = if (isUserAttendee) {
            TextUiModel.TextRes(R.string.rsvp_widget_you)
        } else {
            this.name?.let { TextUiModel.Text(it) }
        },
        email = TextUiModel.Text(this.email)
    )

    private fun RsvpAttendeeStatus.toRsvpAttendeeAnswer() = when (this) {
        RsvpAttendeeStatus.Unanswered -> RsvpAttendeeAnswer.Unanswered
        RsvpAttendeeStatus.Maybe -> RsvpAttendeeAnswer.Maybe
        RsvpAttendeeStatus.No -> RsvpAttendeeAnswer.No
        RsvpAttendeeStatus.Yes -> RsvpAttendeeAnswer.Yes
    }

    private fun RsvpAnswer.toRsvpAttendeeAnswer() = when (this) {
        RsvpAnswer.Maybe -> RsvpAttendeeAnswer.Maybe
        RsvpAnswer.No -> RsvpAttendeeAnswer.No
        RsvpAnswer.Yes -> RsvpAttendeeAnswer.Yes
    }
}
