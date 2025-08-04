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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsnooze.domain.model.SnoozeOption
import ch.protonmail.android.mailsnooze.presentation.R
import ch.protonmail.android.mailsnooze.presentation.model.CustomSnoozeUiModel
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOperationViewAction
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeUntilUiModel
import ch.protonmail.android.mailsnooze.presentation.model.UnSnooze
import ch.protonmail.android.mailsnooze.presentation.model.UpgradeToSnoozeUiModel

object SnoozeOptionUiModelMapper {

    private fun snoozeUntil(
        @DrawableRes icon: Int,
        @StringRes title: Int,
        detail: String
    ) = SnoozeUntilUiModel(
        action = SnoozeOperationViewAction.SnoozeUntil,
        icon = icon,
        title = TextUiModel(title),
        detail = TextUiModel(detail)
    )

    internal fun SnoozeOption.toSnoozeOptionUiModel(mapper: DayTimeMapper) = when (this) {
        is SnoozeOption.NextWeek -> snoozeUntil(
            R.drawable.ic_proton_briefcase,
            R.string.snooze_sheet_option_next_week,
            mapper.toDayTime(snoozeTime)
        )

        is SnoozeOption.Tomorrow -> snoozeUntil(
            R.drawable.ic_proton_sun,
            R.string.snooze_sheet_option_tomorrow,
            mapper.toTime(snoozeTime)
        )

        is SnoozeOption.ThisWeekend -> snoozeUntil(
            R.drawable.ic_proton_chair,
            R.string.snooze_sheet_option_this_weekend,
            mapper.toDayTime(snoozeTime)
        )

        is SnoozeOption.LaterThisWeek -> snoozeUntil(
            R.drawable.ic_proton_sun_half,
            R.string.snooze_sheet_option_later_this_week,
            mapper.toDayTime(snoozeTime)
        )

        SnoozeOption.Allowed -> CustomSnoozeUiModel(SnoozeOperationViewAction.PickSnooze)
        SnoozeOption.UpgradeRequired -> UpgradeToSnoozeUiModel(SnoozeOperationViewAction.Upgrade)
        SnoozeOption.UnSnooze -> UnSnooze
    }
}
