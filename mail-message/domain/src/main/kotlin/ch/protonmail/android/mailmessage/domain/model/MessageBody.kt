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

data class MessageBody(
    val messageId: MessageId,
    val body: String,
    val hasQuotedText: Boolean,
    val banners: List<MessageBanner>,
    val mimeType: MimeType
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
    object BlockedSender : MessageBanner
    object PhishingAttempt : MessageBanner
    object Spam : MessageBanner

    data class Expiry(val timestamp: Long) : MessageBanner
    data class AutoDelete(val timestamp: Long, val deleteDays: Int) : MessageBanner

    object UnsubscribeNewsletter : MessageBanner

    data class ScheduledSend(val timestamp: Long) : MessageBanner
    data class Snoozed(val timestamp: Long) : MessageBanner

    object EmbeddedImages : MessageBanner
    object RemoteContent : MessageBanner
}
