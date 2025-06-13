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

package ch.protonmail.android.mailpinlock.presentation.autolock.reducer

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsEffects
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsEvent
import javax.inject.Inject

class AutoLockSettingsReducer @Inject constructor() {

    internal fun newStateFrom(currentEffects: AutoLockSettingsEffects, event: AutoLockSettingsEvent) = when (event) {
        is AutoLockSettingsEvent.BiometricAuthRequested ->
            currentEffects.copy(requestBiometricsAuth = Effect.of(event.followUp))

        is AutoLockSettingsEvent.PinChangeRequested -> currentEffects.copy(pinLockChangeRequested = Effect.of(Unit))
        is AutoLockSettingsEvent.PinCreationRequested -> currentEffects.copy(openPinCreation = Effect.of(Unit))
        is AutoLockSettingsEvent.PinRemovalRequested -> currentEffects.copy(pinLockRemovalRequested = Effect.of(Unit))
        is AutoLockSettingsEvent.Error -> reduceError(currentEffects, event)
    }

    private fun reduceError(
        currentEffects: AutoLockSettingsEffects,
        error: AutoLockSettingsEvent.Error
    ): AutoLockSettingsEffects {
        val error = when (error) {
            AutoLockSettingsEvent.Error.BiometricsSetError -> R.string.mail_settings_biometrics_unable_to_set
            AutoLockSettingsEvent.Error.BiometricsUnsetError -> R.string.mail_settings_biometrics_unable_to_unset
            AutoLockSettingsEvent.Error.UnknownLockPolicy -> R.string.mail_settings_biometrics_unknown_lock
        }

        return currentEffects.copy(updateError = Effect.of(TextUiModel(error)))
    }
}
