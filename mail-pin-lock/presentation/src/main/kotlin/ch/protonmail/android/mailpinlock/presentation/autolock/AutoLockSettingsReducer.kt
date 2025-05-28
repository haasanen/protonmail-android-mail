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

package ch.protonmail.android.mailpinlock.presentation.autolock

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.AutoLockBiometricsEnrollmentError
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.AutoLockBiometricsHwError
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.ChangePinLockRequested
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.Data.Loaded
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.ForcePinCreation
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.Update.AutoLockBiometricsToggled
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.Update.AutoLockIntervalSet
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.Update.AutoLockIntervalsDropDownToggled
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.Update.AutoLockPreferenceEnabled
import ch.protonmail.android.mailpinlock.presentation.autolock.AutoLockSettingsEvent.UpdateError
import ch.protonmail.android.mailpinlock.presentation.autolock.mapper.AutoLockBiometricsUiModelMapper
import ch.protonmail.android.mailpinlock.presentation.autolock.mapper.AutoLockIntervalsUiModelMapper
import ch.protonmail.android.mailsettings.domain.model.autolock.AutoLockInterval
import javax.inject.Inject

class AutoLockSettingsReducer @Inject constructor(
    private val intervalsMapper: AutoLockIntervalsUiModelMapper,
    private val biometricsUiModelMapper: AutoLockBiometricsUiModelMapper
) {

    fun newStateFrom(currentState: AutoLockSettingsState, event: AutoLockSettingsEvent) =
        currentState.toNewStateFromEvent(event)

    @Suppress("ComplexMethod")
    private fun AutoLockSettingsState.toNewStateFromEvent(event: AutoLockSettingsEvent): AutoLockSettingsState {
        return when (this) {
            is AutoLockSettingsState.Loading -> when (event) {
                is Loaded -> event.toDataState()
                else -> this
            }

            is AutoLockSettingsState.DataLoaded -> when (event) {
                is Loaded -> event.toDataState()
                is ChangePinLockRequested -> propagatePinLockRequested()
                is ForcePinCreation -> triggerForcePinCreation()
                is AutoLockIntervalSet -> updateAutoLockInterval(event.newValue)
                is AutoLockPreferenceEnabled -> updateAutoLockEnabledToggle(event.newValue)
                is AutoLockIntervalsDropDownToggled ->
                    updateAutoLockIntervalsDropdown(event.newValue)

                is AutoLockBiometricsToggled ->
                    updateAutoLockBiometricsPreference(event.newValue)

                is AutoLockBiometricsHwError -> newStateForBiometricsHwError()
                is AutoLockBiometricsEnrollmentError -> newStateForBiometricsEnrollmentError()

                UpdateError -> triggerUpdateError()
            }
        }
    }

    private fun AutoLockSettingsState.DataLoaded.propagatePinLockRequested() =
        copy(pinLockChangeRequested = Effect.of(Unit))

    private fun AutoLockSettingsState.DataLoaded.triggerForcePinCreation() =
        copy(forceOpenPinCreation = Effect.of(Unit))

    private fun AutoLockSettingsState.DataLoaded.updateAutoLockInterval(
        value: AutoLockInterval
    ): AutoLockSettingsState.DataLoaded {
        val updatedSelectedUiModel = intervalsMapper.toSelectedIntervalUiModel(value)
        return copy(
            autoLockIntervalsState = AutoLockSettingsState.DataLoaded.AutoLockIntervalState(
                autoLockIntervalsState.autoLockIntervalsUiModel.copy(selectedInterval = updatedSelectedUiModel),
                dropdownExpanded = false
            )
        )
    }

    private fun AutoLockSettingsState.DataLoaded.updateAutoLockIntervalsDropdown(newValue: Boolean) =
        copy(autoLockIntervalsState = autoLockIntervalsState.copy(dropdownExpanded = newValue))

    private fun AutoLockSettingsState.DataLoaded.updateAutoLockBiometricsPreference(
        enabled: Boolean
    ): AutoLockSettingsState.DataLoaded = copy(
        autoLockBiometricsState = this.autoLockBiometricsState.copy(
            enabled = enabled
        )
    )

    private fun AutoLockSettingsState.DataLoaded.updateAutoLockEnabledToggle(
        value: Boolean
    ): AutoLockSettingsState.DataLoaded {
        val updatedUiModel = AutoLockEnabledUiModel(value)
        return copy(autoLockEnabledState = AutoLockSettingsState.DataLoaded.AutoLockEnabledState(updatedUiModel))
    }

    private fun AutoLockSettingsState.DataLoaded.newStateForBiometricsHwError() = copy(
        autoLockBiometricsState = autoLockBiometricsState.copy(
            biometricsHwError = Effect.of(TextUiModel(R.string.biometric_error_hw_not_available))
        )
    )

    private fun AutoLockSettingsState.DataLoaded.newStateForBiometricsEnrollmentError() = copy(
        autoLockBiometricsState = autoLockBiometricsState.copy(
            biometricsEnrollmentError = Effect.of(TextUiModel(R.string.no_biometric_data_enrolled))
        )
    )

    private fun AutoLockSettingsState.DataLoaded.triggerUpdateError() = copy(updateError = Effect.of(Unit))

    private fun Loaded.toDataState(): AutoLockSettingsState.DataLoaded {
        val biometricsStateUiModel = biometricsUiModelMapper.toUiModel(biometricsState)
        val autoLockEnabledUiModel = AutoLockEnabledUiModel(lockEnabled.isEnabled)
        val autoLockSelectedIntervalUiModel = intervalsMapper.toSelectedIntervalUiModel(selectedInterval)
        val autoLockIntervalsListUiModel = intervalsMapper.toIntervalsListUiModel()
        val autoLockIntervalsUiModel =
            AutoLockIntervalsUiModel(autoLockSelectedIntervalUiModel, autoLockIntervalsListUiModel)

        val autoLockEnabledState = AutoLockSettingsState.DataLoaded.AutoLockEnabledState(autoLockEnabledUiModel)
        val autoLockIntervalsState = AutoLockSettingsState.DataLoaded.AutoLockIntervalState(
            autoLockIntervalsUiModel,
            dropDownMenuVisible
        )

        return AutoLockSettingsState.DataLoaded(
            autoLockEnabledState = autoLockEnabledState,
            autoLockIntervalsState = autoLockIntervalsState,
            autoLockBiometricsState = biometricsStateUiModel,
            forceOpenPinCreation = Effect.empty(),
            pinLockChangeRequested = Effect.empty(),
            updateError = Effect.empty()
        )
    }
}
