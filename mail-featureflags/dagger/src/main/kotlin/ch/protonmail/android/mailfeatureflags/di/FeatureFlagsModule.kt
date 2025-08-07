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
import ch.protonmail.android.mailfeatureflags.domain.DebugInspectDbEnabled
import ch.protonmail.android.mailfeatureflags.domain.MessagePasswordEnabled
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagResolver
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.LinkifyUrlEnabled
import ch.protonmail.android.mailfeatureflags.domain.MessageExpirationEnabled
import ch.protonmail.android.mailfeatureflags.domain.ShareViaEnabled
import ch.protonmail.android.mailfeatureflags.domain.SnoozeEnabled
import ch.protonmail.android.mailfeatureflags.domain.UpsellingEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsDebugInspectDbEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsMessagePasswordEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsLinkifyUrlsEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsMessageExpirationEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsShareViaEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsSnoozeEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsUpsellEnabled
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
    fun provideMessagePasswordEnabledDefinitions(): FeatureFlagDefinition = MessagePasswordEnabled

    @Provides
    @Singleton
    @IsMessagePasswordEnabled
    fun provideMessagePasswordEnabled(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(MessagePasswordEnabled.key)

    @Provides
    @IntoSet
    @Singleton
    fun provideLinkifyUrlEnabledDefinitions(): FeatureFlagDefinition = LinkifyUrlEnabled

    @Provides
    @Singleton
    @IsLinkifyUrlsEnabled
    fun provideLinkifyUrlEnabled(resolver: FeatureFlagResolver) = resolver.observeFeatureFlag(LinkifyUrlEnabled.key)

    @Provides
    @Singleton
    @IsSnoozeEnabled
    fun provideSnoozeEnabled(resolver: FeatureFlagResolver) = resolver.observeFeatureFlag(SnoozeEnabled.key)

    @Provides
    @Singleton
    @IsUpsellEnabled
    fun provideUpsellEnabled(resolver: FeatureFlagResolver) = resolver.observeFeatureFlag(UpsellingEnabled.key)

    @Provides
    @IntoSet
    @Singleton
    fun provideShareViaDefinition(): FeatureFlagDefinition = ShareViaEnabled

    @Provides
    @Singleton
    @IsShareViaEnabled
    fun provideShareViaEnabled(resolver: FeatureFlagResolver) = resolver.observeFeatureFlag(ShareViaEnabled.key)

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

    @Provides
    @IntoSet
    @Singleton
    fun provideSnoozeEnabledDefinitions(): FeatureFlagDefinition = SnoozeEnabled

    @Provides
    @IntoSet
    @Singleton
    fun provideUpsellEnabledDefinitions(): FeatureFlagDefinition = UpsellingEnabled

    @Provides
    @IntoSet
    @Singleton
    fun provideMessageExpirationDefinitions(): FeatureFlagDefinition = MessageExpirationEnabled

    @Provides
    @Singleton
    @IsMessageExpirationEnabled
    fun provideMessageExpirationEnabled(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(MessageExpirationEnabled.key)
}
