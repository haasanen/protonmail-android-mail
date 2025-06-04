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

@file:Suppress("unused")

package ch.protonmail.android.mailpinlock.presentation.autolock

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R

sealed class AutolockSettingsUiState {
    data class Data(val settings: AutolockSettings) : AutolockSettingsUiState()
    data object Loading : AutolockSettingsUiState()
}

data class AutolockSettings(
    val selectedUiInterval: TextUiModel,
    val protectionType: ProtectionType,
    val biometricsAvailable: Boolean
) {

    val isEnabled = protectionType != ProtectionType.None
    val biometricsEnabled = protectionType == ProtectionType.Biometrics
}

enum class ProtectionType {
    Pin, Biometrics, None
}

data class AutoLockSettingsEffects(
    val updateError: Effect<TextUiModel> = Effect.empty(),
    val forceOpenPinCreation: Effect<Unit> = Effect.empty(),
    val pinLockChangeRequested: Effect<Unit> = Effect.empty(),
    val autoLockBiometricsHwError: Effect<TextUiModel> = Effect.empty(),
    val autoLockBiometricsEnrollmentError: Effect<TextUiModel> = Effect.empty()
)

internal fun AutoLockSettingsEffects.onUpdateErrorEffect() =
    this.copy(updateError = Effect.of(TextUiModel(R.string.mail_settings_auto_lock_update_error)))

internal fun AutoLockSettingsEffects.onForceOpenPinCreation() = this.copy(forceOpenPinCreation = Effect.of(Unit))
internal fun AutoLockSettingsEffects.onPinLockChangeRequested() = this.copy(pinLockChangeRequested = Effect.of(Unit))

internal fun AutoLockSettingsEffects.onAutoLockBiometricsHwError() =
    this.copy(autoLockBiometricsHwError = Effect.of(TextUiModel(R.string.biometric_error_hw_not_available)))

internal fun AutoLockSettingsEffects.onAutoLockBiometricsEnrollmentError() =
    this.copy(autoLockBiometricsEnrollmentError = Effect.of(TextUiModel(R.string.no_biometric_data_enrolled)))
