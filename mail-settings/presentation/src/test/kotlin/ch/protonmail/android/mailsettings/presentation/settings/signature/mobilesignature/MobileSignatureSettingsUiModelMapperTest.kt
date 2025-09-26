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

package ch.protonmail.android.mailsettings.presentation.settings.signature.mobilesignature

import androidx.annotation.StringRes
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.MobileSignatureStatus
import ch.protonmail.android.mailsettings.presentation.settings.signature.mapper.MobileSignatureUiModelMapper
import ch.protonmail.android.mailsettings.presentation.testdata.MobileSignatureTestData
import ch.protonmail.android.mailsettings.presentation.R
import kotlin.test.assertEquals
import kotlin.test.fail
import org.junit.Test

class MobileSignatureSettingsUiModelMapperTest {

    @Test
    fun `toUiModel maps Enabled preference to UI with ON text`() {
        val pref = MobileSignatureTestData.PreferenceEnabled

        val uiModel = MobileSignatureUiModelMapper.toUiModel(pref)

        assertEquals(MobileSignatureStatus.Enabled, uiModel.signatureStatus)
        assertEquals(pref.value, uiModel.signatureValue)
        assertTextRes(uiModel.statusText, R.string.mail_settings_app_customization_mobile_signature_on)
    }

    @Test
    fun `toUiModel maps Disabled preference to UI with OFF text`() {
        val pref = MobileSignatureTestData.PreferenceDisabled

        val uiModel = MobileSignatureUiModelMapper.toUiModel(pref)

        assertEquals(MobileSignatureStatus.Disabled, uiModel.signatureStatus)
        assertEquals(pref.value, uiModel.signatureValue)
        assertTextRes(uiModel.statusText, R.string.mail_settings_app_customization_mobile_signature_off)
    }

    @Test
    fun `toUiModel maps NeedsPaidVersion preference to UI with ON text`() {
        val pref = MobileSignatureTestData.PreferenceNeedsPaid

        val uiModel = MobileSignatureUiModelMapper.toUiModel(pref)

        assertEquals(MobileSignatureStatus.NeedsPaidVersion, uiModel.signatureStatus)
        assertEquals(pref.value, uiModel.signatureValue)
        assertTextRes(uiModel.statusText, R.string.mail_settings_app_customization_mobile_signature_on)
    }

    @Test
    fun `toUiModel maps Empty preference to UI with ON text`() {
        val pref = MobileSignatureTestData.PreferenceEmpty

        val uiModel = MobileSignatureUiModelMapper.toUiModel(pref)

        assertEquals(MobileSignatureStatus.Enabled, uiModel.signatureStatus)
        assertEquals(pref.value, uiModel.signatureValue)
        assertTextRes(uiModel.statusText, R.string.mail_settings_app_customization_mobile_signature_on)
    }

    @Test
    fun `toSettingsUiModel maps Enabled to enabled true and preserves value`() {
        val pref = MobileSignatureTestData.PreferenceEnabled

        val settings = MobileSignatureUiModelMapper.toSettingsUiModel(pref)

        assertEquals(true, settings.enabled)
        assertEquals(pref.value, settings.signatureValue)
        assertEquals(Effect.empty(), settings.editSignatureEffect)
    }

    @Test
    fun `toSettingsUiModel maps Disabled to enabled false and preserves value`() {
        val pref = MobileSignatureTestData.PreferenceDisabled

        val settings = MobileSignatureUiModelMapper.toSettingsUiModel(pref)

        assertEquals(false, settings.enabled)
        assertEquals(pref.value, settings.signatureValue)
        assertEquals(Effect.empty(), settings.editSignatureEffect)
    }

    @Test
    fun `toSettingsUiModel maps NeedsPaidVersion to enabled false and preserves value`() {
        val pref = MobileSignatureTestData.PreferenceNeedsPaid

        val settings = MobileSignatureUiModelMapper.toSettingsUiModel(pref)

        assertEquals(false, settings.enabled)
        assertEquals(pref.value, settings.signatureValue)
        assertEquals(Effect.empty(), settings.editSignatureEffect)
    }

    @Test
    fun `toSettingsUiModel maps Empty to enabled true with empty value`() {
        val pref = MobileSignatureTestData.PreferenceEmpty

        val settings = MobileSignatureUiModelMapper.toSettingsUiModel(pref)

        assertEquals(true, settings.enabled)
        assertEquals("", settings.signatureValue)
        assertEquals(Effect.empty(), settings.editSignatureEffect)
    }

    private fun assertTextRes(text: TextUiModel, @StringRes expectedRes: Int) {
        when (text) {
            is TextUiModel.TextRes -> assertEquals(expectedRes, text.value)
            else -> fail("Expected TextUiModel.TextRes($expectedRes), was $text")
        }
    }
}
