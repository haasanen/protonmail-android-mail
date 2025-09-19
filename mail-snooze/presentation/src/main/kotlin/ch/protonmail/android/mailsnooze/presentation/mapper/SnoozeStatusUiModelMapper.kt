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
import ch.protonmail.android.mailsnooze.presentation.model.mapper.DayTimeMapper
import javax.inject.Inject
import kotlin.time.Clock

class SnoozeStatusUiModelMapper @Inject constructor(private val dayTimeMapper: DayTimeMapper) {

    fun toUiModel(snoozeInfo: ConversationSnoozeStatus): SnoozeStatusUiModel {

        when (snoozeInfo) {
            is Snoozed -> {
                val duration = snoozeInfo.until.minus(Clock.System.now())
                val text = TextUiModel(
                    R.string.snooze_sheet_success,
                    dayTimeMapper.toDayTime(snoozeInfo.until)
                )
                return SnoozeStatusUiModel.SnoozeStatus(
                    formattedDateText = text,
                    highlight = duration.inWholeHours <= 0
                )
            }

            else -> return SnoozeStatusUiModel.NoStatus
        }
    }
}
