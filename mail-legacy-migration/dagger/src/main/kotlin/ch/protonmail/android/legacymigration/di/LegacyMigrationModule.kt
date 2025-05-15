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

import ch.protonmail.android.legacymigration.data.local.LegacyAccountDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyAccountDataSourceImpl
import ch.protonmail.android.legacymigration.data.local.LegacyMigrationStatusLocalDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyMigrationStatusLocalDataSourceImpl
import ch.protonmail.android.legacymigration.data.local.LegacyUserAddressDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserAddressDataSourceImpl
import ch.protonmail.android.legacymigration.data.local.LegacyUserDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserDataSourceImpl
import ch.protonmail.android.legacymigration.data.repository.LegacyAccountRepositoryImpl
import ch.protonmail.android.legacymigration.data.repository.LegacyMigrationStatusRepositoryImpl
import ch.protonmail.android.legacymigration.domain.repository.LegacyAccountRepository
import ch.protonmail.android.legacymigration.domain.repository.LegacyMigrationStatusRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Binds
import javax.inject.Singleton

@Module(includes = [LegacyMigrationModule.BindsModule::class])
@InstallIn(SingletonComponent::class)
object LegacyMigrationModule {

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        fun bindLegacyAccountRepository(impl: LegacyAccountRepositoryImpl): LegacyAccountRepository

        @Binds
        fun bindLegacyAccountDataSource(impl: LegacyAccountDataSourceImpl): LegacyAccountDataSource

        @Binds
        fun bindLegacyUserDataSource(impl: LegacyUserDataSourceImpl): LegacyUserDataSource

        @Binds
        fun bindLegacyUserAddressDataSource(impl: LegacyUserAddressDataSourceImpl): LegacyUserAddressDataSource

        @Binds
        @Singleton
        fun bindLegacyMigrationStatusLocalDataSource(
            impl: LegacyMigrationStatusLocalDataSourceImpl
        ): LegacyMigrationStatusLocalDataSource

        @Binds
        @Singleton
        fun bindLegacyMigrationStatusRepository(
            impl: LegacyMigrationStatusRepositoryImpl
        ): LegacyMigrationStatusRepository

    }
}
