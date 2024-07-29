/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.maillabel.data.local

import app.cash.turbine.test
import ch.protonmail.android.maillabel.data.repository.RustLabelRepository
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.testdata.label.LabelTestData
import ch.protonmail.android.testdata.label.rust.LocalLabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelType
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Enclosed::class)
class RustLabelRepositoryTest {

    private val labelDataSource = mockk<LabelDataSource>()

    private val labelRepository = RustLabelRepository(labelDataSource)

    @Test
    fun `observe system labels from rust data source`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val localLabelWithCount = LocalLabelTestData.localSystemLabelWithCount
        val expectedLabel = LabelTestData.systemLabel

        every { labelDataSource.observeSystemLabels(userId) } returns flowOf(listOf(localLabelWithCount))

        // When
        labelRepository.observeLabels(userId, LabelType.SystemFolder).test {
            // Then
            assertEquals(listOf(expectedLabel), resultOrNull(awaitItem()))
            awaitComplete()
        }
    }

    @Test
    fun `observe message labels from rust data source`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val localLabelWithCount = LocalLabelTestData.localMessageLabelWithCount
        val expectedLabel = LabelTestData.messageLabel

        every { labelDataSource.observeMessageLabels(userId) } returns flowOf(listOf(localLabelWithCount))

        // When
        labelRepository.observeLabels(userId, LabelType.MessageLabel).test {
            // Then
            assertEquals(listOf(expectedLabel), resultOrNull(awaitItem()))
            awaitComplete()
        }
    }

    @Test
    fun `observe message folder from rust data source`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val localLabelWithCount = LocalLabelTestData.localMessageFolderWithCount
        val expectedLabel = LabelTestData.messageFolder

        every { labelDataSource.observeMessageFolders(userId) } returns flowOf(listOf(localLabelWithCount))

        // When
        labelRepository.observeLabels(userId, LabelType.MessageFolder).test {
            // Then
            assertEquals(listOf(expectedLabel), resultOrNull(awaitItem()))
            awaitComplete()
        }
    }

    private fun resultOrNull(dataResult: DataResult<List<Label>>): List<Label>? = when (dataResult) {
        is DataResult.Success -> dataResult.value
        else -> null
    }

    @RunWith(Parameterized::class)
    class SystemLabelTest(
        @Suppress("unused") private val testName: String,
        private val input: TestInput
    ) {

        private val labelDataSource = mockk<LabelDataSource>()

        private val labelRepository = RustLabelRepository(labelDataSource)

        @Test
        fun `test label with system label id is mapped correctly`() = runTest {
            // Given
            val userId = UserIdTestData.userId
            val localLabelWithCount = LocalLabelTestData.buildSystem(input.systemLabelRawId)
            every { labelDataSource.observeSystemLabels(userId) } returns flowOf(listOf(localLabelWithCount))

            // When
            labelRepository.observeSystemLabels(userId).test {

                // Then
                assertEquals(input.expected, awaitItem().first().systemLabelId)
                awaitComplete()
            }
        }

        companion object {

            val inputs = listOf(
                TestInput(
                    SystemLabelId.Inbox.labelId.id,
                    SystemLabelId.Inbox
                ),
                TestInput(
                    SystemLabelId.Archive.labelId.id,
                    SystemLabelId.Archive
                ),
                TestInput(
                    SystemLabelId.AllDrafts.labelId.id,
                    SystemLabelId.AllDrafts
                ),
                TestInput(
                    SystemLabelId.AllSent.labelId.id,
                    SystemLabelId.AllSent
                ),
                TestInput(
                    SystemLabelId.Trash.labelId.id,
                    SystemLabelId.Trash
                ),
                TestInput(
                    SystemLabelId.Spam.labelId.id,
                    SystemLabelId.Spam
                ),
                TestInput(
                    SystemLabelId.AllMail.labelId.id,
                    SystemLabelId.AllMail
                ),
                TestInput(
                    SystemLabelId.Sent.labelId.id,
                    SystemLabelId.Sent
                ),
                TestInput(
                    SystemLabelId.Drafts.labelId.id,
                    SystemLabelId.Drafts
                ),
                TestInput(
                    SystemLabelId.Outbox.labelId.id,
                    SystemLabelId.Outbox
                ),
                TestInput(
                    SystemLabelId.Starred.labelId.id,
                    SystemLabelId.Starred
                ),
                TestInput(
                    SystemLabelId.AllScheduled.labelId.id,
                    SystemLabelId.AllScheduled
                ),
                TestInput(
                    SystemLabelId.Snoozed.labelId.id,
                    SystemLabelId.Snoozed
                )
            )

            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = inputs
                .map { testInput ->
                    val testName = """
                        Raw system label Id: ${testInput.systemLabelRawId}
                        Expected: ${testInput.expected}
                    """.trimIndent()
                    arrayOf(testName, testInput)
                }
        }

        data class TestInput(
            val systemLabelRawId: String,
            val expected: SystemLabelId
        )
    }


}
