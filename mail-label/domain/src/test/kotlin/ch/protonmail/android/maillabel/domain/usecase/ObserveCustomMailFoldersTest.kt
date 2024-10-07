package ch.protonmail.android.maillabel.domain.usecase

import android.graphics.Color
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import ch.protonmail.android.testdata.label.LabelTestData.buildLabel
import ch.protonmail.android.testdata.maillabel.MailLabelTestData.buildCustomFolder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ObserveCustomMailFoldersTest {

    private val labelRepository = mockk<LabelRepository>()

    private val observeCustomMailFolders = ObserveCustomMailFolders(labelRepository)

    @Before
    fun setUp() {
        mockkStatic(Color::class)
        every { Color.parseColor(any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Color::class)
    }

    @Test
    fun `returns custom folders as MailLabels when repository succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val customF0 = buildCustomFolder("0", level = 0, order = 0, parent = null, children = listOf("0.1", "0.2"))
        val customF01 = buildCustomFolder("0.1", level = 1, order = 0, parent = customF0)
        val customF02 = buildCustomFolder(
            "0.2", level = 1, order = 1, parent = customF0, children = listOf("0.2.1", "0.2.2")
        )
        val customF021 = buildCustomFolder("0.2.1", level = 2, order = 0, parent = customF02)
        val customF022 = buildCustomFolder("0.2.2", level = 2, order = 1, parent = customF02)
        val expected = listOf(customF0, customF01, customF02, customF021, customF022)
        val labels = listOf(
            buildLabel(id = "0", type = LabelType.MessageFolder, order = 0),
            buildLabel(id = "0.1", type = LabelType.MessageFolder, order = 0, parentId = "0"),
            buildLabel(id = "0.2", type = LabelType.MessageFolder, order = 1, parentId = "0"),
            buildLabel(id = "0.2.1", type = LabelType.MessageFolder, order = 0, parentId = "0.2"),
            buildLabel(id = "0.2.2", type = LabelType.MessageFolder, order = 1, parentId = "0.2")
        )

        every { labelRepository.observeCustomFolders(userId) } returns flowOf(labels)


        // When
        val actual = observeCustomMailFolders(userId)

        // Then
        assertEquals(expected.right(), actual.firstOrNull())
    }

}
