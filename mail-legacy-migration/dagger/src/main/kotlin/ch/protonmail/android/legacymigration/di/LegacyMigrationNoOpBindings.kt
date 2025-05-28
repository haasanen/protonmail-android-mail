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

import ch.protonmail.android.legacymigration.di.stubs.AuthDeviceRemoteDataSourceNoOp
import ch.protonmail.android.legacymigration.di.stubs.EventManagerProviderNoOp
import ch.protonmail.android.legacymigration.di.stubs.FeatureFlagLocalDataSourceNoOp
import ch.protonmail.android.legacymigration.di.stubs.FeatureFlagRemoteDataSourceNoOp
import ch.protonmail.android.legacymigration.di.stubs.FeatureFlagRepositoryNoOp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.auth.domain.repository.AuthDeviceRemoteDataSource
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object LegacyMigrationNoOpModule {

    @Provides
    @Singleton
    fun provideFeatureFlagRepository(): FeatureFlagRepository = FeatureFlagRepositoryNoOp

    @Provides
    @Singleton
    fun provideLegacyEventManagerProvider(): EventManagerProvider = EventManagerProviderNoOp

    @Provides
    @Singleton
    fun provideLegacyFFRemoteDataSource(): FeatureFlagRemoteDataSource = FeatureFlagRemoteDataSourceNoOp

    @Provides
    @Singleton
    fun provideLegacyFFLocalDataSource(): FeatureFlagLocalDataSource = FeatureFlagLocalDataSourceNoOp


    @Provides
    @Singleton
    fun provideAuthDeviceRemoteDs(): AuthDeviceRemoteDataSource = AuthDeviceRemoteDataSourceNoOp
}
