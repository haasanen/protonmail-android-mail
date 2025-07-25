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

package ch.protonmail.android.mailmessage.presentation.model.snooze

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailcommon.domain.model.SnoozeOption
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmessage.presentation.R

sealed interface SnoozeOptionUiModel
data class SnoozeUntilUiModel(
    val action: SnoozeOperationViewAction,
    @DrawableRes val icon: Int,
    val title: TextUiModel,
    val detail: TextUiModel
) : SnoozeOptionUiModel

data class CustomSnoozeUiModel(val action: SnoozeOperationViewAction) : SnoozeOptionUiModel
data class UpgradeToSnoozeUiModel(val action: SnoozeOperationViewAction) : SnoozeOptionUiModel


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

internal fun SnoozeOption.toSnoozeOptionUiModel() = when (this) {
    is SnoozeOption.NextWeek -> snoozeUntil(
        R.drawable.ic_proton_suitcase,
        R.string.snooze_sheet_option_next_week,
        this.description
    )

    is SnoozeOption.Tomorrow -> snoozeUntil(
        R.drawable.ic_proton_sun,
        R.string.snooze_sheet_option_tomorrow,
        this.description
    )

    is SnoozeOption.ThisWeekend -> snoozeUntil(
        R.drawable.ic_proton_chair,
        R.string.snooze_sheet_option_this_weekend,
        this.description
    )

    is SnoozeOption.LaterThisWeek -> snoozeUntil(
        R.drawable.ic_proton_sun_half,
        R.string.snooze_sheet_option_later_this_week,
        this.description
    )

    SnoozeOption.Allowed -> CustomSnoozeUiModel(SnoozeOperationViewAction.PickSnooze)
    SnoozeOption.UpgradeRequired -> UpgradeToSnoozeUiModel(SnoozeOperationViewAction.Upgrade)
}
