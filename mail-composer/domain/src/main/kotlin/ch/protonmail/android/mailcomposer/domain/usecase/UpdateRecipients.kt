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

package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.model.Recipient
import timber.log.Timber

abstract class UpdateRecipients {

    suspend operator fun invoke(
        currentRecipients: List<Recipient>,
        updatedRecipients: List<Recipient>
    ): Either<DataError, Unit> = either {
        val recipientsToAdd = updatedRecipients.filterNot { it in currentRecipients }
        val recipientsToRemove = currentRecipients.filterNot { it in updatedRecipients }

        Timber.d("draft-recipients: current recipients ${currentRecipients.map { it.address }}")
        Timber.d("draft-recipients: updated recipients ${updatedRecipients.map { it.address }}")

        recipientsToAdd.forEach { addRecipient ->
            Timber.d("draft-recipients: adding $addRecipient")
            save(addRecipient)
                .onLeft { raise(it) }
        }

        recipientsToRemove.forEach { removeRecipient ->
            Timber.d("draft-recipients: removing $removeRecipient")
            remove(removeRecipient)
                .onLeft { raise(it) }
        }
    }

    internal abstract suspend fun save(recipient: Recipient): Either<DataError, Unit>

    internal abstract suspend fun remove(recipient: Recipient): Either<DataError, Unit>

}
