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

package me.proton.android.core.auth.presentation

import android.content.ClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

object LocalClipManager {

    private val LocalClipboardManager = compositionLocalOf<ClipboardManager?> { null }

    val current: ClipboardManager?
        @Composable
        get() = LocalClipboardManager.current
            ?: LocalContext.current.applicationContext.getSystemService(ClipboardManager::class.java)

    @Composable
    fun ClipboardManager.OnClipChangedDisposableEffect(block: (String) -> Unit) {
        DisposableEffect(this) {
            val listener = ClipboardManager.OnPrimaryClipChangedListener {
                block(this@OnClipChangedDisposableEffect.text?.toString() ?: "")
            }
            addPrimaryClipChangedListener(listener)
            onDispose { removePrimaryClipChangedListener(listener) }
        }
    }
}
