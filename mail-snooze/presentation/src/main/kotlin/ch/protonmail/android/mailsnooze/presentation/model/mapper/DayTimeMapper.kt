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

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import javax.inject.Inject
import kotlin.time.Instant

class DayTimeMapper @Inject constructor(
    private val getAppLocale: GetAppLocale
) {

    fun toDayTime(instant: Instant): String = DateFormat.getDateTimeInstance().let {
        SimpleDateFormat(DAY_TIME_SHORT, getAppLocale())
            .format(instant.toCalendar().time)
    }

    fun toTime(instant: Instant): String = DateFormat.getDateTimeInstance().let {
        SimpleDateFormat(TIME, getAppLocale())
            .format(instant.toCalendar().time)
    }

    private fun Instant.toCalendar(): Calendar {
        val itemCalendar = Calendar.getInstance(getAppLocale())
        itemCalendar.time = Date(this.toEpochMilliseconds())
        return itemCalendar
    }

    companion object {

        const val DAY_TIME_SHORT = "EEE, h:mm"
        const val TIME = "h:mm"
    }
}

