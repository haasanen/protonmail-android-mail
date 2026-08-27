/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.mailsettings.domain.model

enum class BackgroundSyncInterval {
    REAL_TIME,
    EVERY_15_MINUTES,
    EVERY_30_MINUTES,
    EVERY_1_HOUR,
    EVERY_2_HOURS,
    EVERY_3_HOURS,
    EVERY_5_HOURS,
    EVERY_12_HOURS,
    EVERY_24_HOURS,
    NEVER;

    val isRealTime: Boolean get() = this == REAL_TIME
    val isNever: Boolean get() = this == NEVER

    /**
     * Minutes between periodic background syncs. Null for [REAL_TIME] (foreground service
     * keeps the stream live) and [NEVER] (no background sync).
     */
    fun intervalMinutes(): Long? = when (this) {
        REAL_TIME, NEVER -> null
        EVERY_15_MINUTES -> 15L
        EVERY_30_MINUTES -> 30L
        EVERY_1_HOUR -> 60L
        EVERY_2_HOURS -> 120L
        EVERY_3_HOURS -> 180L
        EVERY_5_HOURS -> 300L
        EVERY_12_HOURS -> 720L
        EVERY_24_HOURS -> 1440L
    }
}
