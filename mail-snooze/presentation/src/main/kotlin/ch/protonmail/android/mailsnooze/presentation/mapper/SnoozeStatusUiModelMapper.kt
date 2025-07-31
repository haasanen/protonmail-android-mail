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

package ch.protonmail.android.mailsnooze.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsnooze.domain.model.ConversationSnoozeStatus
import ch.protonmail.android.mailsnooze.domain.model.Snoozed
import ch.protonmail.android.mailsnooze.presentation.R
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeStatusUiModel
import javax.inject.Inject
import kotlin.time.Clock

class SnoozeStatusUiModelMapper @Inject constructor() {

    fun toUiModel(snoozeInfo: ConversationSnoozeStatus): SnoozeStatusUiModel {
        when (snoozeInfo) {
            is Snoozed -> {
                val duration = snoozeInfo.until.minus(Clock.System.now())
                if (duration.inWholeMilliseconds <= 0) {
                    return SnoozeStatusUiModel.NoStatus
                }

                val days = duration.inWholeDays
                val hours = duration.inWholeHours % 24
                val minutes = duration.inWholeMinutes % 60

                return when {
                    days > 0 -> SnoozeStatusUiModel.SnoozeStatus(
                        TextUiModel.PluralisedText(
                            R.plurals.snoozed_in_days,
                            days.toInt()
                        ),
                        false
                    )

                    hours > 0 -> SnoozeStatusUiModel.SnoozeStatus(
                        TextUiModel.PluralisedText(
                            R.plurals.snooze_in_hours,
                            hours.toInt()
                        ),
                        false
                    )

                    minutes > 0 -> SnoozeStatusUiModel.SnoozeStatus(
                        TextUiModel.PluralisedText(
                            R.plurals.snooze_in_minutes,
                            minutes.toInt()
                        ),
                        true
                    )

                    else -> SnoozeStatusUiModel.NoStatus
                }
            }

            else -> {
                return SnoozeStatusUiModel.NoStatus
            }
        }
    }
}
