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

package ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.mapper

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.MobileSignaturePreference
import ch.protonmail.android.mailsettings.domain.model.MobileSignatureStatus
import ch.protonmail.android.mailsettings.domain.model.isEnabled
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureSettingsUiModel
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureUiModel

internal object MobileSignatureUiModelMapper {

    fun toUiModel(signaturePreference: MobileSignaturePreference): MobileSignatureUiModel {
        return MobileSignatureUiModel(
            signatureStatus = signaturePreference.status,
            signatureValue = signaturePreference.value,
            statusText = getStatusText(signaturePreference.status)
        )
    }

    fun toSettingsUiModel(signaturePreference: MobileSignaturePreference): MobileSignatureSettingsUiModel =
        MobileSignatureSettingsUiModel(
            enabled = signaturePreference.status.isEnabled(),
            signatureValue = signaturePreference.value,
            editSignatureEffect = Effect.empty()
        )

    private fun getStatusText(status: MobileSignatureStatus): TextUiModel = when (status) {
        MobileSignatureStatus.Enabled,
        MobileSignatureStatus.NeedsPaidVersion -> TextUiModel.TextRes(
            R.string.mail_settings_app_customization_mobile_signature_on
        )

        MobileSignatureStatus.Disabled ->
            TextUiModel.TextRes(R.string.mail_settings_app_customization_mobile_signature_off)
    }
}
