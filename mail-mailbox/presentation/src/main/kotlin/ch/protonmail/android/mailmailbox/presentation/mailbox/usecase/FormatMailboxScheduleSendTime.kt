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

package ch.protonmail.android.mailmailbox.presentation.mailbox.usecase

import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.domain.usecase.GetLocalisedCalendar
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.usecase.GetLocalisedDayMonthHourMinuteDateFormat
import ch.protonmail.android.mailmailbox.presentation.R
import javax.inject.Inject
import kotlin.time.Instant

class FormatMailboxScheduleSendTime @Inject constructor(
    private val getLocalisedCalendar: GetLocalisedCalendar,
    private val getAppLocale: GetAppLocale,
    private val getLocalisedDayMonthHourMinuteDateFormat: GetLocalisedDayMonthHourMinuteDateFormat
) {

    private val currentTime: Calendar
        get() = getLocalisedCalendar()

    operator fun invoke(itemTime: Instant): TextUiModel = when {
        itemTime.isToday() -> {
            TextUiModel.TextResWithArgs(
                R.string.schedule_send_today,
                listOf(itemTime.toHourAndMinutes())
            )
        }

        itemTime.isTomorrow() -> {
            TextUiModel.TextResWithArgs(
                R.string.schedule_send_tomorrow,
                listOf(itemTime.toHourAndMinutes())
            )
        }

        else -> {
            TextUiModel.Text(itemTime.toFullDateWithoutYear())
        }
    }

    private fun Instant.toFullDateWithoutYear(): String {
        val dateFormat = getLocalisedDayMonthHourMinuteDateFormat()

        return dateFormat.format(Date(this.toEpochMilliseconds()))
    }

    private fun Instant.toHourAndMinutes() = DateFormat.getTimeInstance(DateFormat.SHORT, getAppLocale())
        .format(Date(this.toEpochMilliseconds()))

    private fun isToday(itemCalendar: Calendar) = isCurrentYear(itemCalendar) &&
        itemCalendar.get(Calendar.DAY_OF_YEAR) == currentTime.get(Calendar.DAY_OF_YEAR)

    private fun isTomorrow(itemCalendar: Calendar) = isCurrentYear(itemCalendar) &&
        itemCalendar.get(Calendar.DAY_OF_YEAR) - currentTime.get(Calendar.DAY_OF_YEAR) == 1

    private fun isCurrentYear(itemCalendar: Calendar) =
        currentTime.get(Calendar.YEAR) == itemCalendar.get(Calendar.YEAR)

    private fun Instant.isToday() = isToday(toCalendar())

    private fun Instant.isTomorrow() = isTomorrow(toCalendar())

    private fun Instant.toCalendar(): Calendar {
        val itemCalendar = Calendar.getInstance(getAppLocale())
        itemCalendar.time = Date(this.toEpochMilliseconds())
        return itemCalendar
    }
}
