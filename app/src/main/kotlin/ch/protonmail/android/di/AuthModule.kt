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

package ch.protonmail.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.DefaultHelpOptionHandler
import me.proton.core.auth.presentation.HelpOptionHandler
import me.proton.core.user.domain.entity.User
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated(
    """
    This module is deprecated. Can't be removed yet due to required bindings from legacy proton-libs.
    """
)
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideUserCheck(): PostLoginAccountSetup.UserCheck = MockUserCheck()

    @Provides
    @Singleton
    fun provideHelpOptionHandler(): HelpOptionHandler = DefaultHelpOptionHandler()
}

private class MockUserCheck @Inject constructor() : PostLoginAccountSetup.UserCheck {

    override suspend fun invoke(user: User) = PostLoginAccountSetup.UserCheckResult.Success
}
