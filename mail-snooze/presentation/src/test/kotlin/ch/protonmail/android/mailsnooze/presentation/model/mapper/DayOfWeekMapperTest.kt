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

import java.time.DayOfWeek
import ch.protonmail.android.mailsnooze.domain.model.SnoozeWeekStart
import ch.protonmail.android.mailsnooze.presentation.model.mapper.DayOfWeekMapper.toSnoozeWeekStart
import org.junit.Assert
import org.junit.Test

class DayOfWeekMapperTest {

    @Test
    fun `map days of the week to Snooze days of the week `() {
        Assert.assertEquals(DayOfWeek.MONDAY.toSnoozeWeekStart(), SnoozeWeekStart.MONDAY)
        Assert.assertEquals(DayOfWeek.TUESDAY.toSnoozeWeekStart(), SnoozeWeekStart.TUESDAY)
        Assert.assertEquals(DayOfWeek.WEDNESDAY.toSnoozeWeekStart(), SnoozeWeekStart.WEDNESDAY)
        Assert.assertEquals(DayOfWeek.THURSDAY.toSnoozeWeekStart(), SnoozeWeekStart.THURSDAY)
        Assert.assertEquals(DayOfWeek.FRIDAY.toSnoozeWeekStart(), SnoozeWeekStart.FRIDAY)
        Assert.assertEquals(DayOfWeek.SATURDAY.toSnoozeWeekStart(), SnoozeWeekStart.SATURDAY)
        Assert.assertEquals(DayOfWeek.SUNDAY.toSnoozeWeekStart(), SnoozeWeekStart.SUNDAY)
    }
}
