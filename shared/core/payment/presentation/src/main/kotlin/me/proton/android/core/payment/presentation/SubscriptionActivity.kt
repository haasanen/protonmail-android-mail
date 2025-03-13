/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.payment.presentation

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import ch.protonmail.android.design.compose.theme.ProtonTheme
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.payment.domain.IconResourceManager
import me.proton.android.core.payment.presentation.screen.SubscriptionScreen
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionActivity : ProtonActivity() {

    @Inject
    lateinit var iconResourceManager: IconResourceManager

    private var success = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Coil.setImageLoader(
            ImageLoader.Builder(applicationContext)
                .components {
                    add(IconResourceKeyer())
                    add(IconResourceFetcher.Factory(applicationContext, iconResourceManager))
                }
                .crossfade(true)
                .build()
        )

        addOnBackPressedCallback { onClose() }

        setContent {
            ProtonTheme {
                SubscriptionScreen(
                    onClose = { onClose() },
                    onSuccess = { success = true },
                    onErrorMessage = { onError(it) }
                )
            }
        }
    }

    private fun onError(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun onSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onClose() {
        if (success) onSuccess() else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
