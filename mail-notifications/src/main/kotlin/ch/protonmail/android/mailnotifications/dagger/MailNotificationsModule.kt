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

package ch.protonmail.android.mailnotifications.dagger

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import ch.protonmail.android.mailnotifications.data.local.NotificationsPermissionLocalDataSource
import ch.protonmail.android.mailnotifications.data.local.NotificationsPermissionLocalDataSourceImpl
import ch.protonmail.android.mailnotifications.data.repository.DeviceRegistrationRepository
import ch.protonmail.android.mailnotifications.data.repository.DeviceRegistrationRepositoryImpl
import ch.protonmail.android.mailnotifications.data.repository.NotificationsPermissionRepository
import ch.protonmail.android.mailnotifications.data.repository.NotificationsPermissionRepositoryImpl
import ch.protonmail.android.mailnotifications.domain.handler.AccountStateAwareNotificationHandler
import ch.protonmail.android.mailnotifications.domain.handler.NotificationHandler
import ch.protonmail.android.mailnotifications.domain.proxy.NotificationManagerCompatProxy
import ch.protonmail.android.mailnotifications.domain.proxy.NotificationManagerCompatProxyImpl
import ch.protonmail.android.mailnotifications.permissions.NotificationsPermissionOrchestrator
import ch.protonmail.android.mailnotifications.permissions.NotificationsPermissionOrchestratorImpl
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MailNotificationsModule {

    @Provides
    @Reusable
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Reusable
    fun provideNotificationManagerCompat(@ApplicationContext context: Context): NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryPointModule {

        fun handlers(): Set<NotificationHandler>
    }

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        @Singleton
        fun bindDeviceRegistrationRepository(
            implementation: DeviceRegistrationRepositoryImpl
        ): DeviceRegistrationRepository

        @Binds
        @Singleton
        fun bindNotificationPermissionsOrchestrator(
            implementation: NotificationsPermissionOrchestratorImpl
        ): NotificationsPermissionOrchestrator

        @Binds
        @Reusable
        fun bindNotificationPermissionsRepository(
            implementation: NotificationsPermissionRepositoryImpl
        ): NotificationsPermissionRepository

        @Binds
        @Reusable
        fun bindNotificationPermissionsDataSource(
            implementation: NotificationsPermissionLocalDataSourceImpl
        ): NotificationsPermissionLocalDataSource

        @Binds
        @Reusable
        fun bindNotificationManagerCompatProxy(
            notificationManagerProxyImpl: NotificationManagerCompatProxyImpl
        ): NotificationManagerCompatProxy

        @Binds
        @Singleton
        @IntoSet
        fun bindAccountStateAwareNotificationHandler(
            handlerImpl: AccountStateAwareNotificationHandler
        ): NotificationHandler
    }
}
