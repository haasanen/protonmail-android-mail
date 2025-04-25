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

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabels
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import io.mockk.mockk
import io.mockk.every
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ObserveCurrentMailLabelTest {

    private val observeMailLabels = mockk<ObserveMailLabels>()
    private val mutableSelectedIdFlow = MutableStateFlow<MailLabelId>(MailLabelTestData.sentSystemLabel.id)
    private val selectedMailLabelId = mockk<SelectedMailLabelId> {
        every { flow } returns mutableSelectedIdFlow.asStateFlow()
    }

    private val observeCurrentMailLabel = ObserveCurrentMailLabel(observeMailLabels, selectedMailLabelId)

    private val userId = UserIdSample.Primary

    @Test
    fun `should emit current label when selected label exists in mail labels`() = runTest {
        // Given
        val selectedId = MailLabelTestData.sentSystemLabel.id
        val expectedLabel = MailLabelTestData.sentSystemLabel
        val mailLabels = mockk<MailLabels> {
            every { allById } returns mapOf(selectedId to expectedLabel)
        }

        every { observeMailLabels(userId) } returns flowOf(mailLabels)

        // When
        val result = observeCurrentMailLabel(userId).first()

        // Then
        assertEquals(expectedLabel, result)
    }

    @Test
    fun `should emit null when selected label does not exist in mail labels`() = runTest {
        // Given
        val nonExistentId = MailLabelId.System(LabelIdSample.Label2021)
        mutableSelectedIdFlow.value = nonExistentId

        val mailLabels = mockk<MailLabels> {
            every { allById } returns emptyMap()
        }

        every { observeMailLabels(userId) } returns flowOf(mailLabels)

        // When
        val result = observeCurrentMailLabel(userId).first()

        // Then
        assertNull(result)
    }

}
