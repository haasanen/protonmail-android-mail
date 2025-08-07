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

package ch.protonmail.android.mailcomposer.presentation.model

import androidx.annotation.StringRes
import ch.protonmail.android.mailcomposer.presentation.R
import kotlin.time.Instant

data class ExpirationTimeUiModel(
    val selectedOption: ExpirationTimeOption,
    val customTime: Instant? = null
)

enum class ExpirationTimeOption(@StringRes val textResId: Int) {
    None(R.string.composer_bottom_bar_expiration_time_none),
    OneHour(R.string.composer_bottom_bar_expiration_time_one_hour),
    OneDay(R.string.composer_bottom_bar_expiration_time_one_day),
    ThreeDays(R.string.composer_bottom_bar_expiration_time_three_days),
    Custom(R.string.composer_bottom_bar_expiration_time_custom)
}

fun ExpirationTimeUiModel.isExpirationTimeSet() = this.selectedOption != ExpirationTimeOption.None
