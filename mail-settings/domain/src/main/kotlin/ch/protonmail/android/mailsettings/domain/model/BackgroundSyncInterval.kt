/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.mailsettings.domain.model

enum class BackgroundSyncInterval {
    EVERY_15_MINUTES,
    EVERY_30_MINUTES,
    EVERY_1_HOUR,
    EVERY_2_HOURS,
    EVERY_3_HOURS,
    EVERY_5_HOURS,
    EVERY_12_HOURS,
    EVERY_24_HOURS,
    NEVER;

    /** Minutes between periodic background syncs. Null for [NEVER]. */
    fun intervalMinutes(): Long? = when (this) {
        NEVER -> null
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
