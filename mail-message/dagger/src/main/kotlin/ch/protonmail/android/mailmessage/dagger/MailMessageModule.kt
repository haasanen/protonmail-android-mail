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

import ch.protonmail.android.mailmessage.data.ImageLoaderCoroutineScope
import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailmessage.data.local.MessageBodyDataSource
import ch.protonmail.android.mailmessage.data.local.RustMessageBodyDataSource
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSource
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSourceImpl
import ch.protonmail.android.mailmessage.data.local.RustMessageQuery
import ch.protonmail.android.mailmessage.data.local.RustMessageQueryImpl
import ch.protonmail.android.mailmessage.data.repository.InMemoryAvatarImageStateRepositoryImpl
import ch.protonmail.android.mailmessage.data.repository.MessageBodyRepositoryImpl
import ch.protonmail.android.mailmessage.data.repository.PreviousScheduleSendTimeInMemoryRepository
import ch.protonmail.android.mailmessage.data.repository.RustMessageActionRepository
import ch.protonmail.android.mailmessage.data.repository.RustMessageRepositoryImpl
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTrackerImpl
import ch.protonmail.android.mailmessage.domain.repository.InMemoryAvatarImageStateRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageActionRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.repository.PreviousScheduleSendTimeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
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
    @ImageLoaderCoroutineScope
    fun provideImageLoaderCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    @Singleton
    @MessageRustCoroutineScope
    fun provideMessageRustCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    @Singleton
    fun providesMessageRepository(rustMessageDataSource: RustMessageDataSource): MessageRepository =
        RustMessageRepositoryImpl(rustMessageDataSource)

    @Provides
    @Singleton
    fun providesMessageActionRepository(rustMessageDataSource: RustMessageDataSource): MessageActionRepository =
        RustMessageActionRepository(rustMessageDataSource)

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        @Singleton
        fun bindRustMessageQuery(impl: RustMessageQueryImpl): RustMessageQuery

        @Binds
        @Singleton
        fun bindRustMessageDataSource(impl: RustMessageDataSourceImpl): RustMessageDataSource

        @Binds
        @Singleton
        fun bindsRustInvalidationTracker(impl: RustInvalidationTrackerImpl): RustInvalidationTracker

        @Binds
        @Singleton
        fun bindsInMemoryAvatarImageStateRepository(
            impl: InMemoryAvatarImageStateRepositoryImpl
        ): InMemoryAvatarImageStateRepository

        @Binds
        @Singleton
        fun bindsPreviousScheduleSendTimeRepository(
            impl: PreviousScheduleSendTimeInMemoryRepository
        ): PreviousScheduleSendTimeRepository
    }
}

@Module
@InstallIn(ViewModelComponent::class)
interface MailMessageViewModelModule {

    @Binds
    @ViewModelScoped
    fun providesMessageBodyRepository(impl: MessageBodyRepositoryImpl): MessageBodyRepository

    @Binds
    @ViewModelScoped
    fun providesMessageBodyDataSource(impl: RustMessageBodyDataSource): MessageBodyDataSource
}
