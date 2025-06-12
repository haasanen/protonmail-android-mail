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

package ch.protonmail.android.mailfeatureflags.di

import ch.protonmail.android.mailfeatureflags.data.local.DataStoreFeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.data.local.DefaultFeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.ChooseAttachmentSourceEnabled
import ch.protonmail.android.mailfeatureflags.domain.DebugInspectDbEnabled
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagResolver
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.InlineImagesComposerEnabled
import ch.protonmail.android.mailfeatureflags.domain.ScheduledSendEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.InlineImagesInComposerEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsChooseAttachmentSourceEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsDebugInspectDbEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.ScheduleSendEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureFlagsModule {

    @Provides
    @IntoSet
    @Singleton
    fun provideScheduledSendDefinition(): FeatureFlagDefinition = ScheduledSendEnabled

    @Provides
    @Singleton
    @ScheduleSendEnabled
    fun provideScheduledSendEnabled(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(ScheduledSendEnabled.key)

    @Provides
    @IntoSet
    @Singleton
    fun provideAttachmentSourceDefinition(): FeatureFlagDefinition = ChooseAttachmentSourceEnabled

    @Provides
    @Singleton
    @IsChooseAttachmentSourceEnabled
    fun provideAttachmentSourceEnabled(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(ChooseAttachmentSourceEnabled.key)

    @Provides
    @IntoSet
    @Singleton
    fun provideInlineImagesComposerDefinition(): FeatureFlagDefinition = InlineImagesComposerEnabled

    @Provides
    @Singleton
    @InlineImagesInComposerEnabled
    fun provideInlineImagesComposerEnabled(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(InlineImagesComposerEnabled.key)

    @Provides
    @IntoSet
    @Singleton
    fun provideDebugInspectDbEnabledDefinition(): FeatureFlagDefinition = DebugInspectDbEnabled

    @Provides
    @Singleton
    @IsDebugInspectDbEnabled
    fun provideIsDebugInspectDbEnabled(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(DebugInspectDbEnabled.key)

    @Provides
    @IntoSet
    @Singleton
    fun provideDefaultProvider(impl: DefaultFeatureFlagValueProvider): FeatureFlagValueProvider = impl

    @Provides
    @IntoSet
    @Singleton
    fun provideDataStoreProvider(impl: DataStoreFeatureFlagValueProvider): FeatureFlagValueProvider = impl
}
