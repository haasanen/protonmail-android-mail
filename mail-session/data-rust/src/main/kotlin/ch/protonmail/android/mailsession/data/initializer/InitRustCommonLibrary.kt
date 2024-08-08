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

package ch.protonmail.android.mailsession.data.initializer

import android.content.Context
import ch.protonmail.android.mailsession.data.keychain.OsKeyChainMock
import ch.protonmail.android.mailsession.data.model.RustLibConfigParams
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.network.data.di.BaseProtonApiUrl
import okhttp3.HttpUrl
import timber.log.Timber
import uniffi.proton_api_core.ApiEnvConfig
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionParams
import javax.inject.Inject

class InitRustCommonLibrary @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mailSessionRepository: MailSessionRepository,
    @BaseProtonApiUrl private val baseApiUrl: HttpUrl
) {

    fun init(config: RustLibConfigParams) {
        Timber.v("rust-session: Let the rust begin...")

        val skipSrpProofValidation = isRunningAgainstMockWebserver(baseApiUrl)
        val allowInsecureNetworking = isRunningAgainstMockWebserver(baseApiUrl)
        val sessionParams = MailSessionParams(
            context.filesDir.absolutePath,
            context.filesDir.absolutePath,
            context.cacheDir.absolutePath,
            context.filesDir.absolutePath,
            config.isDebug,
            ApiEnvConfig(
                config.appVersion,
                baseApiUrl.toString().removeSuffix("/"),
                config.userAgent,
                allowInsecureNetworking,
                skipSrpProofValidation
            )
        )
        Timber.d("rust-session: Initializing the Rust Lib with $sessionParams")

        val mailSession = MailSession.create(
            sessionParams,
            OsKeyChainMock(context),
            null
        )
        Timber.v("rust-session: Mail session created! (hash: ${mailSession.hashCode()})")
        Timber.v("rust-session: Storing mail session to In Memory Session Repository...")

        mailSessionRepository.setMailSession(mailSession)
    }

    private fun isRunningAgainstMockWebserver(baseApiUrl: HttpUrl) = baseApiUrl.host == "localhost"
}
