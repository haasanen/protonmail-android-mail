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

package ch.protonmail.android.mailconversation.dagger

import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailconversation.data.local.RustConversationDataSource
import ch.protonmail.android.mailconversation.data.local.RustConversationDataSourceImpl
import ch.protonmail.android.mailconversation.data.local.RustConversationDetailQuery
import ch.protonmail.android.mailconversation.data.local.RustConversationDetailQueryImpl
import ch.protonmail.android.mailconversation.data.local.RustConversationsQuery
import ch.protonmail.android.mailconversation.data.local.RustConversationsQueryImpl
import ch.protonmail.android.mailconversation.data.repository.RustConversationActionRepository
import ch.protonmail.android.mailconversation.data.repository.RustConversationRepositoryImpl
import ch.protonmail.android.mailconversation.domain.repository.ConversationActionRepository
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module(includes = [MailConversationModule.BindsModule::class])
@InstallIn(ViewModelComponent::class)
object MailConversationModule {

    @Provides
    @ConversationRustCoroutineScope
    fun provideConversationRustCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Module
    @InstallIn(ViewModelComponent::class)
    internal interface BindsModule {

        @Binds
        @Reusable
        fun bindsRustConversationDataSource(impl: RustConversationDataSourceImpl): RustConversationDataSource

        @Binds
        fun bindsRustConversationsQuery(impl: RustConversationsQueryImpl): RustConversationsQuery

        @Binds
        fun bindsRustConversationDetailQuery(impl: RustConversationDetailQueryImpl): RustConversationDetailQuery

        @Binds
        @Reusable
        fun bindsConversationRepositoryImpl(impl: RustConversationRepositoryImpl): ConversationRepository

        @Binds
        @Reusable
        fun bindsConversationActionRepositoryImpl(impl: RustConversationActionRepository): ConversationActionRepository
    }
}
