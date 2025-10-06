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
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.startsWith
import timber.log.Timber
import javax.inject.Inject

class GetMessageBodyWithClickableLinks @Inject constructor(
    private val getDecryptedMessageBody: GetDecryptedMessageBody,
    @IsLinkifyUrlsEnabled private val isLinkifyUrlEnabled: FeatureFlag<Boolean>
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
        if (!isLinkifyUrlEnabled.get()) {
            Timber.d("linkify-body: Feature flag Disabled! Returning standard body.")
            return@map decryptedBody
        }
        Timber.d("linkify-body: Feature flag enabled, proceeding to linkify...")
        val bodyWithClickableLinks = makeUrlsClickable(decryptedBody.value)
        decryptedBody.copy(value = bodyWithClickableLinks)
    }

    private fun makeUrlsClickable(value: String): String {
        val urlRegex = URL_PATTERN.toRegex()
        Timber.d("linkify-body: using pattern: $URL_PATTERN")

        val linkifiedBody = value.replace(urlRegex) { matchedUrl ->

            val matchedUrlWithProtocol = prependHttpsWhenMissing(matchedUrl.value)
            "<a href=\"$matchedUrlWithProtocol\">${matchedUrl.value}</a>"
        }

        return linkifiedBody
    }

    private fun prependHttpsWhenMissing(url: String): String = when {
        url.startsWith("http") -> url
        else -> "https://$url"
    }


    companion object {

        /**
         * Look back matcher for '="' to hit any attribute eg. 'href="', 'xmlns="'
         */
        private const val LOOKBACK_ATTRIBUTE_MATCHER = """=""""

        /**
         * Look back matcher for '="' to hit any attribute eg. 'href="', 'xmlns="'
         */
        private const val LOOKBACK_SPACED_ATTRIBUTE_MATCHER = """="\s"""

        /**
         * Look back matcher for remote css stylesheet import
         */
        private const val LOOKBACK_CSS_IMPORT_URL_MATCHER = """@import url\("""

        /**
         * Look back matcher for remote background image
         */
        private const val LOOKBACK_BACKGROUND_IMAGE_URL_MATCHER = """background-image: url\('"""

        /**
         * Look back to exclude links that are already in any html tag
         */
        private val NEGATIVE_LOOKBACK = StringBuilder()
            .append("(?<!")
            .append(LOOKBACK_ATTRIBUTE_MATCHER)
            .append("|")
            .append(LOOKBACK_SPACED_ATTRIBUTE_MATCHER)
            .append("|")
            .append(LOOKBACK_CSS_IMPORT_URL_MATCHER)
            .append("|")
            .append(LOOKBACK_BACKGROUND_IMAGE_URL_MATCHER)
            .append(")")
            .toString()

        /**
         * Allow matching urls only if they are at beginning of the line,
         * after a blank space, following a <pre> tag, following a <br> tag
         */
        private const val POSITIVE_LOOKBACK = """(?<=(^|\s|<pre>|<br>|<body>|<div>|<span>))"""

        /**
         * Match the https protocol or a www identifier
         * http://, https://, https://www., www.
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
        private const val PATH = """(/[^\s<>]*)?"""

        /**
         * Exclude links that are in the description of an <a> tag
         */
        private const val NEGATIVE_LOOKAHEAD = """(?![^<]*</a>)"""

        /**
         * Pattern to match urls in a block of text
         */
        private val URL_PATTERN = StringBuilder()
            .append(NEGATIVE_LOOKBACK)
            .append(POSITIVE_LOOKBACK)
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

