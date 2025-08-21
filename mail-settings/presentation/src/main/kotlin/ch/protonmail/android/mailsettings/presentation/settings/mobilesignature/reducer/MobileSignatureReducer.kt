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

package ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.reducer

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureEvent
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureOperation
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureState
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureViewAction
import javax.inject.Inject

class MobileSignatureReducer @Inject constructor() {

    fun newStateFrom(current: MobileSignatureState, operation: MobileSignatureOperation): MobileSignatureState =
        when (operation) {

            is MobileSignatureEvent.SignatureLoaded ->
                MobileSignatureState.Data(operation.signatureSettingsUiModel)

            is MobileSignatureViewAction.EditSignatureValue ->
                when (current) {
                    is MobileSignatureState.Data ->
                        current.copy(settings = current.settings.copy(editSignatureEffect = Effect.of(Unit)))

                    MobileSignatureState.Loading ->
                        current
                }

            is MobileSignatureViewAction.ToggleSignatureEnabled,
            is MobileSignatureViewAction.UpdateSignatureValue -> current

        }
}
