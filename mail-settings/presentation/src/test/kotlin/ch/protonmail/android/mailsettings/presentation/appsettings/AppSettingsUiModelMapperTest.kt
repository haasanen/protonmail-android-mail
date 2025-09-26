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

package ch.protonmail.android.mailsettings.presentation.appsettings

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.testdata.AppSettingsTestData
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class AppSettingsUiModelMapperTest(
    @Suppress("unused") private val testName: String,
    private val args: Arguments,
    private val expectedUiModel: AppSettingsUiModel
) {

    @Test
    fun `should map to ui model`() {
        val uiModel = AppSettingsUiModelMapper.toUiModel(args.appSettings, args.notificationsEnabled)
        assertEquals(expectedUiModel, uiModel)
    }

    companion object {

        val baseAppSettings = AppSettingsTestData.appSettings

        val baseUiModel = AppSettingsUiModel(
            autoLockEnabled = false,
            alternativeRoutingEnabled = true,
            customLanguage = null,
            deviceContactsEnabled = true,
            theme = TextUiModel.TextRes(R.string.mail_settings_system_default),
            notificationsEnabledStatus = TextUiModel(R.string.notifications_on)
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "to app settings with default values",
                Arguments(
                    baseAppSettings,
                    true
                ),
                baseUiModel
            ),
            arrayOf(
                "to app settings with light theme",
                Arguments(
                    baseAppSettings.copy(theme = Theme.LIGHT),
                    true
                ),
                baseUiModel.copy(theme = TextUiModel.TextRes(R.string.mail_settings_theme_light))
            ),
            arrayOf(
                "to app settings with dark theme",
                Arguments(
                    baseAppSettings.copy(theme = Theme.DARK),
                    true
                ),
                baseUiModel.copy(theme = TextUiModel.TextRes(R.string.mail_settings_theme_dark))
            ),
            arrayOf(
                "to app settings with custom language",
                Arguments(
                    baseAppSettings.copy(customAppLanguage = "Custom language"),
                    true
                ),
                baseUiModel.copy(customLanguage = "Custom language")
            ),
            arrayOf(
                "to app settings with pin lock enabled",
                Arguments(
                    baseAppSettings.copy(autolockProtection = Protection.Pin),
                    true
                ),
                baseUiModel.copy(autoLockEnabled = true)
            ),
            arrayOf(
                "to app settings with biometrics lock enabled",
                Arguments(
                    baseAppSettings.copy(autolockProtection = Protection.Biometrics),
                    true
                ),
                baseUiModel.copy(autoLockEnabled = true)
            ),
            arrayOf(
                "to app settings with no lock enabled",
                Arguments(
                    baseAppSettings,
                    true
                ),
                baseUiModel.copy(autoLockEnabled = false)
            ),
            arrayOf(
                "to app settings with alternative routing enabled",
                Arguments(
                    baseAppSettings.copy(hasAlternativeRouting = true),
                    true
                ),
                baseUiModel.copy(alternativeRoutingEnabled = true)
            ),
            arrayOf(
                "to app settings with device contacts enabled",
                Arguments(
                    baseAppSettings.copy(hasCombinedContactsEnabled = true),
                    true
                ),
                baseUiModel.copy(deviceContactsEnabled = true)
            ),
            arrayOf(
                "to app settings with notifications off",
                Arguments(
                    baseAppSettings,
                    false
                ),
                baseUiModel.copy(notificationsEnabledStatus = TextUiModel(R.string.notifications_off))
            )
        )

        data class Arguments(val appSettings: AppSettings, val notificationsEnabled: Boolean)
    }
}
