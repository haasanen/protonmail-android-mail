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

sealed interface AutoLockSettingsOperation

sealed interface AutoLockSettingsViewAction : AutoLockSettingsOperation {

    data object RequestProtectionRemoval : AutoLockSettingsViewAction
    data object RequestPinProtection : AutoLockSettingsViewAction
    data object RequestPinProtectionChange : AutoLockSettingsViewAction
    data object RequestBiometricsProtection : AutoLockSettingsViewAction

    data object MigrateFromPinToBiometrics : AutoLockSettingsViewAction

    data object SetPinPreference : AutoLockSettingsViewAction
    data object SetBiometricsPreference : AutoLockSettingsViewAction
    data object RemoveBiometricsProtection : AutoLockSettingsViewAction
}

sealed interface AutoLockSettingsEvent : AutoLockSettingsOperation {

    data object PinCreationRequested : AutoLockSettingsEvent
    data object PinChangeRequested : AutoLockSettingsEvent
    data object PinRemovalRequested : AutoLockSettingsEvent
    data object PinMigrationToBiometrics : AutoLockSettingsEvent
    data class BiometricAuthRequested(val followUp: BiometricsOperationFollowUp) : AutoLockSettingsEvent

    sealed interface Error : AutoLockSettingsEvent {
        data object BiometricsSetError : Error
        data object BiometricsUnsetError : Error
        data object UnknownLockPolicy : Error
    }
}
