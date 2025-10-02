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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.protonmail.android.mailsession.data.usecase.StartBackgroundExecution
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import uniffi.proton_mail_uniffi.BackgroundExecutionStatus

@HiltWorker
internal class BackgroundExecutionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val startBackgroundExecution: StartBackgroundExecution
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = triggerBackgroundExecution()

    private suspend fun triggerBackgroundExecution(): Result {
        Timber.tag("BackgroundExecutionWorker").d("Triggering background execution...")

        val result = startBackgroundExecution().first()

        return when (val status = result.status) {
            is BackgroundExecutionStatus.Failed -> {
                logger.d("Failed with error: ${status.v1}")
                Result.failure()
            }

            // Consider as a success if aborted in foreground/no logged in accounts.
            BackgroundExecutionStatus.AbortedInForeground,
            BackgroundExecutionStatus.SkippedNoActiveContexts -> Result.success()

            // Resolve the result based on pending work/unsent messages.
            // This is necessary as the worker might have never obtained proper connectivity,
            // so we need to force a couple of retries to make sure that it actually got network access.
            // See https://issuetracker.google.com/issues/445324855
            BackgroundExecutionStatus.AbortedInBackground,
            BackgroundExecutionStatus.Executed,
            BackgroundExecutionStatus.TimedOut -> resolveResult(result.hasUnsentMessages, result.hasPendingActions)
        }
    }


    private fun resolveResult(hasUnsentMessages: Boolean, hasPendingActions: Boolean): Result {
        val hasUncompletedWork = hasUnsentMessages || hasPendingActions

        if (!hasUncompletedWork) {
            return Result.success()
        }

        logger.d("Uncompleted work - unsentMessages: $hasUnsentMessages, pendingActions: $hasPendingActions")

        return if (runAttemptCount < RETRY_LIMIT) {
            logger.d("Work scheduled for retry - ${runAttemptCount + 1}/$RETRY_LIMIT")
            Result.retry()
        } else {
            logger.d("Not scheduling for retry - attempts threshold reached")
            Result.success()
        }
    }

    private companion object {

        const val RETRY_LIMIT = 3
        private val logger = Timber.tag("BackgroundExecutionWorker")
    }
}
