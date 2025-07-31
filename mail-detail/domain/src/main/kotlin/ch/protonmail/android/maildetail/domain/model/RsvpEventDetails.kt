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

package ch.protonmail.android.maildetail.domain.model

data class RsvpEventDetails(
    val eventId: EventId,
    val summary: String,
    val location: String?,
    val description: String?,
    val recurrence: String?,
    val startsAt: Long,
    val endsAt: Long,
    val occurrence: RsvpOccurrence,
    val organizer: RsvpOrganizer,
    val attendees: List<RsvpAttendee>,
    val userAttendeeIdx: Int,
    val calendar: RsvpCalendar,
    val state: RsvpState
)

data class EventId(
    val id: String
)

data class RsvpOrganizer(
    val name: String?,
    val email: String
)

data class RsvpAttendee(
    val name: String?,
    val email: String,
    val status: RsvpAttendeeStatus
)

data class RsvpCalendar(
    val id: CalendarId,
    val name: String,
    val color: String
)

data class CalendarId(
    val id: String
)

sealed interface RsvpState {
    data class AnswerableInvite(val progress: RsvpProgress, val attendance: RsvpAttendance) : RsvpState
    data class UnanswerableInvite(val reason: RsvpUnanswerableReason) : RsvpState
    data class CancelledInvite(val isOutdated: Boolean) : RsvpState
    data class Reminder(val progress: RsvpProgress) : RsvpState
    data object CancelledReminder : RsvpState
}

enum class RsvpProgress { Pending, Ongoing, Ended }

enum class RsvpAttendance { Optional, Required }

enum class RsvpUnanswerableReason { InviteIsOutdated, InviteHasUnknownRecency }

enum class RsvpOccurrence { Date, DateTime }

enum class RsvpAttendeeStatus { Unanswered, Maybe, No, Yes }
