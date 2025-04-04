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
import ch.protonmail.android.mailfeatureflags.domain.ComposerEnabledDefinition
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagResolver
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.annotation.ComposerEnabled
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
    fun provideComposerEnabledDefinition(): FeatureFlagDefinition = ComposerEnabledDefinition

    @Provides
    @Singleton
    @ComposerEnabled
    fun provideComposerEnabled(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(ComposerEnabledDefinition.key)


    @Provides
    @IntoSet
    @Singleton
    fun provideV6CssInjectionEnabledDefinition(): FeatureFlagDefinition = UseV6CssInjectionDefinition

    @Provides
    @Singleton
    @UseV6CssInjection
    fun provideUseV6CssInjection(resolver: FeatureFlagResolver) =
        resolver.observeFeatureFlag(UseV6CssInjectionDefinition.key)

    @Provides
    @IntoSet
    @Singleton
    fun provideDefaultProvider(impl: DefaultFeatureFlagValueProvider): FeatureFlagValueProvider = impl

    @Provides
    @IntoSet
    @Singleton
    fun provideDataStoreProvider(impl: DataStoreFeatureFlagValueProvider): FeatureFlagValueProvider = impl
}
