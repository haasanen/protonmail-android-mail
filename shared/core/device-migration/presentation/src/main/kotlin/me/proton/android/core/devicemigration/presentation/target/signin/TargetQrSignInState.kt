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

package me.proton.android.core.devicemigration.presentation.target.signin

import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import me.proton.core.compose.effect.Effect

internal sealed class TargetQrSignInState(open val effect: Effect<TargetQrSignInEvent>? = null) {
    data object Loading : TargetQrSignInState()
    data class Idle(
        val errorMessage: String?,
        val qrCode: String,
        val generateBitmap: suspend (String, Dp) -> Bitmap
    ) : TargetQrSignInState()

    data class Failure(val message: String, val onRetry: (() -> Unit)?) : TargetQrSignInState()
    data class SuccessfullySignedIn(override val effect: Effect<TargetQrSignInEvent>) : TargetQrSignInState(effect)
}
