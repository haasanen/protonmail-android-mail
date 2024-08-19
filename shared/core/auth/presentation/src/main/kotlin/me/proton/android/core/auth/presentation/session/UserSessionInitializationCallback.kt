/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.auth.presentation.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import me.proton.android.core.auth.presentation.LogTag
import me.proton.core.util.kotlin.CoreLogger
import uniffi.proton_mail_uniffi.MailUserSessionInitializationCallback
import uniffi.proton_mail_uniffi.MailUserSessionInitializationStage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionInitializationCallback @Inject constructor() : MailUserSessionInitializationCallback {

    private val mutableStage = MutableStateFlow<MailUserSessionInitializationStage?>(null)

    suspend fun waitFinished() = mutableStage
        .filter { it == MailUserSessionInitializationStage.FINISHED }
        .first()

    override fun onStage(stage: MailUserSessionInitializationStage) {
        mutableStage.value = stage
        CoreLogger.v(LogTag.SESSION, "rust-session: rust-session onStage: $stage")
    }
}
