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

package ch.protonmail.android.mailsession.data.mapper

import ch.protonmail.android.mailsession.domain.model.SessionError
import uniffi.proton_mail_uniffi.SessionError as LocalSessionError

fun LocalSessionError.toSessionError(): SessionError {
    return when (this) {
        LocalSessionError.DB -> SessionError.Local.DbError
        LocalSessionError.CRYPTO -> SessionError.Local.CryptoError
        LocalSessionError.KEY_CHAIN -> SessionError.Local.KeyChainError
        LocalSessionError.KEY_CHAIN_HAS_NO_KEY -> SessionError.Local.KeyChainHasNoKey
        LocalSessionError.HTTP -> SessionError.Remote.HttpError
        LocalSessionError.OTHER -> SessionError.Local.Unknown
    }
}
