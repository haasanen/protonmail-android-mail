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

package me.proton.android.core.devicemigration.presentation.origin.intro

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.displayName
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.BeforeTest

@RunWith(Parameterized::class)
class OriginQrSignInScreenTest(deviceConfig: DeviceConfig) {
    @MockK(relaxed = true)
    private lateinit var activity: Activity

    @MockK(relaxed = true)
    private lateinit var activityResultRegistryOwner: ActivityResultRegistryOwner

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = deviceConfig)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `idle state`() {
        paparazzi.snapshot {
            UiTestCase {
                OriginQrSignInScreen(
                    state = OriginQrSignInState.Idle,
                    effect = null
                )
            }
        }
    }

    @Test
    fun `missing permission state`() {
        paparazzi.snapshot {
            UiTestCase {
                OriginQrSignInScreen(
                    state = OriginQrSignInState.MissingCameraPermission(Product.Mail.displayName()),
                    effect = null
                )
            }
        }
    }

    @Test
    fun `verifying state`() {
        paparazzi.snapshot {
            UiTestCase {
                OriginQrSignInScreen(
                    state = OriginQrSignInState.Verifying,
                    effect = null
                )
            }
        }
    }

    @Composable
    private fun UiTestCase(content: @Composable () -> Unit) {
        ProtonTheme {
            CompositionLocalProvider(
                LocalActivity provides activity,
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
                LocalInspectionMode provides true
            ) {
                content()
            }
        }
    }

    companion object {
        @Parameters
        @JvmStatic
        fun parameters() = listOf(
            DeviceConfig.PIXEL_5,
            DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NIGHT)
        )
    }
}
