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

package me.proton.android.core.devicemigration.presentation.origin.intro

import me.proton.core.compose.effect.Effect

internal data class OriginQrSignInStateHolder(
    val effect: Effect<OriginQrSignInEvent>? = null,
    val state: OriginQrSignInState
)

internal sealed interface OriginQrSignInState {
    data object Idle : OriginQrSignInState
    data object Loading : OriginQrSignInState
    data object Verifying : OriginQrSignInState
    data object SignedInSuccessfully : OriginQrSignInState
    data class MissingCameraPermission(val productName: String) : OriginQrSignInState
}

internal fun OriginQrSignInState.shouldDisableInteraction(): Boolean =
    this is OriginQrSignInState.Loading || this is OriginQrSignInState.SignedInSuccessfully
