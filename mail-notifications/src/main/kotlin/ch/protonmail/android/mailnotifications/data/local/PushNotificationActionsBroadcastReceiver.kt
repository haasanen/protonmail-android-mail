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

package ch.protonmail.android.mailnotifications.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailnotifications.data.model.QuickActionPayloadData
import ch.protonmail.android.mailnotifications.data.usecase.ExecutePushNotificationAction
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationDismissPendingIntentData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationPendingIntentPayloadData
import ch.protonmail.android.mailnotifications.domain.usecase.DismissEmailNotificationsForUser
import ch.protonmail.android.mailnotifications.domain.usecase.actions.CreateNotificationAction.Companion.NotificationActionIntentExtraKey
import ch.protonmail.android.mailnotifications.domain.usecase.actions.CreateNotificationAction.Companion.NotificationDismissalIntentExtraKey
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.deserialize
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
internal class PushNotificationActionsBroadcastReceiver @Inject constructor() : BroadcastReceiver() {

    @Inject
    lateinit var executePushNotificationAction: ExecutePushNotificationAction

    @Inject
    lateinit var dismissEmailNotificationsForUser: DismissEmailNotificationsForUser

    @Inject
    @AppScope
    lateinit var coroutineScope: CoroutineScope

    @Inject
    @ApplicationContext
    lateinit var applicationContext: Context

    override fun onReceive(context: Context?, intent: Intent?) {
        val rawAction = intent?.extras?.getString(NotificationActionIntentExtraKey)
        if (rawAction != null) {
            val actionData = rawAction.deserialize<PushNotificationPendingIntentPayloadData>()
            handleNotificationAction(actionData)
            return
        }

        val dismissalAction = intent?.extras?.getString(NotificationDismissalIntentExtraKey)
        if (dismissalAction != null) {
            val actionData = dismissalAction.deserialize<PushNotificationDismissPendingIntentData>()
            handleNotificationDismissal(actionData)
            return
        }
    }

    private fun handleNotificationAction(actionData: PushNotificationPendingIntentPayloadData) {
        val userId = UserId(actionData.userId)
        val quickActionData = QuickActionPayloadData(userId, actionData.messageId, actionData.action)

        coroutineScope.launch {
            executePushNotificationAction(quickActionData)
                .onLeft { Timber.e("Error performing quick action $actionData - $it") }
                .onRight { Timber.d("Quick action executed with success - '${actionData.action}'") }
        }

        dismissEmailNotificationsForUser(
            userId = userId,
            notificationId = actionData.notificationId,
            checkIfNotificationExists = false
        )
    }

    private fun handleNotificationDismissal(actionData: PushNotificationDismissPendingIntentData) {
        when (actionData) {
            is PushNotificationDismissPendingIntentData.GroupNotification -> {
                dismissEmailNotificationsForUser(
                    userId = UserId(actionData.groupId)
                )
            }

            is PushNotificationDismissPendingIntentData.SingleNotification -> {
                dismissEmailNotificationsForUser(
                    userId = UserId(actionData.userId),
                    notificationId = actionData.notificationId,
                    checkIfNotificationExists = true // Always check for swipe dismissals
                )
            }
        }
    }
}
