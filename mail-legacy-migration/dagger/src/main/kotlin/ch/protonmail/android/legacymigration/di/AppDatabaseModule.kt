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

package ch.protonmail.android.legacymigration.di

import android.content.Context
import androidx.room.RoomDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.keytransparency.data.local.KeyTransparencyDatabase
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.telemetry.data.db.TelemetryDatabase
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.userrecovery.data.db.DeviceRecoveryDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): ch.protonmail.android.legacymigration.data.local.db.AppDatabase =
        ch.protonmail.android.legacymigration.data.local.db.AppDatabase.buildDatabase(context)
}

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
abstract class AppDatabaseBindsModule {
    @Binds
    abstract fun provideRoomDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): RoomDatabase

    @Binds
    abstract fun provideAccountDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): AccountDatabase

    @Binds
    abstract fun provideUserDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): UserDatabase

    @Binds
    abstract fun provideAddressDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): AddressDatabase

    @Binds
    abstract fun provideKeySaltDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): KeySaltDatabase

    @Binds
    abstract fun providePublicAddressDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): PublicAddressDatabase

    @Binds
    abstract fun provideHumanVerificationDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): HumanVerificationDatabase

    @Binds
    abstract fun provideMailSettingsDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): MailSettingsDatabase

    @Binds
    abstract fun provideUserSettingsDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): UserSettingsDatabase

    @Binds
    abstract fun provideOrganizationDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): OrganizationDatabase

    @Binds
    abstract fun provideEventMetadataDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): EventMetadataDatabase

    @Binds
    abstract fun provideFeatureFlagDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): FeatureFlagDatabase

    @Binds
    abstract fun provideChallengeDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): ChallengeDatabase

    @Binds
    abstract fun providePaymentDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): PaymentDatabase

    @Binds
    abstract fun provideObservabilityDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): ObservabilityDatabase

    @Binds
    abstract fun provideKeyTransparencyDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): KeyTransparencyDatabase

    @Binds
    abstract fun provideNotificationDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): NotificationDatabase

    @Binds
    abstract fun providePushDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): PushDatabase

    @Binds
    abstract fun provideTelemetryDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): TelemetryDatabase

    @Binds
    abstract fun provideDeviceRecoveryDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): DeviceRecoveryDatabase

    @Binds
    abstract fun provideAuthDatabase(
        appDatabase: ch.protonmail.android.legacymigration.data.local.db.AppDatabase
    ): AuthDatabase
}
