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
import ch.protonmail.android.mailmessage.domain.model.AttachmentDataError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class LoadMessageBodyImage @Inject constructor(
    private val messageBodyRepository: MessageBodyRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        url: String,
        shouldLoadImagesSafely: Boolean
    ): Either<AttachmentDataError, MessageBodyImage> =
        messageBodyRepository.loadImage(userId, messageId, url, shouldLoadImagesSafely)

}
