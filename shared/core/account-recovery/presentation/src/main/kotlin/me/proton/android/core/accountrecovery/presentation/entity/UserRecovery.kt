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

package me.proton.android.core.accountrecovery.presentation.entity

import me.proton.core.domain.type.IntEnum
import me.proton.core.network.domain.session.SessionId

data class UserRecovery(
    val email: String,
    val remainingHours: Int,
    val isAccountRecoveryResetEnabled: Boolean,
    val selfInitiated: Boolean,
    val startDateFormatted: String, // formatted and calculated startDate - currentDateTime
    val endDateFormatted: String, // formatted and calculated endDate - currentDateTime
    val durationFormatted: String, // formatted and calculated endDate - currentDateTime
    val state: IntEnum<State>,
    val startTime: Long,
    val endTime: Long,
    val sessionId: SessionId,
    val reason: Reason?
) {
    enum class State(val value: Int) {
        None(0),
        Grace(1),
        Cancelled(2),
        Insecure(3),
        Expired(4);

        companion object {
            val map = values().associateBy { it.value }
            fun enumOf(value: Int): IntEnum<State> = IntEnum(value, map[value])
        }
    }

    enum class Reason(val value: Int) {
        None(0),
        Cancelled(1),
        Authentication(2);

        companion object {
            val map = values().associateBy { it.value }
        }
    }
}
