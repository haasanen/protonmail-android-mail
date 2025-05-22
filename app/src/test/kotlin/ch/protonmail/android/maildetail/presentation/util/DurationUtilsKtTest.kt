package ch.protonmail.android.maildetail.presentation.util

import android.content.res.Resources
import ch.protonmail.android.mailcommon.presentation.R
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

class DurationUtilsKtTest {

    private val resources = mockk<Resources>()

    @Test
    fun `should return minutes when expiration time is less than one hour`() {
        // Given
        val duration = Duration.parseIsoString("P0DT0H25M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_minutes_full_word,
                duration.inWholeMinutes.toInt(),
                duration.inWholeMinutes.toInt()
            )
        } returns "25 minutes"

        // When
        val actual = duration.toFormattedExpirationTime(resources)

        // Then
        val expected = listOf("25 minutes")
        assertEquals(expected, actual)
    }

    @Test
    fun `should return hours and minutes when expiration time is less than one day`() {
        // Given
        val duration = Duration.parseIsoString("P0DT3H25M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_hours_full_word,
                duration.inWholeHours.toInt(),
                duration.inWholeHours.toInt()
            )
        } returns "3 hours"
        every {
            resources.getQuantityString(
                R.plurals.expiration_minutes_full_word,
                (duration.inWholeMinutes - duration.inWholeHours * 60).toInt(),
                (duration.inWholeMinutes - duration.inWholeHours * 60).toInt()
            )
        } returns "25 minutes"

        // When
        val actual = duration.toFormattedExpirationTime(resources)

        // Then
        val expected = listOf("3 hours", "25 minutes")
        assertEquals(expected, actual)
    }

    @Test
    fun `should return days, hours and minutes when expiration time is less than one year`() {
        // Given
        val duration = Duration.parseIsoString("P5DT3H25M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_days_full_word,
                duration.inWholeDays.toInt(),
                duration.inWholeDays.toInt()
            )
        } returns "5 days"
        every {
            resources.getQuantityString(
                R.plurals.expiration_hours_full_word,
                (duration.inWholeHours - duration.inWholeDays * 24).toInt(),
                (duration.inWholeHours - duration.inWholeDays * 24).toInt()
            )
        } returns "3 hours"
        every {
            resources.getQuantityString(
                R.plurals.expiration_minutes_full_word,
                (duration.inWholeMinutes - duration.inWholeHours * 60).toInt(),
                (duration.inWholeMinutes - duration.inWholeHours * 60).toInt()
            )
        } returns "25 minutes"

        // When
        val actual = duration.toFormattedExpirationTime(resources)

        // Then
        val expected = listOf("5 days", "3 hours", "25 minutes")
        assertEquals(expected, actual)
    }

    @Test
    fun `should return seconds when expiration time is less than one minute`() {
        // Given
        val duration = Duration.parseIsoString("P0DT0H0M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_seconds_full_word,
                duration.inWholeSeconds.toInt(),
                duration.inWholeSeconds.toInt()
            )
        } returns "15 seconds"

        // When
        val actual = duration.toFormattedExpirationTime(resources)

        // Then
        val expected = listOf("15 seconds")
        assertEquals(expected, actual)
    }

    @Test
    fun `should return days when auto delete time is less than one year`() {
        // Given
        val duration = Duration.parseIsoString("P5DT3H25M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_days_full_word,
                duration.inWholeDays.toInt(),
                duration.inWholeDays.toInt()
            )
        } returns "5 days"

        // When
        val actual = duration.toFormattedAutoDeleteTime(resources)

        // Then
        val expected = listOf("5 days")
        assertEquals(expected, actual)
    }

    @Test
    fun `should return hours and minutes when auto delete time is less than one day`() {
        // Given
        val duration = Duration.parseIsoString("P0DT3H25M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_hours_full_word,
                duration.inWholeHours.toInt(),
                duration.inWholeHours.toInt()
            )
        } returns "3 hours"
        every {
            resources.getQuantityString(
                R.plurals.expiration_minutes_full_word,
                (duration.inWholeMinutes - duration.inWholeHours * 60).toInt(),
                (duration.inWholeMinutes - duration.inWholeHours * 60).toInt()
            )
        } returns "25 minutes"

        // When
        val actual = duration.toFormattedAutoDeleteTime(resources)

        // Then
        val expected = listOf("3 hours", "25 minutes")
        assertEquals(expected, actual)
    }

    @Test
    fun `should return minutes when auto delete time is less than one hour`() {
        // Given
        val duration = Duration.parseIsoString("P0DT0H25M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_minutes_full_word,
                duration.inWholeMinutes.toInt(),
                duration.inWholeMinutes.toInt()
            )
        } returns "25 minutes"

        // When
        val actual = duration.toFormattedAutoDeleteTime(resources)

        // Then
        val expected = listOf("25 minutes")
        assertEquals(expected, actual)
    }

    @Test
    fun `should return seconds when auto delete time is less than one minute`() {
        // Given
        val duration = Duration.parseIsoString("P0DT0H0M15S")
        every {
            resources.getQuantityString(
                R.plurals.expiration_seconds_full_word,
                duration.inWholeSeconds.toInt(),
                duration.inWholeSeconds.toInt()
            )
        } returns "15 seconds"

        // When
        val actual = duration.toFormattedAutoDeleteTime(resources)

        // Then
        val expected = listOf("15 seconds")
        assertEquals(expected, actual)
    }
}
