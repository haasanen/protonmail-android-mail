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

package ch.protonmail.android.mailmailbox.presentation.mailbox.mapper

import java.time.Duration
import java.time.Instant
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ExpiryInformationUiModel
import javax.inject.Inject
import kotlin.time.toKotlinDuration

class ExpiryInformationUiModelMapper @Inject constructor() {

    fun toUiModel(expirationTime: Long): ExpiryInformationUiModel {
        if (expirationTime <= 0) {
            return ExpiryInformationUiModel.NoExpiry
        }

        val duration = Duration.between(
            Instant.now(),
            Instant.ofEpochSecond(expirationTime)
        ).toKotlinDuration()

        val days = duration.inWholeDays
        val hours = duration.inWholeHours % 24
        val minutes = duration.inWholeMinutes % 60
        val seconds = duration.inWholeSeconds % 60

        return when {
            days > 0 -> ExpiryInformationUiModel.HasExpiry(
                TextUiModel.PluralisedText(
                    R.plurals.expires_in_days,
                    days.toInt()
                ),
                false
            )

            hours > 0 -> ExpiryInformationUiModel.HasExpiry(
                TextUiModel.PluralisedText(
                    R.plurals.expires_in_hours,
                    hours.toInt()
                ),
                false
            )

            minutes > 0 -> ExpiryInformationUiModel.HasExpiry(
                TextUiModel.PluralisedText(
                    R.plurals.expires_in_minutes,
                    minutes.toInt()
                ),
                true
            )

            seconds > 0 -> ExpiryInformationUiModel.HasExpiry(
                TextUiModel(
                    R.string.expires_in_less_than_a_minute
                ),
                true
            )

            else -> ExpiryInformationUiModel.NoExpiry
        }
    }
}
