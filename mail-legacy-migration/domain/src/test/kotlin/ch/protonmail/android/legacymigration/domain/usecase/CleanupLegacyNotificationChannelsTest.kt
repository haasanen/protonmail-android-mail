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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class CleanupLegacyNotificationChannelsTest {

    private val notificationManagerCompat = mockk<NotificationManagerCompat>()

    private lateinit var cleanupLegacyNotificationChannels: CleanupLegacyNotificationChannels

    @BeforeTest
    fun setup() {
        every { notificationManagerCompat.deleteNotificationChannel(any()) } just runs
        cleanupLegacyNotificationChannels = CleanupLegacyNotificationChannels(notificationManagerCompat)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should delete all legacy notification channels`() = runTest {
        // When
        cleanupLegacyNotificationChannels.invoke()

        // Then
        verify {
            notificationManagerCompat.deleteNotificationChannel(V5_CHANNEL_ONGOING_OPS_ID)
            notificationManagerCompat.deleteNotificationChannel(V5_CHANNEL_ACCOUNT_ID)
            notificationManagerCompat.deleteNotificationChannel(V5_CHANNEL_ATTACHMENTS_ID)
            notificationManagerCompat.deleteNotificationChannel(V5_CHANNEL_EMAIL_ID)
            notificationManagerCompat.deleteNotificationChannel(V6_CHANNEL_ATTACHMENTS_ID)
            notificationManagerCompat.deleteNotificationChannel(V6_CHANNEL_EMAIL_CHANNEL_ID)
            notificationManagerCompat.deleteNotificationChannel(V6_CHANNEL_LOGIN_CHANNEL_ID)
        }

        confirmVerified(notificationManagerCompat)
    }

    private companion object {

        const val V5_CHANNEL_ONGOING_OPS_ID = "ongoingOperations"
        const val V5_CHANNEL_ACCOUNT_ID = "account"
        const val V5_CHANNEL_ATTACHMENTS_ID = "attachments"
        const val V5_CHANNEL_EMAIL_ID = "emails"
        const val V6_CHANNEL_ATTACHMENTS_ID = "attachment_channel_id"
        const val V6_CHANNEL_EMAIL_CHANNEL_ID = "email_channel_id"
        const val V6_CHANNEL_LOGIN_CHANNEL_ID = "login_channel_id"
    }
}
