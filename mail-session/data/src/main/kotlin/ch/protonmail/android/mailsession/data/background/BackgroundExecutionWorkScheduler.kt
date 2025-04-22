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

package ch.protonmail.android.mailsession.data.background

import androidx.work.ExistingPeriodicWorkPolicy
import ch.protonmail.android.mailcommon.data.worker.CancelWorkManagerWork
import ch.protonmail.android.mailcommon.data.worker.Enqueuer
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class BackgroundExecutionWorkScheduler @Inject constructor(
    private val enqueuer: Enqueuer,
    private val cancelWorkManagerWork: CancelWorkManagerWork
) {

    fun scheduleWork() {
        enqueuer.enqueueUniquePeriodicWork(
            workerId = WORKER_ID,
            tag = BACKGROUND_WORK_TAG,
            worker = BackgroundExecutionWorker::class.java,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            initialDelay = 30.seconds.toJavaDuration()
        )
    }

    fun cancelPendingWork() {
        cancelWorkManagerWork.cancelAllWorkByTag(BACKGROUND_WORK_TAG)
    }

    private companion object {

        const val BACKGROUND_WORK_TAG = "background_work_execution"
        const val WORKER_ID = "background_work_execution_task"
    }
}
