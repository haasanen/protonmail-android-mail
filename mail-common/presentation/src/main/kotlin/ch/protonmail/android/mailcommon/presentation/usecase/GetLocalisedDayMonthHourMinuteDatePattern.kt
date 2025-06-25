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

package ch.protonmail.android.mailcommon.presentation.usecase

import android.icu.text.DateFormat
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import javax.inject.Inject

class GetLocalisedDayMonthHourMinuteDateFormat @Inject constructor(
    private val getAppLocale: GetAppLocale
) {

    operator fun invoke(): DateFormat {

        // Skeleton pattern for date format containing day and month
        val skeletonPattern = DateFormat.ABBR_MONTH + DateFormat.DAY + DateFormat.HOUR + DateFormat.MINUTE

        // Get the date format for the current locale. The following function uses DateTimePatternGenerator
        // to find the best pattern for the given skeleton pattern
        return DateFormat.getPatternInstance(skeletonPattern, getAppLocale())

    }
}
