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

package ch.protonmail.android.navigation.deeplinks

import android.net.Uri
import ch.protonmail.android.mailnotifications.domain.NotificationsDeepLinkHelper
import timber.log.Timber

internal object DeepLinkParser {

    private const val MessagePathLength = 4
    private const val GroupPathLength = 3

    fun parseUri(uri: Uri?): NotificationDeepLinkData? {
        uri ?: return null

        if (uri.host != NotificationsDeepLinkHelper.NotificationHost) return null

        val pathSegments = uri.pathSegments
        Timber.d("Trampoline: pathSegments = $pathSegments")

        return when (pathSegments.size) {

            // Message: /mailbox/message/{messageId}/{userId}/{notificationId}
            MessagePathLength if pathSegments[0] == "mailbox" && pathSegments[1] == "message" -> {
                NotificationDeepLinkData.Message(
                    messageId = pathSegments[2],
                    userId = pathSegments[3]
                )
            }

            // Group: /mailbox/{notificationId}/{userId}
            GroupPathLength if pathSegments[0] == "mailbox" -> {
                NotificationDeepLinkData.Group(
                    userId = pathSegments[2]
                )
            }

            else -> {
                Timber.w("Trampoline: Unknown deep link format: $uri")
                null
            }
        }
    }
}
