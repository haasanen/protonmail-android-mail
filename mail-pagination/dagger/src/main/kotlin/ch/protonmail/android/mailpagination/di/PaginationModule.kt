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

package ch.protonmail.android.mailpagination.di

import ch.protonmail.android.mailpagination.data.PagingCoroutineScope
import ch.protonmail.android.mailpagination.data.cache.PagingCacheWithInvalidationFilterImpl
import ch.protonmail.android.mailpagination.data.repository.InMemoryPageInvalidationRepositoryImpl
import ch.protonmail.android.mailpagination.domain.cache.PagingCacheWithInvalidationFilter
import ch.protonmail.android.mailpagination.domain.repository.PageInvalidationRepository
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaginationModule {

    @Provides
    @Singleton
    @PagingCoroutineScope
    fun providePagingCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    fun provideConversationPagingCache(
        pageInvalidationRepository: PageInvalidationRepository,
        @PagingCoroutineScope coroutineScope: CoroutineScope
    ): PagingCacheWithInvalidationFilter<LocalConversation> =
        PagingCacheWithInvalidationFilterImpl(pageInvalidationRepository, coroutineScope)

    @Provides
    fun provideMessagePagingCache(
        pageInvalidationRepository: PageInvalidationRepository,
        @PagingCoroutineScope coroutineScope: CoroutineScope
    ): PagingCacheWithInvalidationFilter<LocalMessageMetadata> =
        PagingCacheWithInvalidationFilterImpl(pageInvalidationRepository, coroutineScope)

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        fun bindPageInvalidationRepository(impl: InMemoryPageInvalidationRepositoryImpl): PageInvalidationRepository
    }
}
