package ch.protonmail.android.maildetail.presentation.mapper.rsvp

import androidx.compose.ui.graphics.Color
import arrow.core.right
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmessage.domain.model.CalendarId
import ch.protonmail.android.mailmessage.domain.model.EventId
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendee
import ch.protonmail.android.mailmessage.domain.model.RsvpAttendeeStatus
import ch.protonmail.android.mailmessage.domain.model.RsvpCalendar
import ch.protonmail.android.mailmessage.domain.model.RsvpEvent
import ch.protonmail.android.mailmessage.domain.model.RsvpOccurrence
import ch.protonmail.android.mailmessage.domain.model.RsvpOrganizer
import ch.protonmail.android.mailmessage.domain.model.RsvpState
import ch.protonmail.android.maildetail.presentation.model.RsvpAnswer
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpButtonsUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpCalendarUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpEventUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpOrganizerUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpStatusUiModel
import ch.protonmail.android.maildetail.presentation.usecase.FormatRsvpWidgetTime
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class RsvpEventUiModelMapperTest {

    private val colorMapper = mockk<ColorMapper> {
        every { this@mockk.toColor(any()) } returns Color.Magenta.right()
    }
    private val formatRsvpWidgetTime = mockk<FormatRsvpWidgetTime> {
        every { this@mockk(any(), any(), any()) } returns TextUiModel.Text("15 Jul • 14:30 - 15:30")
    }
    private val rsvpStatusUiModelMapper = mockk<RsvpStatusUiModelMapper> {
        every { this@mockk.toUiModel(any()) } returns RsvpStatusUiModel.EventCancelled
    }
    private val rsvpButtonsUiModelMapper = mockk<RsvpButtonsUiModelMapper> {
        every { this@mockk.toUiModel(any(), any()) } returns RsvpButtonsUiModel.Hidden
    }

    private val mapper = RsvpEventUiModelMapper(
        colorMapper = colorMapper,
        formatRsvpWidgetTime = formatRsvpWidgetTime,
        rsvpStatusUiModelMapper = rsvpStatusUiModelMapper,
        rsvpButtonsUiModelMapper = rsvpButtonsUiModelMapper
    )

    @Test
    fun `correctly map the RSVP event to a ui model`() {
        // Given
        val eventDetails = RsvpEvent(
            eventId = EventId("id"),
            summary = "Inbox OKR Weekly",
            location = "Room 234",
            description = "Description",
            recurrence = "Weekly on Monday",
            startsAt = 123,
            endsAt = 124,
            occurrence = RsvpOccurrence.DateTime,
            organizer = RsvpOrganizer(
                name = "Samantha Peterson",
                email = "samantha@protonmail.com"
            ),
            attendees = listOf(
                RsvpAttendee(
                    status = RsvpAttendeeStatus.Unanswered,
                    name = "You",
                    email = "you@protonmail.com"
                ),
                RsvpAttendee(
                    status = RsvpAttendeeStatus.Yes,
                    name = "Madison Long",
                    email = "madison@protonmail.com"
                ),
                RsvpAttendee(
                    status = RsvpAttendeeStatus.Maybe,
                    name = null,
                    email = "email@protonmail.com"
                ),
                RsvpAttendee(
                    status = RsvpAttendeeStatus.No,
                    name = "Olivia Reed",
                    email = "olivia@protonmail.com"
                )
            ),
            userAttendeeIdx = 0,
            calendar = RsvpCalendar(
                id = CalendarId("id"),
                name = "Work",
                color = "Color"
            ),
            state = RsvpState.CancelledReminder
        )

        // When
        val actual = mapper.toUiModel(eventDetails)

        // Then
        val expected = RsvpEventUiModel(
            title = TextUiModel.Text("Inbox OKR Weekly"),
            dateTime = TextUiModel.Text("15 Jul • 14:30 - 15:30"),
            isAttendanceOptional = false,
            buttons = RsvpButtonsUiModel.Hidden,
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
            status = RsvpStatusUiModel.EventCancelled
        )
        assertEquals(expected, actual)
    }
}
