package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.ScheduleSendOptions
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class GetScheduleSendOptionsTest {

    private val draftRepository = mockk<DraftRepository>()

    private val getScheduleSendOptions = GetScheduleSendOptions(draftRepository)

    @Test
    fun `get schedule send options successfully`() = runTest {
        // Given
        val scheduleSendOptions = ScheduleSendOptions(
            Instant.fromEpochSeconds(123),
            Instant.fromEpochSeconds(456),
            isCustomTimeOptionAvailable = false
        )
        givenGetScheduleSendOptionsSucceeds(scheduleSendOptions)

        // When
        val actual = getScheduleSendOptions()

        // Then
        assertEquals(scheduleSendOptions.right(), actual)
    }

    @Test
    fun `returns error when get schedule send options fails`() = runTest {
        // Given
        val expected = DataError.Local.NoDraftId
        givenGetScheduleSendOptionsFails(expected)

        // When
        val actualEither = getScheduleSendOptions()

        // Then
        assertEquals(expected.left(), actualEither)
    }

    private fun givenGetScheduleSendOptionsSucceeds(scheduleSendOptions: ScheduleSendOptions) {
        coEvery { draftRepository.getScheduleSendOptions() } returns scheduleSendOptions.right()
    }

    private fun givenGetScheduleSendOptionsFails(expected: DataError) {
        coEvery { draftRepository.getScheduleSendOptions() } returns expected.left()
    }
}
