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

package ch.protonmail.android.initializer.background

import android.content.Context
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailsession.data.background.BackgroundExecutionWorkScheduler
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsettings.domain.model.BackgroundSyncInterval
import ch.protonmail.android.mailsettings.domain.usecase.privacy.ObserveBackgroundSyncInterval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RustWorkLifecycleObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mailSessionRepository: MailSessionRepository,
    private val backgroundExecutionWorkScheduler: BackgroundExecutionWorkScheduler,
    observeBackgroundSyncInterval: ObserveBackgroundSyncInterval,
    @AppScope private val appScope: CoroutineScope
) : DefaultLifecycleObserver {

    private val backgroundSyncInterval: StateFlow<BackgroundSyncInterval> =
        observeBackgroundSyncInterval()
            .map { it.getOrElse { BackgroundSyncInterval.REAL_TIME } }
            .stateIn(appScope, SharingStarted.Eagerly, BackgroundSyncInterval.REAL_TIME)

    init {
        appScope.launch {
            backgroundSyncInterval.collect { applyForegroundServiceState(it) }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        applyForegroundServiceState(backgroundSyncInterval.value)
        owner.lifecycleScope.launch {
            backgroundExecutionWorkScheduler.cancelPendingWork()
            onRustEnterForeground()
            Timber.d("onStart finished - pending work canceled + onEnterForeground")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        appScope.launch { applyBackgroundSyncIntervalInBackground() }
        onRustExitForeground()
        Timber.d("onStop finished - background sync interval applied + onExitForeground")
    }

    private suspend fun applyBackgroundSyncIntervalInBackground() {
        when (val interval = backgroundSyncInterval.value) {
            BackgroundSyncInterval.NEVER -> {
                backgroundExecutionWorkScheduler.cancelPendingWork()
                Timber.d("Background sync disabled by user; canceling pending work")
            }

            BackgroundSyncInterval.REAL_TIME -> {
                // Stock 30-minute safety net; the foreground service keeps the stream live.
                backgroundExecutionWorkScheduler.scheduleWork()
            }

            else -> {
                backgroundExecutionWorkScheduler.scheduleWork(
                    interval.intervalMinutes() ?: 30L
                )
            }
        }
    }

    private fun applyForegroundServiceState(interval: BackgroundSyncInterval) {
        if (interval.isRealTime) {
            startMailSyncService()
        } else {
            context.stopService(Intent(context, MailSyncForegroundService::class.java))
        }
    }

    private fun onRustExitForeground() {
        mailSessionRepository.getMailSession().onExitForeground()
    }

    private fun onRustEnterForeground() {
        mailSessionRepository.getMailSession().onEnterForeground()
    }

    private fun startMailSyncService() {
        try {
            if (!mailSessionRepository.isMailSessionInitialised()) {
                return
            }
            context.startForegroundService(Intent(context, MailSyncForegroundService::class.java))
        } catch (e: Exception) {
            // Session may not be initialised yet (lateinit) or start may be rejected
            // (app in background). The scheduled work is the fallback.
            Timber.w(e, "Failed to start mail sync service")
        }
    }
}
