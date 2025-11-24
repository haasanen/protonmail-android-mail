/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import ch.protonmail.android.mailmailbox.domain.model.MailboxFetchNewStatus
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.LoadingBarUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.ceil
import kotlin.math.max

/**
 * MailboxLoadingBarStateController
 *
 * This class processes "fetch new" events (Start/End) and controls
 * when the animated cycling loading bar should be shown or hidden.
 *
 * Goal:
 *  - The loading bar should never disappear in the middle of an animation cycle.
 *  - It should always complete full cycles (e.g. 2 seconds each), so the hide
 *    moment feels intentional and smooth.
 *
 *      Example (cycleDuration = 2000ms):
 *        • elapsed = 1500ms → baseCycles = 1 → total 2s
 *        • elapsed = 2100ms → baseCycles = 2 → total 4s
 *        • elapsed = 3000ms → baseCycles = 2 → total 4s
 *
 */

class MailboxLoadingBarStateController(
    private val scope: CoroutineScope
) {

    private val mutableStateFlow =
        MutableStateFlow<LoadingBarUiState>(LoadingBarUiState.Hide)

    private var isActive: Boolean = false
    private var startTimeMs: Long? = null
    private var pendingStopJob: Job? = null

    fun onMailboxFetchNewStatus(event: MailboxFetchNewStatus) {
        Timber.d("onMailboxFetchNewStatus -> $event")
        when (event) {
            is MailboxFetchNewStatus.Started -> handleFetchStarted(event)
            is MailboxFetchNewStatus.Ended -> handleFetchEnded(event)
        }
    }

    fun observeState(): Flow<LoadingBarUiState> = mutableStateFlow.asStateFlow()

    private fun handleFetchStarted(event: MailboxFetchNewStatus.Started) {
        if (isActive) {
            Timber.d("Received Started event while already active. Ignoring")
            return
        }

        // First Start of a new session
        isActive = true
        startTimeMs = event.timestampMs

        // Cancel any old hide job
        pendingStopJob?.cancel()
        pendingStopJob = null

        // Show the bar with the configured cycle duration
        mutableStateFlow.value = LoadingBarUiState.Show(
            cycleDurationMs = DEFAULT_CYCLE_MS
        )
    }

    private fun handleFetchEnded(event: MailboxFetchNewStatus.Ended) {
        if (!isActive) {
            Timber.d("Received Ended event while not active. Ignoring")
            return
        }

        val start = startTimeMs ?: run {
            // No start recorded – reset defensively
            isActive = false
            mutableStateFlow.value = LoadingBarUiState.Hide
            return
        }

        isActive = false

        val elapsed = (event.timestampMs - start).coerceAtLeast(0L)

        // Base number of cycles (at least 1)
        val baseCycles = max(
            1,
            ceil(elapsed.toDouble() / DEFAULT_CYCLE_MS).toInt()
        )

        // Remainder within the current cycle
        val remainder = elapsed % DEFAULT_CYCLE_MS
        val nearBoundary =
            elapsed >= DEFAULT_CYCLE_MS &&
                DEFAULT_CYCLE_MS - remainder < DEFAULT_TOLERANCE_MS

        val requiredCycles = baseCycles + if (nearBoundary) 1 else 0
        val targetEndTimeMs = start + requiredCycles * DEFAULT_CYCLE_MS

        pendingStopJob?.cancel()
        pendingStopJob = scope.launch {
            val delayMs = (targetEndTimeMs - event.timestampMs).coerceAtLeast(0L)
            if (delayMs > 0) {
                delay(delayMs)
            }
            mutableStateFlow.value = LoadingBarUiState.Hide
        }
    }

    companion object Companion {

        const val DEFAULT_CYCLE_MS: Long = 2_000L
        const val DEFAULT_TOLERANCE_MS: Long = 50L
    }
}
