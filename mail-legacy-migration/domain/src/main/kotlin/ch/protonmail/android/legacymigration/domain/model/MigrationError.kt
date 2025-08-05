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

package ch.protonmail.android.legacymigration.domain.model

sealed interface MigrationError {
    sealed interface LegacyDbFailure {
        data object MissingPrimaryUserId : MigrationError
        data object MissingAccount : MigrationError
        data object MissingSessionId : MigrationError
        data object MissingSession : MigrationError
        data object MissingUser : MigrationError
        data object MissingUserAddress : MigrationError

    }

    data object LoginFlowFailed : MigrationError
    data object Unknown : MigrationError

    sealed interface AutoLockFailure {
        data object FailedToReadAutoLockEnabled : MigrationError
        data object FailedToDecryptAutoLockEnabled : MigrationError

        data object EmptyBiometricPreferenceRead : MigrationError
        data object FailedToReadBiometricPreference : MigrationError
        data object FailedToDecryptBiometricPreference : MigrationError
        data object FailedToSetBiometricPreference : MigrationError

        data object FailedToReadAutoLockPin : MigrationError
        data object FailedToDecryptAutoLockPin : MigrationError
        data object FailedToSetAutoLockPin : MigrationError

        data object FailedToReadAutoLockInterval : MigrationError
        data object FailedToDecryptAutoLockInterval : MigrationError
        data object FailedToSetAutoLockInterval : MigrationError
    }

    sealed interface SignatureFailure {
        data object FailedToReadMobileSignaturePreference : MigrationError
        data object FailedToReadSignaturePreference : MigrationError

    }

    // Error during migration with specific known reason
    sealed interface MigrateFailed : MigrationError {
        data object ApiFailure : MigrateFailed
        data object InternalError : MigrateFailed
        data object AuthenticationFailure : MigrateFailed
        data object Unknown : MigrateFailed
    }
}
