package ch.protonmail.android.maildetail.presentation.usecase

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.icu.text.DateFormat
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.domain.usecase.GetLocalisedCalendar
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
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

class FormatScheduleSendTimeTest {

    private val getLocalisedCalendar = mockk<GetLocalisedCalendar>()
    private val getAppLocale = mockk<GetAppLocale>()
    private val formatter = FormatScheduleSendTime(
        getLocalisedCalendar,
        getAppLocale
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
        assertIs<TextUiModel.TextResWithArgs>(actual, actual.toString())
        assertEquals(TextUiModel.TextResWithArgs(R.string.schedule_send_tomorrow, listOf(formattedTime)), actual)
    }

    @Test
    fun `when the schedule time is not tomorrow show localized full date`() {
        // Given
        givenCurrentTimeAndLocale(
            Instant.fromEpochSeconds(1_658_772_437),
            Locale.ITALIAN
        ) // Mon Jul 25 20:07:17 CEST 2022
        val scheduleTime = Instant.fromEpochSeconds(1_658_994_137) // Thu Jul 28 09:42:17 CEST 2022
        val formattedTime = "giovedì 28 luglio 2022, 09:42"

        // When
        val actual = formatter(scheduleTime)

        // Then
        assertIs<TextUiModel.Text>(actual, actual.toString())
        assertEquals(TextUiModel.Text(formattedTime), actual)
    }

    private fun givenCurrentTimeAndLocale(currentTime: Instant, locale: Locale) {
        val calendar = Calendar.getInstance(locale)
        calendar.time = Date(currentTime.toEpochMilliseconds())

        every { getLocalisedCalendar() } returns calendar
        every { getAppLocale() } returns locale
    }

}
