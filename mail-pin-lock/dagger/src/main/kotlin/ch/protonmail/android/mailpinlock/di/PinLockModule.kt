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

package ch.protonmail.android.mailpinlock.di

import ch.protonmail.android.mailpinlock.data.AppLockDataSource
import ch.protonmail.android.mailpinlock.data.BiometricsSystemStateRepositoryImpl
import ch.protonmail.android.mailpinlock.data.RustAppLockDataSource
import ch.protonmail.android.mailpinlock.domain.AutolockRepository
import ch.protonmail.android.mailpinlock.domain.BiometricsSystemStateRepository
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ch.protonmail.android.mailpinlock.data.AutolockRepository as AutolockRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
object PinLockModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindsModule {

        @Binds
        @Reusable
        fun bindAutoLockRepository(impl: AutolockRepositoryImpl): AutolockRepository

        @Binds
        @Reusable
        fun bindApplockDatasource(impl: RustAppLockDataSource): AppLockDataSource

        @Binds
        fun bindBiometricsSystemStateRepository(
            impl: BiometricsSystemStateRepositoryImpl
        ): BiometricsSystemStateRepository
    }
}
