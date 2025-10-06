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

package ch.protonmail.android.mailsettings.presentation.testdata

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.MobileSignaturePreference
import ch.protonmail.android.mailsettings.domain.model.MobileSignatureStatus
import ch.protonmail.android.mailsettings.presentation.settings.signature.model.MobileSignatureUiModel
import ch.protonmail.android.mailsettings.presentation.R

object MobileSignatureTestData {

    // Domain models
    val PreferenceEnabled = MobileSignaturePreference(
        value = "My signature",
        status = MobileSignatureStatus.Enabled
    )

    val PreferenceDisabled = MobileSignaturePreference(
        value = "",
        status = MobileSignatureStatus.Disabled
    )

    val PreferenceNeedsPaid = MobileSignaturePreference(
        value = "My signature",
        status = MobileSignatureStatus.NeedsPaidVersion
    )

    val PreferenceEmpty = MobileSignaturePreference.Empty

    // UI models ---
    val SignatureEnabled = MobileSignatureUiModel(
        signatureStatus = MobileSignatureStatus.Enabled,
        signatureValue = "Best regards,\nSerdar",
        statusText = TextUiModel(R.string.mail_settings_app_customization_mobile_signature_on)
    )

    val SignatureDisabled = MobileSignatureUiModel(
        signatureStatus = MobileSignatureStatus.Disabled,
        signatureValue = "",
        statusText = TextUiModel(R.string.mail_settings_app_customization_mobile_signature_off)
    )

    val SignatureNeedsPaid = MobileSignatureUiModel(
        signatureStatus = MobileSignatureStatus.NeedsPaidVersion,
        signatureValue = "",
        statusText = TextUiModel(R.string.mail_settings_app_customization_mobile_signature_on)
    )

    val SignatureEmpty = MobileSignatureUiModel(
        signatureStatus = MobileSignatureStatus.Enabled,
        signatureValue = "",
        statusText = TextUiModel(R.string.mail_settings_app_customization_mobile_signature_on)
    )
}
