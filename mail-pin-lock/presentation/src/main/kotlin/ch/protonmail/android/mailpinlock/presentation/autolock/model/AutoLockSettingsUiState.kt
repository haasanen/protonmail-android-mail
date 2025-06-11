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

package ch.protonmail.android.mailpinlock.presentation.autolock.model

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel

sealed class AutoLockSettingsUiState {
    data class Data(val settings: AutoLockSettings) : AutoLockSettingsUiState()
    data object Loading : AutoLockSettingsUiState()
}

data class AutoLockSettings(
    val selectedUiInterval: TextUiModel,
    val protectionType: ProtectionType,
    val biometricsAvailable: Boolean
) {

    val isEnabled = protectionType != ProtectionType.None
}

enum class ProtectionType {
    Pin, Biometrics, None
}

data class AutoLockSettingsEffects(
    val updateError: Effect<TextUiModel> = Effect.empty(),
    val openPinCreation: Effect<Unit> = Effect.empty(),
    val requestBiometricsAuth: Effect<BiometricsOperationFollowUp> = Effect.empty(),
    val pinLockChangeRequested: Effect<Unit> = Effect.empty(),
    val pinLockRemovalRequested: Effect<Unit> = Effect.empty(),
    val pinLockToBiometricsRequested: Effect<Unit> = Effect.empty()
)

enum class BiometricsOperationFollowUp { SetNone, SetPin, SetBiometrics, RemovePinAndSetBiometrics }
