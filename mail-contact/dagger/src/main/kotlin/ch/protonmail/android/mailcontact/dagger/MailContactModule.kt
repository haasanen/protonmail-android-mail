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

package ch.protonmail.android.mailcontact.dagger

import ch.protonmail.android.mailcontact.data.ContactGroupRepositoryImpl
import ch.protonmail.android.mailcontact.data.ContactRustCoroutineScope
import ch.protonmail.android.mailcontact.data.DeviceContactsRepositoryImpl
import ch.protonmail.android.mailcontact.data.DeviceContactsSuggestionsPromptImpl
import ch.protonmail.android.mailcontact.data.local.RustContactDataSource
import ch.protonmail.android.mailcontact.data.local.RustContactDataSourceImpl
import ch.protonmail.android.mailcontact.data.repository.ContactRepositoryImpl
import ch.protonmail.android.mailcontact.domain.DeviceContactsSuggestionsPrompt
import ch.protonmail.android.mailcontact.domain.repository.ContactGroupRepository
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.mailcontact.domain.repository.DeviceContactsRepository
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

@Module(includes = [MailContactModule.BindsModule::class])
@InstallIn(SingletonComponent::class)
object MailContactModule {

    @Provides
    @Singleton
    @ContactRustCoroutineScope
    fun provideContactRustCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        @Reusable
        fun bindRustContactDataSource(impl: RustContactDataSourceImpl): RustContactDataSource

        @Binds
        @Reusable
        fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

        @Binds
        @Reusable
        fun bindContactGroupRepository(impl: ContactGroupRepositoryImpl): ContactGroupRepository

        @Binds
        @Reusable
        fun bindDeviceContactsRepository(impl: DeviceContactsRepositoryImpl): DeviceContactsRepository

        @Binds
        @Singleton
        fun bindDeviceContactsSuggestionsPrompt(
            impl: DeviceContactsSuggestionsPromptImpl
        ): DeviceContactsSuggestionsPrompt

    }
}
