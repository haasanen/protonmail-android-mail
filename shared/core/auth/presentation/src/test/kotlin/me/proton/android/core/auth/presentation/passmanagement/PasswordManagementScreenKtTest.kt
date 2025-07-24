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

package me.proton.android.core.auth.presentation.passmanagement

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.Test

@RunWith(Parameterized::class)
class PasswordManagementScreenKtTest(config: DeviceConfig) {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = config)

    @Test
    fun `loading state`() {
        paparazzi.snapshot {
            ProtonTheme {
                PasswordManagementScreen(
                    state = PasswordManagementState.Loading,
                    onAction = {},
                    onBackClicked = {}
                )
            }
        }
    }

    @Test
    fun `user input login pass only`() {
        paparazzi.snapshot {
            ProtonTheme {
                PasswordManagementScreen(
                    state = PasswordManagementState.UserInput(),
                    onAction = {},
                    onBackClicked = {}
                )
            }
        }
    }

    @Test
    fun `user input login and second pass`() {
        paparazzi.snapshot {
            ProtonTheme {
                PasswordManagementScreen(
                    state = PasswordManagementState.UserInput(
                        mailboxPassword = MailboxPasswordState(isAvailable = true)
                    ),
                    onAction = {},
                    onBackClicked = {}
                )
            }
        }
    }

    companion object {

        @Parameters
        @JvmStatic
        fun configurations() = listOf(
            DeviceConfig.PIXEL_6,
            DeviceConfig.PIXEL_6.copy(nightMode = NightMode.NIGHT),
            DeviceConfig.PIXEL_6.copy(orientation = ScreenOrientation.LANDSCAPE)
        )
    }
}
