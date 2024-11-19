package me.proton.android.core.auth.presentation.signup

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.Test

@RunWith(Parameterized::class)
class SignUpLoadingScreenTest(
    config: DeviceConfig
) {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = config)

    @Test
    fun signUpLoadingScreen() {
        paparazzi.snapshot {
            SignUpLoading()
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
