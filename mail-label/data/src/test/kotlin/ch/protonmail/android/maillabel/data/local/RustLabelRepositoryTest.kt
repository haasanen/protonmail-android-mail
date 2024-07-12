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
import ch.protonmail.android.testdata.label.LabelTestData
import ch.protonmail.android.testdata.label.rust.LocalLabelTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelType
import kotlin.test.Test
import kotlin.test.assertEquals

class RustLabelRepositoryTest {

    private val labelDataSource = mockk<LabelDataSource>()

    private val labelRepository = RustLabelRepository(labelDataSource)

    @Test
    fun `observe system labels from rust data source`() = runTest {
        // Given
        val localLabelWithCount = LocalLabelTestData.localSystemLabelWithCount
        val expectedLabel = LabelTestData.systemLabel

        every { labelDataSource.observeSystemLabels() } returns flowOf(listOf(localLabelWithCount))

        // When
        labelRepository.observeLabels(UserId("anyuser"), LabelType.SystemFolder).test {
            // Then
            assertEquals(listOf(expectedLabel), resultOrNull(awaitItem()))
            awaitComplete()
        }
    }

    @Test
    fun `observe message labels from rust data source`() = runTest {
        // Given
        val localLabelWithCount = LocalLabelTestData.localMessageLabelWithCount
        val expectedLabel = LabelTestData.messageLabel

        every { labelDataSource.observeMessageLabels() } returns flowOf(listOf(localLabelWithCount))

        // When
        labelRepository.observeLabels(UserId("anyuser"), LabelType.MessageLabel).test {
            // Then
            assertEquals(listOf(expectedLabel), resultOrNull(awaitItem()))
            awaitComplete()
        }
    }

    @Test
    fun `observe message folder from rust data source`() = runTest {
        // Given
        val localLabelWithCount = LocalLabelTestData.localMessageFolderWithCount
        val expectedLabel = LabelTestData.messageFolder

        every { labelDataSource.observeMessageFolders() } returns flowOf(listOf(localLabelWithCount))

        // When
        labelRepository.observeLabels(UserId("anyuser"), LabelType.MessageFolder).test {
            // Then
            assertEquals(listOf(expectedLabel), resultOrNull(awaitItem()))
            awaitComplete()
        }
    }


    private fun resultOrNull(dataResult: DataResult<List<Label>>): List<Label>? = when (dataResult) {
        is DataResult.Success -> dataResult.value
        else -> null
    }

}
