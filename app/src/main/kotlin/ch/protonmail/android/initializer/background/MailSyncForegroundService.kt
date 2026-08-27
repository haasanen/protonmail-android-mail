/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.initializer.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import ch.protonmail.android.R
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import uniffi.mail_uniffi.MailBackgroundExecScope
import javax.inject.Inject

/**
 * Foreground service that keeps the mail sync stream running while the app is backgrounded.
 *
 * The Rust core suspends its task service when the app exits the foreground. A held
 * background execution scope prevents that suspension, so the new-mail stream keeps
 * delivering while the process stays alive.
 */
@AndroidEntryPoint
class MailSyncForegroundService @Inject constructor(
    private val mailSessionRepository: MailSessionRepository
) : Service() {

    private var backgroundExecutionScope: MailBackgroundExecScope? = null

    override fun onCreate() {
        super.onCreate()
        try {
            startForeground(
                NOTIFICATION_ID,
                buildForegroundNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed to post foreground notification; stopping sync service")
            stopSelf()
            return
        }
        acquireBackgroundExecutionScope()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onTimeout(timeoutType: Int) {
        Timber.w("Mail sync service timed out (dataSync limit, type=$timeoutType); stopping")
        stopSelf()
    }

    override fun onDestroy() {
        backgroundExecutionScope?.finsihed()
        backgroundExecutionScope = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun acquireBackgroundExecutionScope() {
        if (!mailSessionRepository.isMailSessionInitialised()) {
            Timber.w("Mail session not initialised; running without background execution scope")
            return
        }
        backgroundExecutionScope = mailSessionRepository.getMailSession().newBackgroundExecutionScope()
        Timber.d("Mail sync service: background execution scope acquired")
    }

    private fun buildForegroundNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.mail_sync_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.mail_sync_notification_text))
            .setSmallIcon(R.drawable.ic_proton_brand_proton_mail)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "v7_mail_sync_channel_id"
    }
}
