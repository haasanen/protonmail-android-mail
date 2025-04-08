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

package ch.protonmail.android.composer.data.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import uniffi.proton_mail_uniffi.AddSingleRecipientError
import uniffi.proton_mail_uniffi.ComposerRecipient
import uniffi.proton_mail_uniffi.ComposerRecipientList
import uniffi.proton_mail_uniffi.ComposerRecipientValidationCallback
import uniffi.proton_mail_uniffi.RemoveRecipientError
import uniffi.proton_mail_uniffi.SingleRecipientEntry

class ComposerRecipientListWrapper(private val rustRecipients: ComposerRecipientList) {

    fun recipients(): List<ComposerRecipient> = rustRecipients.recipients()

    fun registerCallback(callback: ComposerRecipientValidationCallback) = rustRecipients.setCallback(callback)

    fun addSingleRecipient(recipient: SingleRecipientEntry): Either<DataError, Unit> =
        when (rustRecipients.addSingleRecipient(recipient)) {
            AddSingleRecipientError.OK -> Unit.right()
            AddSingleRecipientError.DUPLICATE -> DataError.Local.SaveDraftError.DuplicateRecipient.left()
            AddSingleRecipientError.SAVE_FAILED -> DataError.Local.SaveDraftError.SaveFailed.left()
        }

    fun removeSingleRecipient(recipient: SingleRecipientEntry): Either<DataError, Unit> =
        when (rustRecipients.removeSingleRecipient(recipient.email)) {
            RemoveRecipientError.OK -> Unit.right()
            RemoveRecipientError.EMPTY_GROUP_NAME -> DataError.Local.SaveDraftError.EmptyRecipientGroupName.left()
            RemoveRecipientError.SAVE_FAILED -> DataError.Local.SaveDraftError.SaveFailed.left()
        }
}
