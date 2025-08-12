/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.android.core.account.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.proton.android.core.account.data.qualifier.QueryWatcherCoroutineScope
import me.proton.android.core.account.data.usecase.ObserveCoreAccountImpl
import me.proton.android.core.account.data.usecase.ObserveCoreSessionsImpl
import me.proton.android.core.account.data.usecase.ObservePrimaryCoreAccountImpl
import me.proton.android.core.account.data.usecase.ObserveCoreAccountsImpl
import me.proton.android.core.account.data.usecase.ObserveStoredAccountsImpl
import me.proton.android.core.account.domain.usecase.ObserveCoreAccount
import me.proton.android.core.account.domain.usecase.ObserveCoreSessions
import me.proton.android.core.account.domain.usecase.ObservePrimaryCoreAccount
import me.proton.android.core.account.domain.usecase.ObserveCoreAccounts
import me.proton.android.core.account.domain.usecase.ObserveStoredAccounts
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoreAccountModule {

    @Provides
    @Singleton
    @QueryWatcherCoroutineScope
    fun provideQueryWatcherCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindsModule {

        @Binds
        @Singleton
        fun bindObserveAllSessions(impl: ObserveCoreSessionsImpl): ObserveCoreSessions

        @Binds
        @Singleton
        fun bindObservePrimaryStoredAccountImpl(impl: ObservePrimaryCoreAccountImpl): ObservePrimaryCoreAccount

        @Binds
        @Singleton
        fun bindObserveCoreAccounts(impl: ObserveCoreAccountsImpl): ObserveCoreAccounts

        @Binds
        @Singleton
        fun bindObserveCoreAccount(impl: ObserveCoreAccountImpl): ObserveCoreAccount

        @Binds
        @Singleton
        fun bindObserveStoredAccounts(impl: ObserveStoredAccountsImpl): ObserveStoredAccounts
    }
}
