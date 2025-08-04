package ch.protonmail.android.maildetail.presentation.usecase

import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import android.content.Context
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.mailmessage.domain.model.RsvpOccurrence
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatRsvpWidgetTimeTest {

    private val context = mockk<Context>()
    private val mockGetAppLocale = mockk<GetAppLocale> {
        every { this@mockk() } returns Locale.US
    }

    private val formatRsvpWidgetTime = FormatRsvpWidgetTime(context, mockGetAppLocale)

    @Test
    fun `when single day event occurrence is today, returns today text resource`() {
        // Given
        val today = LocalDate.now(ZoneId.systemDefault())
        val startsAt = today.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val endsAt = today.atTime(23, 59).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

        // When
        val result = formatRsvpWidgetTime(RsvpOccurrence.Date, startsAt, endsAt)

        // Then
        assertEquals(TextUiModel.TextRes(R.string.rsvp_widget_today), result)
    }

    @Test
    fun `when single day event occurrence is tomorrow, returns tomorrow text resource`() {
        // Given
        val tomorrow = LocalDate.now(ZoneId.systemDefault()).plusDays(1)
        val startsAt = tomorrow.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val endsAt = tomorrow.atTime(23, 59).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

        // When
        val result = formatRsvpWidgetTime(RsvpOccurrence.Date, startsAt, endsAt)

        // Then
        assertEquals(TextUiModel.TextRes(R.string.rsvp_widget_tomorrow), result)
    }

    @Test
    fun `when single day event occurrence is more than 2 days in the future, returns formatted date`() {
        // Given
        val futureDate = LocalDate.now(ZoneId.systemDefault()).plusDays(10)
        val startsAt = futureDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val endsAt = futureDate.atTime(23, 59).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

        // When
        val result = formatRsvpWidgetTime(RsvpOccurrence.Date, startsAt, endsAt)

        // Then
        val expectedMonth = futureDate.month.name.lowercase().replaceFirstChar { it.uppercase() }.substring(0, 3)
        assertEquals(TextUiModel.Text("${futureDate.dayOfMonth} $expectedMonth"), result)
    }

    @Test
    fun `when event occurrence spans multiple full days, returns date range`() {
        // Given
        val startDate = LocalDate.of(2024, 6, 15)
        val endDate = startDate.plusDays(5)
        val startsAt = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val endsAt = endDate.atTime(23, 59).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

        // When
        val result = formatRsvpWidgetTime(RsvpOccurrence.Date, startsAt, endsAt)

        // Then
        assertEquals(TextUiModel.Text("15 Jun - 20 Jun"), result)
    }

    @Test
    fun `when event occurrence is at a specific time in a single day, returns date with time range`() {
        // Given
        val baseDate = LocalDate.of(2024, 6, 15)
        val startsAt = baseDate.atTime(14, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endsAt = baseDate.atTime(16, 45).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // When
        val result = formatRsvpWidgetTime(RsvpOccurrence.DateTime, startsAt, endsAt)

        // Then
        assertEquals(TextUiModel.Text("15 Jun • 14:30 - 16:45"), result)
    }

    @Test
    fun `when event occurrence is at a specific time and spans multiple days, returns full date-time range`() {
        // Given
        val startDate = LocalDate.of(2024, 6, 15)
        val endDate = startDate.plusDays(2)
        val startsAt = startDate.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endsAt = endDate.atTime(17, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // When
        val result = formatRsvpWidgetTime(RsvpOccurrence.DateTime, startsAt, endsAt)

        // Then
        assertEquals(TextUiModel.Text("15 Jun, 09:00 - 17 Jun, 17:30"), result)
    }
}
