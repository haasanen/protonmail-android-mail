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
import androidx.annotation.VisibleForTesting
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
import timber.log.Timber

class AutoLockInitializer : Initializer<Unit>, LifecycleEventObserver {

    @VisibleForTesting
    internal var autoLockCheckPendingState: AutoLockCheckPendingState? = null

    @VisibleForTesting
    internal var startAutoLockCountdown: StartAutoLockCountdown? = null

    private var stoppedSinceLastResume = false
    private var firstResume = true

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
            Lifecycle.Event.ON_RESUME -> {
                val shouldTrigger = firstResume || stoppedSinceLastResume
                firstResume = false
                stoppedSinceLastResume = false

                if (shouldTrigger) {
                    Timber.d("auto-lock: Triggering auto lock check after resume")
                    autoLockCheckPendingState?.triggerAutoLockCheck()
                } else {
                    Timber.d("auto-lock: Skipping auto lock check after resume, no prior STOP")
                }
            }

            Lifecycle.Event.ON_STOP -> {
                stoppedSinceLastResume = true
                Timber.d("auto-lock: App moved to background, starting auto-lock countdown")

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
