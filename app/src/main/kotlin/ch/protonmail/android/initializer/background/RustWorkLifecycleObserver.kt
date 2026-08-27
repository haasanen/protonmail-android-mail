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
import ch.protonmail.android.mailsession.data.background.BackgroundExecutionWorkScheduler
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RustWorkLifecycleObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mailSessionRepository: MailSessionRepository,
    private val backgroundExecutionWorkScheduler: BackgroundExecutionWorkScheduler
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        startMailSyncService()
        owner.lifecycleScope.launch {
            backgroundExecutionWorkScheduler.cancelPendingWork()
            onRustEnterForeground()
            Timber.d("onStart finished - pending work canceled + onEnterForeground")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundExecutionWorkScheduler.scheduleWork()
        onRustExitForeground()
        Timber.d("onStop finished - schedule work called + onExitForeground")
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
            // (app in background). The 30-minute work schedule is the fallback.
            Timber.w(e, "Failed to start mail sync service")
        }
    }
}
