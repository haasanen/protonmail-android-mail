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
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsLinkifyUrlsEnabled
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class GetMessageBodyWithClickableLinks @Inject constructor(
    private val getDecryptedMessageBody: GetDecryptedMessageBody,
    @IsLinkifyUrlsEnabled private val isLinkifyUrlEnabled: Flow<Boolean>
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
        if (!isLinkifyUrlEnabled.first()) {
            Timber.d("linkify-body: Feature flag Disabled! Returning standard body.")
            return@map decryptedBody
        }
        Timber.d("linkify-body: Feature flag enabled, proceeding to linkify...")
        val bodyWithClickableLinks = makeUrlsClickable(decryptedBody.value)
        decryptedBody.copy(value = bodyWithClickableLinks)
    }

    private fun makeUrlsClickable(value: String): String {
        Timber.d("linkify-body: processing body $value")
        val urlRegex = urlPattern.toRegex()
        Timber.d("linkify-body: using pattern: $urlPattern")

        val linkifiedBody = value.replace(urlRegex) { matchedUrl ->
            Timber.d("linkify-body: found url ${matchedUrl.value}")

            "<a href=\"${matchedUrl.value}\">${matchedUrl.value}</a>"
        }

        Timber.d("linkify-body: processed body $linkifiedBody")
        return linkifiedBody
    }


    companion object {

        /**
         * Exclude links that are already in the href of an <a> tag
         */
        private const val NEGATIVE_LOOKBACK = """(?<!href=")"""

        /**
         * Match the protocols
         * - http://
         * - https://
         * - https://www.
         * - www.
         */
        private const val PROTOCOL = """(https?://(www\.)?|www\.)"""

        /**
         * Match an alphanumeric host allowing hyphen and any number of subdomains
         */
        private const val HOST = """[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*"""

        /**
         * Match any alphanumeric top level domain 2 to 6 char long, eg.
         * .com
         * .ch
         * .me
         */
        private const val DOMAIN = """\.[a-zA-Z]{2,6}"""

        /**
         * Match any following chars (path) that is not an empty space
         */
        private const val PATH = """(/[^\s]*)?"""

        /**
         * Exclude links that are in the description of an <a> tag
         */
        private const val NEGATIVE_LOOKAHEAD = """(?![^<]*</a>)"""

        /**
         * Pattern to match urls in a block of text
         */
        private val urlPattern = StringBuilder()
            .append(NEGATIVE_LOOKBACK)
            .append(PROTOCOL)
            .append("(")
            .append(HOST)
            .append(DOMAIN)
            .append(")")
            .append(PATH)
            .append(NEGATIVE_LOOKAHEAD)
            .toString()
    }
}

