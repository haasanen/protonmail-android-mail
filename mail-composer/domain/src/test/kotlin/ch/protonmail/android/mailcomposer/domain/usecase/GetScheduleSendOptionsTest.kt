package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.ScheduleSendOptions
import ch.protonmail.android.mailcomposer.domain.model.ScheduleSendOptionsWithPreviousTime
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.PreviousScheduleSendTime
import ch.protonmail.android.mailmessage.domain.repository.PreviousScheduleSendTimeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class GetScheduleSendOptionsTest {

    private val draftRepository = mockk<DraftRepository>()
    private val previousScheduleSendTimeRepository = mockk<PreviousScheduleSendTimeRepository>()

    private val getScheduleSendOptions = GetScheduleSendOptions(
        draftRepository,
        previousScheduleSendTimeRepository
    )

    @Test
    fun `get schedule send options successfully`() = runTest {
        // Given
        val scheduleSendOptions = ScheduleSendOptions(
            Instant.fromEpochSeconds(123),
            Instant.fromEpochSeconds(456),
            isCustomTimeOptionAvailable = false
        )
        givenGetScheduleSendOptionsSucceeds(scheduleSendOptions)
        givenNoPreviousScheduleTimeOption()

        // When
        val actual = getScheduleSendOptions()

        // Then
        assertEquals(scheduleSendOptions, actual.getOrNull()?.options)
    }

    @Test
    fun `returns error when get schedule send options fails`() = runTest {
        // Given
        val expected = DataError.Local.NotFound
        givenGetScheduleSendOptionsFails(expected)

        // When
        val actualEither = getScheduleSendOptions()

        // Then
        assertEquals(expected.left(), actualEither)
    }

    @Test
    fun `adds previous schedule send time option when available`() = runTest {
        // Given
        val scheduleSendOptions = ScheduleSendOptions(
            Instant.fromEpochSeconds(123),
            Instant.fromEpochSeconds(456),
            isCustomTimeOptionAvailable = false
        )
        val previousScheduleTime = PreviousScheduleSendTime(Instant.fromEpochSeconds(789))
        val expected = ScheduleSendOptionsWithPreviousTime(scheduleSendOptions, previousScheduleTime)
        givenGetScheduleSendOptionsSucceeds(scheduleSendOptions)
        givenThereIsAPreviousScheduleTimeOption(previousScheduleTime.time)

        // When
        val actual = getScheduleSendOptions()

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `adds null as previous schedule send time option when none available`() = runTest {
        // Given
        val scheduleSendOptions = ScheduleSendOptions(
            Instant.fromEpochSeconds(123),
            Instant.fromEpochSeconds(456),
            isCustomTimeOptionAvailable = true
        )
        val expected = ScheduleSendOptionsWithPreviousTime(
            options = scheduleSendOptions,
            previousTime = null
        )
        givenGetScheduleSendOptionsSucceeds(scheduleSendOptions)
        givenNoPreviousScheduleTimeOption()

        // When
        val actual = getScheduleSendOptions()

        // Then
        assertEquals(expected.right(), actual)
    }

    private fun givenThereIsAPreviousScheduleTimeOption(time: Instant) {
        coEvery { previousScheduleSendTimeRepository.get() } returns PreviousScheduleSendTime(time)
    }

    private fun givenNoPreviousScheduleTimeOption() {
        coEvery { previousScheduleSendTimeRepository.get() } returns null
    }

    private fun givenGetScheduleSendOptionsSucceeds(scheduleSendOptions: ScheduleSendOptions) {
        coEvery { draftRepository.getScheduleSendOptions() } returns scheduleSendOptions.right()
    }

    private fun givenGetScheduleSendOptionsFails(expected: DataError) {
        coEvery { draftRepository.getScheduleSendOptions() } returns expected.left()
    }
}
