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

package ch.protonmail.android.legacymigration.domain.usecase

import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import javax.inject.Inject

class CleanupLegacyNotificationChannels @Inject constructor(
    private val notificationManager: NotificationManagerCompat
) {

    /**
     * Deletes V5/V6 notification channels (if present) to prevent multiple unused entries in the OS App Settings.
     */
    operator fun invoke() {
        (v5ChannelIds + v6ChannelIds).toSet().forEach {
            notificationManager.deleteNotificationChannel(it)
            Timber.d("Legacy migration: Deleting legacy notification channel $it")
        }
    }

    private companion object {

        const val V5_CHANNEL_ONGOING_OPS_ID = "ongoingOperations"
        const val V5_CHANNEL_ACCOUNT_ID = "account"
        const val V5_CHANNEL_ATTACHMENTS_ID = "attachments"
        const val V5_CHANNEL_EMAIL_ID = "emails"
        const val V6_CHANNEL_ATTACHMENTS_ID = "attachment_channel_id"
        const val V6_CHANNEL_EMAIL_CHANNEL_ID = "email_channel_id"
        const val V6_CHANNEL_LOGIN_CHANNEL_ID = "login_channel_id"

        val v5ChannelIds = listOf(
            V5_CHANNEL_EMAIL_ID,
            V5_CHANNEL_ACCOUNT_ID,
            V5_CHANNEL_ATTACHMENTS_ID,
            V5_CHANNEL_ONGOING_OPS_ID
        )

        val v6ChannelIds = listOf(
            V6_CHANNEL_ATTACHMENTS_ID,
            V6_CHANNEL_EMAIL_CHANNEL_ID,
            V6_CHANNEL_LOGIN_CHANNEL_ID
        )
    }
}
