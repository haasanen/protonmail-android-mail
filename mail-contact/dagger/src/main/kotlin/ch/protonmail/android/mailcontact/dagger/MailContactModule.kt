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

import ch.protonmail.android.mailcontact.data.ContactDetailRepositoryImpl
import ch.protonmail.android.mailcontact.data.ContactGroupRepositoryImpl
import ch.protonmail.android.mailcontact.data.DeviceContactsRepositoryImpl
import ch.protonmail.android.mailcontact.data.DeviceContactsSuggestionsPromptImpl
import ch.protonmail.android.mailcontact.data.local.RustContactDataSource
import ch.protonmail.android.mailcontact.data.local.RustContactDataSourceImpl
import ch.protonmail.android.mailcontact.data.repository.ContactRepositoryImpl
import ch.protonmail.android.mailcontact.domain.DeviceContactsSuggestionsPrompt
import ch.protonmail.android.mailcontact.domain.repository.ContactDetailRepository
import ch.protonmail.android.mailcontact.domain.repository.ContactGroupRepository
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.mailcontact.domain.repository.DeviceContactsRepository
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MailContactModule {

    @Binds
    @Reusable
    abstract fun bindRustContactDataSource(impl: RustContactDataSourceImpl): RustContactDataSource

    @Binds
    @Reusable
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Reusable
    abstract fun bindContactDetailRepository(impl: ContactDetailRepositoryImpl): ContactDetailRepository

    @Binds
    @Reusable
    abstract fun bindContactGroupRepository(impl: ContactGroupRepositoryImpl): ContactGroupRepository

    @Binds
    @Reusable
    abstract fun bindDeviceContactsRepository(impl: DeviceContactsRepositoryImpl): DeviceContactsRepository

    @Binds
    @Singleton
    abstract fun bindDeviceContactsSuggestionsPrompt(
        impl: DeviceContactsSuggestionsPromptImpl
    ): DeviceContactsSuggestionsPrompt

}
