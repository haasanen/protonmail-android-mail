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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalAutoLockPin
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.domain.model.autolock.SetAutoLockPinError
import timber.log.Timber
import uniffi.proton_mail_uniffi.PinSetError
import uniffi.proton_mail_uniffi.PinSetErrorReason

fun AutoLockPin.toLocalAutoLockPin(): Either<DataError, LocalAutoLockPin> {
    val digitsOnly = value.filter { it.isDigit() }

    return if (digitsOnly.isNotEmpty()) {
        digitsOnly.map { it.digitToInt().toUInt() }.right()
    } else {
        Timber.e("AutoLockPin contains no digits: $value")
        DataError.Local.TypeConversionError.left()
    }
}

fun PinSetError.toAutoLockPinError(): SetAutoLockPinError {
    return when (this) {
        is PinSetError.Reason -> when (v1) {
            PinSetErrorReason.TOO_SHORT -> SetAutoLockPinError.PinIsTooShort
            PinSetErrorReason.TOO_LONG -> SetAutoLockPinError.PinIsTooLong
            PinSetErrorReason.MALFORMED -> SetAutoLockPinError.PinIsMalformed
        }

        is PinSetError.Other -> SetAutoLockPinError.Other(v1.toDataError())
    }
}
