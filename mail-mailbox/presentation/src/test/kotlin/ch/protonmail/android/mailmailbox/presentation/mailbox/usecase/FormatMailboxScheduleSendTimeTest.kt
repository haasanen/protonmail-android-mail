package ch.protonmail.android.mailmailbox.presentation.mailbox.usecase

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.icu.text.DateFormat
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.domain.usecase.GetLocalisedCalendar
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.usecase.GetLocalisedDayMonthHourMinuteDateFormat
import ch.protonmail.android.mailmailbox.presentation.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class FormatMailboxScheduleSendTimeTest {

    private val getLocalisedCalendar = mockk<GetLocalisedCalendar>()
    private val getAppLocale = mockk<GetAppLocale>()
    private val getLocalisedDayMonthHourMinuteDateFormat = mockk<GetLocalisedDayMonthHourMinuteDateFormat>()
    private val mockedDateFormat = mockk<DateFormat>()
    private val formatter = FormatMailboxScheduleSendTime(
        getLocalisedCalendar,
        getAppLocale,
        getLocalisedDayMonthHourMinuteDateFormat
    )

    @Before
    fun setUp() {
        mockkStatic(TimeZone::class)
        every { TimeZone.getDefault() } returns TimeZone.getTimeZone("Europe/Zurich")

        mockkStatic(DateFormat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when the schedule time is tomorrow show localized 'tomorrow' string`() {
        // Given
        givenCurrentTimeAndLocale(Instant.fromEpochSeconds(1_658_772_437), Locale.UK) // Mon Jul 25 20:07:17 CEST 2022
        val scheduleTime = Instant.fromEpochSeconds(1_658_853_752) // Tue Jul 26 18:42:35 CEST 2022
        val formattedTime = "18:42"

        // When
        val actual = formatter(scheduleTime)

        // Then
        assertIs<TextUiModel.TextResWithArgs>(actual)
        assertEquals(TextUiModel.TextResWithArgs(R.string.schedule_send_tomorrow, listOf(formattedTime)), actual)
    }

    @Test
    fun `when the schedule time is today show localized 'today' string`() {
        // Given
        // Wednesday, June 25, 2025 2:19:28 PM
        givenCurrentTimeAndLocale(Instant.fromEpochSeconds(1_750_861_168), Locale.UK)
        val scheduleTime = Instant.fromEpochSeconds(1_750_879_800) // Wednesday, June 25, 2025 7:30:00 PM
        val formattedTime = "21:30" // CEST

        // When
        val actual = formatter(scheduleTime)

        // Then
        assertIs<TextUiModel.TextResWithArgs>(actual)
        assertEquals(TextUiModel.TextResWithArgs(R.string.schedule_send_today, listOf(formattedTime)), actual)
    }

    @Test
    fun `when the schedule time is not today or tomorrow show localized short date`() {
        // Given
        givenCurrentTimeAndLocale(
            Instant.fromEpochSeconds(1_658_772_437),
            Locale.ITALIAN
        ) // Mon Jul 25 20:07:17 CEST 2022
        val expected = "28 Luglio, 09:42"
        every { DateFormat.getDateInstance(any(), any<Locale>()) } returns mockedDateFormat
        every { mockedDateFormat.format(any()) } returns expected
        val scheduleTime = Instant.fromEpochSeconds(1_658_994_137) // Thu Jul 28 09:42:17 CEST 2022

        // When
        val actual = formatter(scheduleTime)

        // Then
        assertIs<TextUiModel.Text>(actual, actual.toString())
        assertEquals(TextUiModel.Text(expected), actual)
    }

    private fun givenCurrentTimeAndLocale(currentTime: Instant, locale: Locale) {
        val calendar = Calendar.getInstance(locale)
        calendar.time = Date(currentTime.toEpochMilliseconds())

        every { getLocalisedCalendar() } returns calendar
        every { getAppLocale() } returns locale
        every { getLocalisedDayMonthHourMinuteDateFormat() } returns mockedDateFormat
    }

}
