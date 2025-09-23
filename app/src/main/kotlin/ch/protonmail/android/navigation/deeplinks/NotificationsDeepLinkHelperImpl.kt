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

package ch.protonmail.android.navigation.deeplinks

import android.content.Intent
import ch.protonmail.android.feature.appicon.usecase.CreateLaunchIntent
import ch.protonmail.android.mailnotifications.domain.NotificationsDeepLinkHelper
import javax.inject.Inject

class NotificationsDeepLinkHelperImpl @Inject constructor(
    private val createLaunchIntent: CreateLaunchIntent
) : NotificationsDeepLinkHelper {

    override fun buildMessageDeepLinkIntent(
        notificationId: String,
        messageId: String,
        userId: String
    ): Intent {
        val intent = createLaunchIntent()?.apply {
            action = Intent.ACTION_VIEW
            data = buildMessageDeepLinkUri(notificationId, messageId, userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return intent!!
    }

    override fun buildMessageGroupDeepLinkIntent(notificationId: String, userId: String): Intent {
        val intent = createLaunchIntent()?.apply {
            action = Intent.ACTION_VIEW
            data = buildMessageGroupDeepLinkUri(notificationId, userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return intent!!
    }
}
