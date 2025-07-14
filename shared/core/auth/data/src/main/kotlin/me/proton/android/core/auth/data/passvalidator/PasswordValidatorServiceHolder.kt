/*
 * Copyright (c) 2025 Proton Technologies AG
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

package me.proton.android.core.auth.data.passvalidator

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.proton.android.core.auth.data.LogTag
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.DispatcherProvider
import uniffi.proton_account_uniffi.PasswordValidatorService
import javax.inject.Inject

@ActivityRetainedScoped
class PasswordValidatorServiceHolder @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {

    private val mutex = Mutex()
    private lateinit var passwordValidatorService: PasswordValidatorService

    suspend fun bind(provider: suspend () -> PasswordValidatorService) {
        mutex.withLock {
            check(!this::passwordValidatorService.isInitialized) {
                "PasswordValidatorService is already initialized."
            }
            runCatching {
                passwordValidatorService = provider()
            }.onFailure {
                CoreLogger.e(LogTag.DEFAULT, it, "Failed to initialize password validator service.")
            }.getOrNull()
        }

        withContext(dispatcherProvider.Io) {
            runCatching {
                passwordValidatorService.fetchValidators()
            }.onFailure {
                CoreLogger.e(LogTag.DEFAULT, it, "Failed to fetch password validators.")
            }
        }
    }

    suspend fun get(): PasswordValidatorService = mutex.withLock {
        passwordValidatorService
    }
}
