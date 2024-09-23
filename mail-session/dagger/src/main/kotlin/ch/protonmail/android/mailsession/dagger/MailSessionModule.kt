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

package ch.protonmail.android.mailsession.dagger

import ch.protonmail.android.mailsession.data.RepositoryFlowCoroutineScope
import ch.protonmail.android.mailsession.data.repository.InMemoryMailSessionRepository
import ch.protonmail.android.mailsession.data.repository.RustEventLoopRepository
import ch.protonmail.android.mailsession.data.repository.UserSessionRepositoryImpl
import ch.protonmail.android.mailsession.domain.coroutines.EventLoopScope
import ch.protonmail.android.mailsession.domain.repository.EventLoopRepository
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module(includes = [MailSessionModule.BindsModule::class])
@InstallIn(SingletonComponent::class)
object MailSessionModule {

    @Provides
    @Singleton
    @RepositoryFlowCoroutineScope
    fun provideRepositoryFlowCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    @Singleton
    fun providesUserRepository(mailSessionRepository: MailSessionRepository): UserSessionRepository =
        UserSessionRepositoryImpl(mailSessionRepository)

    @Provides
    @Singleton
    @EventLoopScope
    fun provideEventLoopScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindsModule {

        @Binds
        @Singleton
        fun bindMailSessionRepository(impl: InMemoryMailSessionRepository): MailSessionRepository

        @Binds
        @Singleton
        fun bindEventLoopRepository(impl: RustEventLoopRepository): EventLoopRepository
    }
}
