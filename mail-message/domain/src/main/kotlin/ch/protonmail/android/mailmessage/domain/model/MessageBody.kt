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

package ch.protonmail.android.mailmessage.domain.model

import kotlin.time.Instant

data class MessageBody(
    val messageId: MessageId,
    val body: String,
    val hasQuotedText: Boolean,
    val banners: List<MessageBanner>,
    val mimeType: MimeType,
    val transformations: MessageBodyTransformations
)

enum class MimeType(val value: String) {
    PlainText("text/plain"),
    Html("text/html"),
    MultipartMixed("multipart/mixed");

    companion object {

        fun from(value: String) = entries.find { it.value == value } ?: PlainText
    }
}

sealed interface MessageBanner {
    data object BlockedSender : MessageBanner
    data object PhishingAttempt : MessageBanner
    data object Spam : MessageBanner

    data class Expiry(val expiresAt: Instant) : MessageBanner
    data class AutoDelete(val deletesAt: Instant) : MessageBanner

    data object UnsubscribeNewsletter : MessageBanner

    data class ScheduledSend(val scheduledAt: Instant) : MessageBanner
    data class Snoozed(val snoozedUntil: Instant) : MessageBanner

    data object EmbeddedImages : MessageBanner
    data object RemoteContent : MessageBanner
}
