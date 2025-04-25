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

package ch.protonmail.android.maillabel.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.model.toDynamicSystemMailLabel
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import ch.protonmail.android.testdata.label.LabelTestData.buildLabel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ObserveSystemMailLabelsTest {

    private val labelRepository = mockk<LabelRepository>()
    private val observeSystemMailLabels = ObserveSystemMailLabels(labelRepository)

    private val userId = UserIdSample.Primary

    @Test
    fun `should emit system mail labels`() = runTest {
        // Given
        val labelWithIdList = listOf(
            LabelWithSystemLabelId(
                label = buildLabel(id = SystemLabelId.Sent.labelId.id, type = LabelType.SystemFolder),
                systemLabelId = SystemLabelId.Sent
            ),
            LabelWithSystemLabelId(
                label = buildLabel(id = SystemLabelId.Drafts.labelId.id, type = LabelType.SystemFolder),
                systemLabelId = SystemLabelId.Drafts
            )
        )
        val expected = labelWithIdList.toDynamicSystemMailLabel().right()
        coEvery { labelRepository.observeSystemLabels(userId) } returns flowOf(labelWithIdList)

        // When
        val result = observeSystemMailLabels(userId).first()

        // Then
        assertEquals(expected, result)
        verify(exactly = 1) { labelRepository.observeSystemLabels(userId) }
    }
}
