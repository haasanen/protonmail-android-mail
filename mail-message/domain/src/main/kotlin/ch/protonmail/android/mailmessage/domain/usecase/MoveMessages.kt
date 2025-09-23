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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.FindLocalSystemLabelId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class MoveMessages @Inject constructor(
    private val messageRepository: MessageRepository,
    private val findLocalSystemLabelId: FindLocalSystemLabelId
) {

    suspend operator fun invoke(
        userId: UserId,
        messageIds: List<MessageId>,
        labelId: LabelId
    ): Either<DataError, Unit> = messageRepository.moveTo(userId, messageIds, labelId)

    suspend operator fun invoke(
        userId: UserId,
        messageIds: List<MessageId>,
        systemLabelId: SystemLabelId
    ): Either<DataError, Unit> = either {
        val localLabelId = findLocalSystemLabelId(userId, systemLabelId)?.labelId
        if (localLabelId == null) {
            Timber.e("move-message: Local label id cannot be found for SystemLabelId $systemLabelId")
            return DataError.Local.IllegalStateError.left()
        }

        messageRepository.moveTo(userId, messageIds, localLabelId).bind()
    }

}
