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

package ch.protonmail.android.mailpinlock.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class Autolock(
    val autolockInterval: AutoLockInterval = AutoLockInterval.Immediately,
    val protectionType: Protection = Protection.None,
    val biometricsState: AutoLockBiometricsState = AutoLockBiometricsState.BiometricsNotAvailable
) {

    companion object {

        fun default() = Autolock()
    }
}

enum class AutoLockInterval(val duration: Duration) {
    Immediately(0.seconds),
    FiveMinutes(5.minutes),
    FifteenMinutes(15.minutes),
    OneHour(1.hours),
    OneDay(24.hours),
    Never(Duration.INFINITE);

    companion object {

        fun default() = Never

        fun fromMinutes(minutes: Long): AutoLockInterval =
            AutoLockInterval.entries.firstOrNull { it.duration.inWholeMinutes == minutes } ?: Immediately
    }
}

enum class Protection {
    Pin, Biometrics, None;

    companion object {

        fun default() = None
    }
}


