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

package ch.protonmail.android.mailpinlock.presentation.autolock.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.AutoLockBiometricsState.BiometricsAvailable
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettings
import ch.protonmail.android.mailpinlock.presentation.autolock.model.ProtectionType

internal object AutoLockSettingsUiMapper {

    fun toUiModel(autoLock: AutoLock) = AutoLockSettings(
        selectedUiInterval = autoLock.autolockInterval.toTextUiModel(),
        protectionType = autoLock.protectionType.toProtectionTypeUiModel(),
        biometricsAvailable = autoLock.biometricsState is BiometricsAvailable.BiometricsEnrolled
    )

    private fun Protection.toProtectionTypeUiModel() = when (this) {
        Protection.None -> ProtectionType.None
        Protection.Biometrics -> ProtectionType.Biometrics
        Protection.Pin -> ProtectionType.Pin
    }

    internal fun AutoLockInterval.toTextUiModel(): TextUiModel {
        val textRes = when (this) {
            AutoLockInterval.Immediately -> R.string.mail_pinlock_settings_autolock_immediately
            AutoLockInterval.FiveMinutes -> R.string.mail_pinlock_settings_autolock_description_five_minutes
            AutoLockInterval.FifteenMinutes -> R.string.mail_pinlock_settings_autolock_description_fifteen_minutes
            AutoLockInterval.OneHour -> R.string.mail_pinlock_settings_autolock_description_one_hour
            AutoLockInterval.OneDay -> R.string.mail_pinlock_settings_autolock_description_one_day
            AutoLockInterval.Never -> R.string.mail_pinlock_settings_autolock_never
        }
        return TextUiModel.TextRes(textRes)
    }
}
