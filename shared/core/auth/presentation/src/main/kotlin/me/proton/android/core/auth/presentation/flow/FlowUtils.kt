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

package me.proton.android.core.auth.presentation.flow

import kotlinx.coroutines.delay
import kotlin.random.Random

const val SIGNUP_MAX_RETRIES: Int = 5
const val SIGNUP_INITIAL_DELAY_MS: Long = 500L
const val SIGNUP_MAX_DELAY_MS: Long = 5_000L
const val JITTER_FACTOR = 0.5
const val RANDOM = 0.5
const val MULTIPLIER = 2

/**
 * Runs the given [block] with exponential backoff.
 *
 * @param maxRetries Maximum number of retry attempts.
 * @param initialDelayMs Initial delay before the first retry.
 * @param maxDelayMs Maximum delay between retries.
 * @param shouldRetry Predicate to determine if an error is retryable.
 */
suspend fun <T> runWithExponentialBackoffResult(
    maxRetries: Int = SIGNUP_MAX_RETRIES,
    initialDelayMs: Long = SIGNUP_INITIAL_DELAY_MS,
    maxDelayMs: Long = SIGNUP_MAX_DELAY_MS,
    shouldRetry: (T) -> Boolean,
    block: suspend () -> T
): T {
    var attempt = 0
    var delayMs = initialDelayMs

    while (true) {
        val result = block()

        if (!shouldRetry(result)) {
            return result
        }

        if (attempt >= maxRetries) {
            return result
        }

        val jitter = JITTER_FACTOR + Random.nextDouble(RANDOM)
        delay((delayMs * jitter).toLong())
        delayMs = (delayMs * MULTIPLIER).coerceAtMost(maxDelayMs)
        attempt++
    }
}
