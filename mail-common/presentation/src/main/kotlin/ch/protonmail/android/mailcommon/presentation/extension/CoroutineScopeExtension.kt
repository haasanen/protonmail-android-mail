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

package ch.protonmail.android.mailcommon.presentation.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/**
 * Allows launching a job while executing side suspending logic if the original operation
 * takes longer than the provided duration threshold.
 *
 * @param durationThreshold the duration value
 * @param onThresholdExceeded the logic to run upon reaching the [durationThreshold]
 * @param onComplete any clean up logic required after the original job has completed
 * @param block the original suspending logic
 */
@SuppressWarnings("TooGenericExceptionCaught")
fun <T> CoroutineScope.launchWithDelayedCallback(
    durationThreshold: Long = 1000,
    onThresholdExceeded: suspend () -> Unit,
    onComplete: suspend () -> Unit = {},
    block: suspend () -> T
): Job {
    return launch {
        val job = async { block() }

        val checkForThresholdJob = launch {
            delay(durationThreshold)
            if (job.isActive) {
                onThresholdExceeded()
            }
        }

        try {
            job.await()
        } catch (e: Throwable) {
            // Don't rethrow cancellation exceptions if the parent is cancelling
            if (e !is CancellationException) {
                throw e
            }
        } finally {
            checkForThresholdJob.cancel()
            try {
                onComplete()
            } catch (e: Throwable) {
                // Catch the exception here to avoid shadowing the original exception thrown
                // from the block above.
                Timber.e("Exception thrown via onComplete $e")
            }
        }
    }
}
