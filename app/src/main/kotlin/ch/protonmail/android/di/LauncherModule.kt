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

import ch.protonmail.android.mailcommon.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

import ch.protonmail.android.mailnotifications.permissions.NotificationsPermissionsOrchestrator
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.navigation.BaseLauncherViewModel
import ch.protonmail.android.navigation.LauncherViewModel
import ch.protonmail.android.navigation.RustLauncherViewModel
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.domain.entity.Product
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.report.presentation.ReportOrchestrator
import me.proton.core.usersettings.presentation.UserSettingsOrchestrator

@Module
@InstallIn(SingletonComponent::class)
object LauncherModule {

    @Suppress("LongParameterList")
    @Provides
    fun provideBaseLauncherViewModel(
        userSessionRepository: UserSessionRepository,
        product: Product,
        requiredAccountType: AccountType,
        accountManager: AccountManager,
        authOrchestrator: AuthOrchestrator,
        plansOrchestrator: PlansOrchestrator,
        reportOrchestrator: ReportOrchestrator,
        userSettingsOrchestrator: UserSettingsOrchestrator,
        notificationsPermissionsOrchestrator: NotificationsPermissionsOrchestrator
    ): BaseLauncherViewModel {
        return if (BuildConfig.USE_RUST_DATA_LAYER) {
            RustLauncherViewModel(userSessionRepository)
        } else {
            LauncherViewModel(
                product,
                requiredAccountType,
                accountManager,
                authOrchestrator,
                plansOrchestrator,
                reportOrchestrator,
                userSettingsOrchestrator,
                notificationsPermissionsOrchestrator
            )
        }
    }
}
