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

object DayOfWeekMapper {

    private val daysMap: HashMap<DayOfWeek, SnoozeWeekStart> = hashMapOf(
        DayOfWeek.MONDAY to SnoozeWeekStart.MONDAY,
        DayOfWeek.TUESDAY to SnoozeWeekStart.TUESDAY,
        DayOfWeek.WEDNESDAY to SnoozeWeekStart.WEDNESDAY,
        DayOfWeek.THURSDAY to SnoozeWeekStart.THURSDAY,
        DayOfWeek.FRIDAY to SnoozeWeekStart.FRIDAY,
        DayOfWeek.SATURDAY to SnoozeWeekStart.SATURDAY,
        DayOfWeek.SUNDAY to SnoozeWeekStart.SUNDAY
    )

    fun DayOfWeek.toSnoozeWeekStart(): SnoozeWeekStart = daysMap[this] ?: SnoozeWeekStart.MONDAY
}
