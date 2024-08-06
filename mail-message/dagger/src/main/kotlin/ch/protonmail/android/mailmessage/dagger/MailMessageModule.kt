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

package ch.protonmail.android.mailmessage.dagger

import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailmessage.data.local.MessageLocalDataSource
import ch.protonmail.android.mailmessage.data.local.MessageLocalDataSourceImpl
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.data.local.RustMailboxImpl
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSource
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSourceImpl
import ch.protonmail.android.mailmessage.data.local.RustMessageQuery
import ch.protonmail.android.mailmessage.data.local.RustMessageQueryImpl
import ch.protonmail.android.mailmessage.data.local.SearchResultsLocalDataSource
import ch.protonmail.android.mailmessage.data.local.SearchResultsLocalDataSourceImpl
import ch.protonmail.android.mailmessage.data.local.UnreadMessagesCountLocalDataSource
import ch.protonmail.android.mailmessage.data.local.UnreadMessagesCountLocalDataSourceImpl
import ch.protonmail.android.mailmessage.data.remote.UnreadMessagesCountRemoteDataSource
import ch.protonmail.android.mailmessage.data.remote.UnreadMessagesCountRemoteDataSourceImpl
import ch.protonmail.android.mailmessage.data.repository.OutboxRepositoryImpl
import ch.protonmail.android.mailmessage.data.repository.RustMessageRepositoryImpl
import ch.protonmail.android.mailmessage.data.repository.SearchResultsRepositoryImpl
import ch.protonmail.android.mailmessage.data.repository.UnreadMessageCountRepositoryImpl
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTrackerImpl
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.repository.OutboxRepository
import ch.protonmail.android.mailmessage.domain.repository.SearchResultsRepository
import ch.protonmail.android.mailmessage.domain.repository.UnreadMessagesCountRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module(includes = [MailMessageModule.BindsModule::class])
@InstallIn(SingletonComponent::class)
object MailMessageModule {

    @Provides
    @Singleton
    @MessageRustCoroutineScope
    fun provideMessageRustCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Suppress("LongParameterList")
    @Provides
    @Singleton
    fun providesMessageRepository(rustMessageDataSource: RustMessageDataSource): MessageRepository =
        RustMessageRepositoryImpl(rustMessageDataSource)

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        @Singleton
        fun provideMessageLocalDataSource(localDataSourceImpl: MessageLocalDataSourceImpl): MessageLocalDataSource

        @Binds
        @Singleton
        fun provideOutboxRepositoryImpl(repositoryImpl: OutboxRepositoryImpl): OutboxRepository

        @Binds
        @Singleton
        fun provideSearchResultsRepository(repositoryImpl: SearchResultsRepositoryImpl): SearchResultsRepository

        @Binds
        @Singleton
        fun provideSearchResultsLocalDataSource(
            localDataSourceImpl: SearchResultsLocalDataSourceImpl
        ): SearchResultsLocalDataSource

        @Binds
        @Reusable
        fun bindsUnreadMessagesCountRepository(impl: UnreadMessageCountRepositoryImpl): UnreadMessagesCountRepository

        @Binds
        @Reusable
        fun bindsUnreadMessagesCountRemoteDataSource(
            impl: UnreadMessagesCountRemoteDataSourceImpl
        ): UnreadMessagesCountRemoteDataSource

        @Binds
        @Reusable
        fun bindsUnreadMessagesCountLocalDataSource(
            impl: UnreadMessagesCountLocalDataSourceImpl
        ): UnreadMessagesCountLocalDataSource

        @Binds
        @Singleton
        fun bindRustMailbox(impl: RustMailboxImpl): RustMailbox

        @Binds
        @Singleton
        fun bindRustMessageQuery(impl: RustMessageQueryImpl): RustMessageQuery

        @Binds
        @Singleton
        fun bindRustMessageDataSource(impl: RustMessageDataSourceImpl): RustMessageDataSource

        @Binds
        @Singleton
        fun bindsRustInvalidationTracker(impl: RustInvalidationTrackerImpl): RustInvalidationTracker

    }
}
