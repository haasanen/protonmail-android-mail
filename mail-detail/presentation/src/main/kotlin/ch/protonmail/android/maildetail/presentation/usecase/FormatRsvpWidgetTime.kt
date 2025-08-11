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

package ch.protonmail.android.maildetail.presentation.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.content.Context
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.mailmessage.domain.model.RsvpOccurrence
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FormatRsvpWidgetTime @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAppLocale: GetAppLocale
) {

    operator fun invoke(
        occurrence: RsvpOccurrence,
        startsAt: Long,
        endsAt: Long
    ): TextUiModel {
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM").withLocale(getAppLocale())
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withLocale(getAppLocale())

        val today = LocalDate.now(ZoneId.systemDefault())
        val tomorrow = today.plusDays(1)

        when (occurrence) {

            RsvpOccurrence.Date -> {

                val startDate = Instant.ofEpochSecond(startsAt).atZone(ZoneId.of("UTC")).toLocalDate()
                val endDate = Instant.ofEpochSecond(endsAt).atZone(ZoneId.of("UTC")).toLocalDate()

                return if (startDate == endDate) {

                    when (startDate) {
                        today -> TextUiModel.TextRes(R.string.rsvp_widget_today)
                        tomorrow -> TextUiModel.TextRes(R.string.rsvp_widget_tomorrow)
                        else -> TextUiModel.Text(startDate.format(dateFormatter))
                    }
                } else {
                    val formattedStartDate = startDate.format(dateFormatter)
                    val formattedEndDate = endDate.format(dateFormatter)

                    TextUiModel.Text("$formattedStartDate - $formattedEndDate")
                }
            }

            RsvpOccurrence.DateTime -> {

                val startDateTime = Instant.ofEpochSecond(startsAt).atZone(ZoneId.systemDefault())
                val endDateTime = Instant.ofEpochSecond(endsAt).atZone(ZoneId.systemDefault())
                val startDate = startDateTime.toLocalDate()
                val endDate = endDateTime.toLocalDate()

                val formattedEndDate = endDateTime.format(dateFormatter)
                val formattedStartTime = startDateTime.format(timeFormatter)
                val formattedEndTime = endDateTime.format(timeFormatter)

                return if (startDate == endDate) {
                    val formattedStartDate = when (startDate) {
                        today -> context.resources.getString(R.string.rsvp_widget_today)
                        tomorrow -> context.resources.getString(R.string.rsvp_widget_tomorrow)
                        else -> startDateTime.format(dateFormatter)
                    }
                    TextUiModel.Text("$formattedStartDate • $formattedStartTime - $formattedEndTime")
                } else {
                    val formattedStartDate = startDateTime.format(dateFormatter)
                    TextUiModel.Text("$formattedStartDate, $formattedStartTime - $formattedEndDate, $formattedEndTime")
                }
            }
        }
    }
}
