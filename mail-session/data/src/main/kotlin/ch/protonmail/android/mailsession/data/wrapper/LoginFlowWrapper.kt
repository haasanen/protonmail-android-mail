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

package ch.protonmail.android.mailsession.data.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailsession.domain.model.LoginError
import ch.protonmail.android.mailsession.domain.model.toLoginError
import uniffi.proton_mail_uniffi.LoginFlow
import uniffi.proton_mail_uniffi.LoginFlowMigrateResult
import uniffi.proton_account_uniffi.MigrationData

class LoginFlowWrapper(private val loginFlow: LoginFlow) {
    suspend fun migrate(data: MigrationData): Either<LoginError, Unit> = when (val result = loginFlow.migrate(data)) {
        is LoginFlowMigrateResult.Error -> result.v1.toLoginError().left()
        is LoginFlowMigrateResult.Ok -> Unit.right()
    }
}
