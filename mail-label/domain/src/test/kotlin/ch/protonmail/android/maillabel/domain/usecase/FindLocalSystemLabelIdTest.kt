package ch.protonmail.android.maillabel.domain.usecase

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabels
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import kotlin.test.assertEquals

class FindLocalSystemLabelIdTest {

    private val getAllMailLocalLabelId: GetAllMailLocalLabelId = mockk()
    private val observeMailLabels: ObserveMailLabels = mockk()

    private val findLocalSystemLabelId = FindLocalSystemLabelId(
        observeMailLabels = observeMailLabels,
        getAllMailLocalLabelId = getAllMailLocalLabelId
    )

    @Test
    fun `returns local label id of the matching system mail label`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val system = listOf(MailLabelTestData.inboxSystemLabel, MailLabelTestData.archiveSystemLabel)
        givenSystemMailLabels(userId, system)

        // When
        val actual = findLocalSystemLabelId(userId, SystemLabelId.Archive)

        // Then
        assertEquals(MailLabelTestData.archiveSystemLabel.id, actual)
    }

    @Test
    fun `returns null when no matching system mail label is found for this labelId`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val system = listOf(MailLabelTestData.inboxSystemLabel, MailLabelTestData.spamSystemLabel)
        givenSystemMailLabels(userId, system)

        // When
        val actual = findLocalSystemLabelId(userId, SystemLabelId.Archive)

        // Then
        assertNull(actual)
    }

    @Test
    fun `returns all mail label id when given system label id is All Mail or Almost All Mail`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val system = listOf(MailLabelTestData.inboxSystemLabel, MailLabelTestData.allMailSystemLabel)
        val expectedAllMailLabelId = LabelId("local all mail label id")
        givenSystemMailLabels(userId, system)
        coEvery { getAllMailLocalLabelId(userId) } returns expectedAllMailLabelId

        // When
        val actual = findLocalSystemLabelId(userId, SystemLabelId.AllMail)

        // Then
        assertEquals(expectedAllMailLabelId, actual?.labelId)
        verify { observeMailLabels wasNot Called }
    }

    private fun givenSystemMailLabels(userId: UserId, system: List<MailLabel.System>) {
        coEvery { observeMailLabels(userId) } returns flowOf(
            MailLabels(
                system = system,
                folders = emptyList(),
                labels = emptyList()
            )
        )
    }
}
