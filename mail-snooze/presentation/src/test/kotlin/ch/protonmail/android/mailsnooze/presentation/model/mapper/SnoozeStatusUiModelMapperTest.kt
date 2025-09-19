/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsnooze.presentation.model.mapper

import ch.protonmail.android.mailsnooze.domain.model.NoSnooze
import ch.protonmail.android.mailsnooze.domain.model.Snoozed
import ch.protonmail.android.mailsnooze.presentation.mapper.SnoozeStatusUiModelMapper
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeStatusUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class SnoozeStatusUiModelMapperTest {

    private val dayTimeMapper = mockk<DayTimeMapper>()
    private val sut = SnoozeStatusUiModelMapper(dayTimeMapper)

    @Test
    fun `when less than an hour there should be a highlight`() {
        every { dayTimeMapper.toDayTime(any()) } returns "test"
        val result = sut.toUiModel(
            Snoozed(
                Clock.System.now().plus(59.minutes)
            )
        )

        Assert.assertTrue(result is SnoozeStatusUiModel.SnoozeStatus)
        Assert.assertTrue((result as SnoozeStatusUiModel.SnoozeStatus).highlight)
    }

    @Test
    fun `when more than an hour there should be no highlight`() {
        every { dayTimeMapper.toDayTime(any()) } returns "test"
        val result = sut.toUiModel(
            Snoozed(
                Clock.System.now().plus(100.minutes)
            )
        )

        Assert.assertTrue(result is SnoozeStatusUiModel.SnoozeStatus)
        Assert.assertFalse((result as SnoozeStatusUiModel.SnoozeStatus).highlight)
    }

    @Test
    fun `not snoozed returns no status`() {
        val result = sut.toUiModel(NoSnooze)

        Assert.assertTrue(result is SnoozeStatusUiModel.NoStatus)
    }
}
