package ch.protonmail.android.maillabel.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class FindLocalSystemLabelIdTest {

    private val labelRepository = mockk<LabelRepository>()

    private val findLocalSystemLabelId = FindLocalSystemLabelId(labelRepository)

    @Test
    fun `should proxy the call to the labels repository (success)`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = LabelId("8")
        coEvery {
            labelRepository.resolveLocalIdBySystemLabel(userId, SystemLabelId.Archive)
        } returns expected.right()


        // When
        val actual = findLocalSystemLabelId(userId, SystemLabelId.Archive)

        // Then
        assertEquals(MailLabelId.System(expected), actual)
        coVerify(exactly = 1) { labelRepository.resolveLocalIdBySystemLabel(userId, SystemLabelId.Archive) }
        confirmVerified(labelRepository)
    }

    @Test
    fun `should proxy the call to the labels repository (failure)`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        coEvery {
            labelRepository.resolveLocalIdBySystemLabel(userId, SystemLabelId.Archive)
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = findLocalSystemLabelId(userId, SystemLabelId.Archive)

        // Then
        assertNull(actual)
        coVerify(exactly = 1) { labelRepository.resolveLocalIdBySystemLabel(userId, SystemLabelId.Archive) }
        confirmVerified(labelRepository)
    }
}
