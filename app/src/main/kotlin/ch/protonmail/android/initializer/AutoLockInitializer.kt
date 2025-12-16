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

package ch.protonmail.android.initializer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import ch.protonmail.android.mailpinlock.data.StartAutoLockCountdown
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class AutoLockInitializer : Initializer<Unit>, LifecycleEventObserver {

    private var autoLockCheckPendingState: AutoLockCheckPendingState? = null
    private var startAutoLockCountdown: StartAutoLockCountdown? = null

    override fun create(context: Context) {
        autoLockCheckPendingState = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AutoLockInitializerEntryPoint::class.java
        ).autoLockCheckPendingState()

        startAutoLockCountdown = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AutoLockInitializerEntryPoint::class.java
        ).startAutoLockCountdown()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> autoLockCheckPendingState?.triggerAutoLockCheck()
            Lifecycle.Event.ON_STOP -> {
                autoLockCheckPendingState?.clearSkip()
                startAutoLockCountdown?.invoke()
            }
            else -> Unit
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AutoLockInitializerEntryPoint {

        fun autoLockCheckPendingState(): AutoLockCheckPendingState

        fun startAutoLockCountdown(): StartAutoLockCountdown
    }
}
