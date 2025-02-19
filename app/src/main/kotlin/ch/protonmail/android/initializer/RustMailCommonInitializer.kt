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

package ch.protonmail.android.initializer

import android.content.Context
import androidx.startup.Initializer
import ch.protonmail.android.BuildConfig
import ch.protonmail.android.di.RustMailCommonEntryPoint
import ch.protonmail.android.mailsession.data.model.RustLibConfigParams
import ch.protonmail.android.useragent.BuildUserAgent
import ch.protonmail.android.useragent.GetAndroidVersion
import ch.protonmail.android.useragent.GetAppVersion
import ch.protonmail.android.useragent.GetDeviceData
import dagger.hilt.android.EntryPointAccessors

class RustMailCommonInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val rustLibConfig = buildRustConfigParams()

        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RustMailCommonEntryPoint::class.java
        ).initRustCommonLibrary().init(rustLibConfig)
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = listOf()

    private fun buildRustConfigParams(): RustLibConfigParams {
        val getAppVersion = GetAppVersion()
        val getUserAgent = BuildUserAgent(getAppVersion, GetAndroidVersion(), GetDeviceData())

        val rustLibConfig = RustLibConfigParams(
            isDebug = BuildConfig.DEBUG,
            appVersion = "android-mail@${BuildConfig.VERSION_NAME}",
            userAgent = getUserAgent()
        )
        return rustLibConfig
    }

}
