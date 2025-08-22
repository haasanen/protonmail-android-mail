package me.proton.android.core.auth.presentation.signup

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import me.proton.android.core.auth.presentation.signup.ui.SignUpLoadingScreen
import me.proton.core.account.domain.entity.AccountType
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.Test

@RunWith(Parameterized::class)
class SignUpLoadScreenTest(
    config: DeviceConfig
) {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = config)

    @Test
    fun signUpLoadingScreen() {
        paparazzi.snapshot {
            SignUpLoadingScreen(
                state = CreateUsernameState.Idle(
                    accountType = AccountType.Internal,
                    isLoading = false
                )
            )
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
