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
import ch.protonmail.android.mailfeatureflags.data.local.UnleashFeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.data.local.factory.BooleanFeatureFlagFactory
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.annotation.FeatureFlagsCoroutineScope
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsDebugInspectDbEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsLinkifyUrlsEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsMessageExpirationEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsMessagePasswordEnabled
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsUpsellEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.DebugInspectDbEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import ch.protonmail.android.mailfeatureflags.domain.model.LinkifyUrlEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.MessageExpirationEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.MessagePasswordEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.UpsellingEnabled
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    @FeatureFlagsCoroutineScope
    fun provideFeatureFlagsCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    @Singleton
    @IsMessagePasswordEnabled
    fun provideMessagePasswordEnabled(factory: BooleanFeatureFlagFactory) =
        factory.create(key = MessagePasswordEnabled.key, false)

    @Provides
    @IntoSet
    @Singleton
    fun provideLinkifyUrlEnabledDefinitions(): FeatureFlagDefinition = LinkifyUrlEnabled

    @Provides
    @Singleton
    @IsLinkifyUrlsEnabled
    fun provideLinkifyUrlEnabled(factory: BooleanFeatureFlagFactory) =
        factory.create(key = LinkifyUrlEnabled.key, false)

    @Provides
    @Singleton
    @IsUpsellEnabled
    fun provideUpsellEnabled(factory: BooleanFeatureFlagFactory) = factory.create(key = UpsellingEnabled.key, false)

    @Provides
    @IntoSet
    @Singleton
    fun provideDebugInspectDbEnabledDefinition(): FeatureFlagDefinition = DebugInspectDbEnabled

    @Provides
    @Singleton
    @IsDebugInspectDbEnabled
    fun provideIsDebugInspectDbEnabled(factory: BooleanFeatureFlagFactory) =
        factory.create(key = DebugInspectDbEnabled.key, false)

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
    fun provideUnleashProvider(impl: UnleashFeatureFlagValueProvider): FeatureFlagValueProvider = impl

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
    fun provideMessageExpirationEnabled(factory: BooleanFeatureFlagFactory) =
        factory.create(key = MessageExpirationEnabled.key, false)
}
