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

package ch.protonmail.android.mailcomposer.dagger

import ch.protonmail.android.composer.data.local.RoomTransactor
import ch.protonmail.android.composer.data.repository.AttachmentRepositoryImpl
import ch.protonmail.android.composer.data.repository.AttachmentStateRepositoryImpl
import ch.protonmail.android.composer.data.repository.DraftRepositoryImpl
import ch.protonmail.android.composer.data.repository.MessageExpirationTimeRepositoryImpl
import ch.protonmail.android.composer.data.repository.MessagePasswordRepositoryImpl
import ch.protonmail.android.composer.data.repository.MessageRepositoryImpl
import ch.protonmail.android.mailcomposer.domain.Transactor
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentStateRepository
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailcomposer.domain.repository.MessageExpirationTimeRepository
import ch.protonmail.android.mailcomposer.domain.repository.MessagePasswordRepository
import ch.protonmail.android.mailcomposer.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MailComposerModule {

    @Binds
    @Reusable
    abstract fun bindsDraftRepository(impl: DraftRepositoryImpl): DraftRepository

    @Binds
    @Reusable
    abstract fun provideMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Reusable
    abstract fun bindsRoomTransactor(impl: RoomTransactor): Transactor

    @Binds
    @Reusable
    abstract fun bindsAttachmentStateRepository(impl: AttachmentStateRepositoryImpl): AttachmentStateRepository

    @Binds
    @Reusable
    abstract fun bindsAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository

    @Binds
    @Reusable
    abstract fun bindsMessagePasswordRepository(impl: MessagePasswordRepositoryImpl): MessagePasswordRepository

    @Binds
    @Reusable
    abstract fun bindsMessageExpirationTimeRepository(
        impl: MessageExpirationTimeRepositoryImpl
    ): MessageExpirationTimeRepository
}
