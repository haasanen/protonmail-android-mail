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

package ch.protonmail.android.mailbugreport

import ch.protonmail.android.mailbugreport.data.LogsFileHandlerImpl
import ch.protonmail.android.mailbugreport.data.RustLogsFileHandlerImpl
import ch.protonmail.android.mailbugreport.data.local.RustBugReportDataSource
import ch.protonmail.android.mailbugreport.data.local.RustBugReportDataSourceImpl
import ch.protonmail.android.mailbugreport.data.provider.LogcatProviderImpl
import ch.protonmail.android.mailbugreport.data.repository.BugReportRepositoryImpl
import ch.protonmail.android.mailbugreport.domain.LogsExportFeatureSetting
import ch.protonmail.android.mailbugreport.domain.LogsFileHandler
import ch.protonmail.android.mailbugreport.domain.annotations.AppLogsFileHandler
import ch.protonmail.android.mailbugreport.domain.annotations.LogsExportFeatureSettingValue
import ch.protonmail.android.mailbugreport.domain.annotations.RustLogsFileHandler
import ch.protonmail.android.mailbugreport.domain.provider.LogcatProvider
import ch.protonmail.android.mailbugreport.domain.repository.BugReportRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MailReportModule {

    @Provides
    @Singleton
    @LogsExportFeatureSettingValue
    fun provideLogsExporting() = LogsExportFeatureSetting(enabled = true, internalEnabled = true)

    @Module
    @InstallIn(SingletonComponent::class)
    internal interface BindsModule {

        @Binds
        @Reusable
        fun provideBugReportRepository(impl: BugReportRepositoryImpl): BugReportRepository

        @Binds
        @Reusable
        fun provideBugReportDataSource(impl: RustBugReportDataSourceImpl): RustBugReportDataSource

        @Binds
        @Reusable
        fun provideLogcatProvider(impl: LogcatProviderImpl): LogcatProvider

        @Binds
        @Singleton
        @AppLogsFileHandler
        fun provideAppLogsFileHandler(impl: LogsFileHandlerImpl): LogsFileHandler

        @Binds
        @Singleton
        @RustLogsFileHandler
        fun provideRustLogsFileHandler(impl: RustLogsFileHandlerImpl): LogsFileHandler
    }
}
