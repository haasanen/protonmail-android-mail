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
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetMessageBodyWithClickableLinks @Inject constructor(
    private val getDecryptedMessageBody: GetDecryptedMessageBody
) {

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        transformations: MessageBodyTransformations = MessageBodyTransformations.MessageDetailsDefaults
    ): Either<GetMessageBodyError, DecryptedMessageBody> = getDecryptedMessageBody(
        userId,
        messageId,
        transformations
    ).map { decryptedBody ->
        val bodyWithClickableLinks = makeUrlsClickable(decryptedBody.value)
        decryptedBody.copy(value = bodyWithClickableLinks)
    }

    private fun makeUrlsClickable(value: String): String {
        val urlRegex = URL_REGEX.toRegex()

        val linkifiedBody = value.replace(urlRegex) { matchedUrl ->

            val linkifiedUrl = "<a href=\"${matchedUrl.value}\">${matchedUrl.value}</a>"
            println("linkify-body: found url ${matchedUrl.value}")
            linkifiedUrl
        }

        return linkifiedBody
    }

}

private const val URL_REGEX = """(https?://)?(www\.)?([a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z]{2,6})(/[^\s]*)?"""

