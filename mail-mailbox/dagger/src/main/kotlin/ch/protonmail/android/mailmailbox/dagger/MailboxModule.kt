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

package ch.protonmail.android.mailmailbox.dagger

import ch.protonmail.android.mailmailbox.data.RustSenderAddressCoroutineScope
import ch.protonmail.android.mailmailbox.data.local.RustSenderAddressDataSourceImpl
import ch.protonmail.android.mailmailbox.data.local.SenderAddressDataSource
import ch.protonmail.android.mailmailbox.data.repository.MailboxBannersRepositoryImpl
import ch.protonmail.android.mailmailbox.data.repository.SenderAddressRepositoryImpl
import ch.protonmail.android.mailmailbox.data.repository.UnreadCountersRepositoryImpl
import ch.protonmail.android.mailmailbox.domain.repository.MailboxBannersRepository
import ch.protonmail.android.mailmailbox.domain.repository.SenderAddressRepository
import ch.protonmail.android.mailmailbox.domain.repository.UnreadCountersRepository
import ch.protonmail.android.mailmailbox.domain.usecase.GetMailboxItems
import ch.protonmail.android.mailmailbox.presentation.paging.MailboxItemPagingSourceFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module(includes = [MailboxModule.BindsModule::class])
@InstallIn(ViewModelComponent::class)
object MailboxModule {

    @Provides
    @RustSenderAddressCoroutineScope
    fun provideRustSenderAddressCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    fun providesMailboxItemPagingSourceFactory(getMailboxItems: GetMailboxItems): MailboxItemPagingSourceFactory =
        MailboxItemPagingSourceFactory(
            getMailboxItems
        )

    @Module
    @InstallIn(ViewModelComponent::class)
    internal interface BindsModule {

        @Binds
        @Reusable
        fun bindsUnreadCountRepository(impl: UnreadCountersRepositoryImpl): UnreadCountersRepository

        @Binds
        @Reusable
        fun bindsMailboxBannersRepository(impl: MailboxBannersRepositoryImpl): MailboxBannersRepository

        @Binds
        @Reusable
        fun bindsSenderAddressRepository(impl: SenderAddressRepositoryImpl): SenderAddressRepository

        @Reusable
        @Binds
        fun provideSenderAddressDataSource(
            senderAddressDataSourceImpl: RustSenderAddressDataSourceImpl
        ): SenderAddressDataSource
    }
}
