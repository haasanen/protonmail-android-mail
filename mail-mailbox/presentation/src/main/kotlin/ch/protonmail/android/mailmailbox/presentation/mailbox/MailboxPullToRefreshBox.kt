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

import android.os.SystemClock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import ch.protonmail.android.design.compose.component.ProtonPullToRefreshIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailboxPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    content: @Composable () -> Unit
) {
    val isUiRefreshing = rememberWithMinRefreshingDuration(isRefreshing)

    // Haptic when refresh starts
    val haptics = LocalHapticFeedback.current

    // Fire haptic only once per refresh cycle, and persist that across rotation.
    var didHapticForThisCycle by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isUiRefreshing) {
        if (isUiRefreshing && !didHapticForThisCycle) {
            withContext(Dispatchers.IO) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            didHapticForThisCycle = true
        }
        if (!isUiRefreshing) {
            didHapticForThisCycle = false
        }
    }

    PullToRefreshBox(
        modifier = modifier,
        state = state,
        isRefreshing = isUiRefreshing,
        onRefresh = onRefresh,
        indicator = {
            ProtonPullToRefreshIndicator(
                state = state,
                isRefreshing = isUiRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        content()
    }
}

/**
 * Turns Refreshing TRUE on immediately,
 * but delays FALSE until [minRefreshingDurationMs] has elapsed inorder to keep the refresh
 * spinner on screen for a minimum amount of time.
 * */
@Composable
fun rememberWithMinRefreshingDuration(
    input: Boolean,
    minRefreshingDurationMs: Long = MIN_REFRESHING_DURATION_MS
): Boolean {
    var isUiRefreshing by rememberSaveable { mutableStateOf(false) }
    var refreshingStartedAtMs by rememberSaveable { mutableLongStateOf(0L) }

    LaunchedEffect(input) {
        if (input) {
            if (!isUiRefreshing) {
                isUiRefreshing = true
                refreshingStartedAtMs = SystemClock.elapsedRealtime()
            }
        } else {
            if (isUiRefreshing) {
                val elapsed = SystemClock.elapsedRealtime() - refreshingStartedAtMs
                val remaining = minRefreshingDurationMs - elapsed
                if (remaining > 0) delay(remaining)
            }
            isUiRefreshing = false
        }
    }
    return isUiRefreshing
}

private const val MIN_REFRESHING_DURATION_MS = 1_500L
