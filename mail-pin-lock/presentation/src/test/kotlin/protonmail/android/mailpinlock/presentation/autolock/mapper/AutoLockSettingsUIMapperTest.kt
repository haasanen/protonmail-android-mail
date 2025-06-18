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

package protonmail.android.mailpinlock.presentation.autolock.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.mapper.AutoLockSettingsUiMapper
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettings
import ch.protonmail.android.mailpinlock.presentation.autolock.model.ProtectionType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class AutoLockSettingsUiMapperTest(
    @Suppress("unused") private val testName: String,
    private val toMap: AutoLock,
    private val expectedUiModel: AutoLockSettings
) {

    @Test
    fun `should map to ui model`() {
        val uiModel = AutoLockSettingsUiMapper.toUiModel(toMap)
        assertEquals(expectedUiModel, uiModel)
    }

    companion object {

        val baseAutoLock = AutoLock()

        val baseUiModel = AutoLockSettings(
            TextUiModel(R.string.mail_pinlock_settings_autolock_immediately),
            protectionType = ProtectionType.None,
            biometricsAvailable = false
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "to autolock with default values",
                baseAutoLock,
                baseUiModel
            ),
            arrayOf(
                "to autolock with pin",
                baseAutoLock.copy(protectionType = Protection.Pin),
                baseUiModel.copy(protectionType = ProtectionType.Pin)
            ),
            arrayOf(
                "to autolock with biometrics",
                baseAutoLock.copy(protectionType = Protection.Biometrics),
                baseUiModel.copy(protectionType = ProtectionType.Biometrics)
            ),
            arrayOf(
                "to autolock with never",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.Never),
                baseUiModel.copy(selectedUiInterval = TextUiModel(R.string.mail_pinlock_settings_autolock_never))
            ),
            arrayOf(
                "to autolock with biometrics Immediately",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.Immediately),
                baseUiModel.copy(selectedUiInterval = TextUiModel(R.string.mail_pinlock_settings_autolock_immediately))
            ),
            arrayOf(
                "to autolock with biometrics One Minute",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.OneMinute),
                baseUiModel.copy(
                    selectedUiInterval = TextUiModel(R.string.mail_pinlock_settings_autolock_description_one_minute)
                )
            ),
            arrayOf(
                "to autolock with biometrics Two Minutes",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.TwoMinutes),
                baseUiModel.copy(
                    selectedUiInterval = TextUiModel(R.string.mail_pinlock_settings_autolock_description_two_minutes)
                )
            ),
            arrayOf(
                "to autolock with biometrics Five Minutes",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.FiveMinutes),
                baseUiModel.copy(
                    selectedUiInterval = TextUiModel(R.string.mail_pinlock_settings_autolock_description_five_minutes)
                )
            ),
            arrayOf(
                "to autolock with biometrics Ten Minutes",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.TenMinutes),
                baseUiModel.copy(
                    selectedUiInterval = TextUiModel(R.string.mail_pinlock_settings_autolock_description_ten_minutes)
                )
            ),
            arrayOf(
                "to autolock with biometrics FifteenMinutes",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.FifteenMinutes),
                baseUiModel.copy(
                    selectedUiInterval = TextUiModel(
                        R.string.mail_pinlock_settings_autolock_description_fifteen_minutes
                    )
                )
            ),
            arrayOf(
                "to autolock with biometrics T",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.ThirtyMinutes),
                baseUiModel.copy(
                    selectedUiInterval = TextUiModel(
                        R.string.mail_pinlock_settings_autolock_description_thirty_minutes
                    )
                )
            ),
            arrayOf(
                "to autolock with biometrics One Hour",
                baseAutoLock.copy(autolockInterval = AutoLockInterval.SixtyMinutes),
                baseUiModel.copy(
                    selectedUiInterval = TextUiModel(
                        R.string.mail_pinlock_settings_autolock_description_sixty_minutes
                    )
                )
            )
        )
    }
}
