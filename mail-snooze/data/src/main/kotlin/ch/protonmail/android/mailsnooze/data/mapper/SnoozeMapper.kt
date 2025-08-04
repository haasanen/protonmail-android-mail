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

package ch.protonmail.android.mailsnooze.data.mapper

import ch.protonmail.android.mailcommon.data.mapper.LocalNonDefaultWeekStart
import ch.protonmail.android.mailsnooze.domain.model.SnoozeOption
import ch.protonmail.android.mailsnooze.domain.model.SnoozeWeekStart
import timber.log.Timber
import uniffi.proton_mail_uniffi.SnoozeActions
import uniffi.proton_mail_uniffi.SnoozeTime
import kotlin.time.Instant

fun SnoozeWeekStart.toLocalWeekStart() = when (this) {
    SnoozeWeekStart.MONDAY -> LocalNonDefaultWeekStart.MONDAY
    SnoozeWeekStart.SATURDAY -> LocalNonDefaultWeekStart.SATURDAY
    SnoozeWeekStart.SUNDAY -> LocalNonDefaultWeekStart.SUNDAY
    else -> {
        Timber.w("Unsupported week start given $this defaulting to Monday")
        LocalNonDefaultWeekStart.MONDAY
    }
}

fun SnoozeActions.toSnoozeActions(): List<SnoozeOption> {
    fun ULong.toInstant() = Instant.fromEpochSeconds(this.toLong())
    return this.options.map {
        when (it) {
            is SnoozeTime.Tomorrow -> SnoozeOption.Tomorrow(it.v1.toInstant())
            is SnoozeTime.ThisWeekend -> SnoozeOption.ThisWeekend(it.v1.toInstant())
            is SnoozeTime.LaterThisWeek -> SnoozeOption.LaterThisWeek(it.v1.toInstant())
            is SnoozeTime.NextWeek -> SnoozeOption.NextWeek(it.v1.toInstant())
            is SnoozeTime.Custom -> SnoozeOption.Allowed
        }
    }.toMutableList().apply {
        if (!options.contains(SnoozeTime.Custom)) {
            add(SnoozeOption.UpgradeRequired)
        }
        if (showUnsnooze) {
            add(SnoozeOption.UnSnooze)
        }
    }
}
