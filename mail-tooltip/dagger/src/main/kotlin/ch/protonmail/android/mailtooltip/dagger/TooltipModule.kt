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

package ch.protonmail.android.mailtooltip.dagger

import android.content.Context
import ch.protonmail.android.mailtooltip.data.AccountsTooltipDataStoreProvider
import ch.protonmail.android.mailtooltip.data.local.AccountsTooltipLocalDataSource
import ch.protonmail.android.mailtooltip.data.local.AccountsTooltipLocalDataSourceImpl
import ch.protonmail.android.mailtooltip.data.repository.AccountsTooltipRepositoryImpl
import ch.protonmail.android.mailtooltip.domain.repository.AccountsTooltipRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(includes = [TooltipModule.BindsModule::class])
@InstallIn(SingletonComponent::class)
object TooltipModule {

    @Provides
    @Singleton
    fun provideAccountHighlightDataStoreProvider(
        @ApplicationContext context: Context
    ): AccountsTooltipDataStoreProvider = AccountsTooltipDataStoreProvider(context)

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        @Reusable
        fun bindsAccountsHighlightLocalDataSource(
            impl: AccountsTooltipLocalDataSourceImpl
        ): AccountsTooltipLocalDataSource

        @Binds
        @Reusable
        fun bindsAccountsHighlightRepository(impl: AccountsTooltipRepositoryImpl): AccountsTooltipRepository
    }
}
