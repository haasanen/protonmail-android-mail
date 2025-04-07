package me.proton.android.core.auth.presentation.signup

import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputState
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputViewModel
import me.proton.android.core.auth.presentation.signup.ui.CreatePasswordContent
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.Test

@RunWith(Parameterized::class)
class CreatePasswordStateScreenTest(
    config: DeviceConfig
) {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = config)

    @Test
    fun createPasswordScreen() {
        val fakeViewModelStore = mockk<ViewModelStore> {
            every { this@mockk[any()] } answers {
                if (firstArg<String>().endsWith(OneTimePasswordInputViewModel::class.java.name)) {
                    mockk<OneTimePasswordInputViewModel> {
                        every { state } returns MutableStateFlow(OneTimePasswordInputState.Idle)
                    }
                } else null
            }
        }
        val fakeViewModelStoreOwner = mockk<ViewModelStoreOwner> {
            every { viewModelStore } returns fakeViewModelStore
        }
        paparazzi.snapshot {
            CompositionLocalProvider(LocalViewModelStoreOwner provides fakeViewModelStoreOwner) {
                ProtonTheme {
                    CreatePasswordContent(
                        state = CreatePasswordState.Idle
                    )
                }
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
