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

package ch.protonmail.android.mailmessage.data.mapper

import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpAnswer
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpAttendance
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpAttendee
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpAttendeeStatus
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpCalendar
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpEvent
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpOccurrence
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpOrganizer
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpProgress
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpState
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpStateAnswerableInvite
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpStateCancelledInvite
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpStateCancelledReminder
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpStateReminder
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpStateUnanswerableInvite
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpUnanswerableReason
import ch.protonmail.android.mailmessage.domain.model.CalendarId
import ch.protonmail.android.mailmessage.domain.model.EventId
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendance
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendee
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendeeStatus
import ch.protonmail.android.mailmessage.domain.model.RsvpCalendar
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import ch.protonmail.android.mailmessage.domain.model.RsvpOccurrence
import ch.protonmail.android.mailmessage.domain.model.RsvpOrganizer
import ch.protonmail.android.mailmessage.domain.model.RsvpProgress
import ch.protonmail.android.mailmessage.domain.model.RsvpState
import ch.protonmail.android.mailmessage.domain.model.RsvpUnanswerableReason

fun LocalRsvpEvent.toRsvpEvent() = RsvpEvent(
    eventId = this.id?.let { EventId(it) },
    summary = this.summary,
    location = this.location,
    description = this.description,
    recurrence = this.recurrence,
    startsAt = this.startsAt.toLong(),
    endsAt = this.endsAt.toLong(),
    occurrence = this.occurrence.toRsvpOccurrence(),
    organizer = this.organizer.toRsvpOrganizer(),
    attendees = this.attendees.map { it.toRsvpAttendee() },
    userAttendeeIdx = this.userAttendeeIdx?.toInt(),
    calendar = this.calendar?.toRsvpCalendar(),
    state = this.state.toRsvpState()
)

private fun LocalRsvpOccurrence.toRsvpOccurrence() = when (this) {
    LocalRsvpOccurrence.DATE -> RsvpOccurrence.Date
    LocalRsvpOccurrence.DATE_TIME -> RsvpOccurrence.DateTime
}

private fun LocalRsvpOrganizer.toRsvpOrganizer() = RsvpOrganizer(
    name = this.name,
    email = this.email
)

private fun LocalRsvpAttendee.toRsvpAttendee() = RsvpAttendee(
    name = this.name,
    email = this.email,
    status = this.status.toRsvpAttendeeStatus()
)

private fun LocalRsvpAttendeeStatus.toRsvpAttendeeStatus() = when (this) {
    LocalRsvpAttendeeStatus.UNANSWERED -> RsvpAttendeeStatus.Unanswered
    LocalRsvpAttendeeStatus.MAYBE -> RsvpAttendeeStatus.Maybe
    LocalRsvpAttendeeStatus.NO -> RsvpAttendeeStatus.No
    LocalRsvpAttendeeStatus.YES -> RsvpAttendeeStatus.Yes
}

private fun LocalRsvpCalendar.toRsvpCalendar() = RsvpCalendar(
    id = CalendarId(this.id),
    name = this.name,
    color = this.color
)

private fun LocalRsvpState.toRsvpState() = when (this) {
    is LocalRsvpStateAnswerableInvite -> RsvpState.AnswerableInvite(
        progress = this.progress.toRsvpProgress(),
        attendance = this.attendance.toRsvpAttendance()
    )
    is LocalRsvpStateCancelledInvite -> RsvpState.CancelledInvite(
        isOutdated = this.isOutdated
    )
    is LocalRsvpStateCancelledReminder -> RsvpState.CancelledReminder
    is LocalRsvpStateReminder -> RsvpState.Reminder(
        progress = this.progress.toRsvpProgress()
    )
    is LocalRsvpStateUnanswerableInvite -> RsvpState.UnanswerableInvite(
        reason = this.reason.toRsvpUnanswerableReason()
    )
}

private fun LocalRsvpProgress.toRsvpProgress() = when (this) {
    LocalRsvpProgress.PENDING -> RsvpProgress.Pending
    LocalRsvpProgress.ONGOING -> RsvpProgress.Ongoing
    LocalRsvpProgress.ENDED -> RsvpProgress.Ended
}

private fun LocalRsvpAttendance.toRsvpAttendance() = when (this) {
    LocalRsvpAttendance.OPTIONAL -> RsvpAttendance.Optional
    LocalRsvpAttendance.REQUIRED -> RsvpAttendance.Required
}

private fun LocalRsvpUnanswerableReason.toRsvpUnanswerableReason() = when (this) {
    LocalRsvpUnanswerableReason.INVITE_IS_OUTDATED -> RsvpUnanswerableReason.InviteIsOutdated
    LocalRsvpUnanswerableReason.ADDRESS_IS_INCORRECT -> RsvpUnanswerableReason.AddressIsIncorrect
    LocalRsvpUnanswerableReason.USER_IS_ORGANIZER -> RsvpUnanswerableReason.UserIsOrganizer
    LocalRsvpUnanswerableReason.USER_IS_NOT_INVITED -> RsvpUnanswerableReason.UserIsNotInvited
    LocalRsvpUnanswerableReason.EVENT_DOES_NOT_EXIST -> RsvpUnanswerableReason.EventDoesNotExist
    LocalRsvpUnanswerableReason.NETWORK_FAILURE -> RsvpUnanswerableReason.NetworkFailure
}

fun RsvpAnswer.toLocalRsvpAnswer() = when (this) {
    RsvpAnswer.Maybe -> LocalRsvpAnswer.MAYBE
    RsvpAnswer.No -> LocalRsvpAnswer.NO
    RsvpAnswer.Yes -> LocalRsvpAnswer.YES
}
