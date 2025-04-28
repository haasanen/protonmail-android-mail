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

package ch.protonmail.android.mailmessage.data.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalMimeType
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import timber.log.Timber
import uniffi.proton_mail_common.BodyOutput
import uniffi.proton_mail_common.TransformOpts
import uniffi.proton_mail_uniffi.BodyOutputResult
import uniffi.proton_mail_uniffi.DecryptedMessage
import uniffi.proton_mail_uniffi.EmbeddedAttachmentInfo
import uniffi.proton_mail_uniffi.EmbeddedAttachmentInfoResult

class DecryptedMessageWrapper(private val decryptedMessage: DecryptedMessage) {

    suspend fun body(transformOpts: TransformOpts): Either<DataError, BodyOutput> =
        when (val result = decryptedMessage.body(transformOpts)) {
            is BodyOutputResult.Error -> result.v1.toDataError().left()
            is BodyOutputResult.Ok -> result.v1.right()
        }

    suspend fun getEmbeddedAttachment(contentId: String): Either<DataError, EmbeddedAttachmentInfo> =
        when (val result = decryptedMessage.getEmbeddedAttachment(contentId)) {
            is EmbeddedAttachmentInfoResult.Error -> {
                Timber.d("DecryptedMessageWrapper: Failed to load image: $contentId: ${result.v1}")
                result.v1.toDataError().left()
            }
            is EmbeddedAttachmentInfoResult.Ok -> result.v1.right()
        }

    fun mimeType(): LocalMimeType = decryptedMessage.mimeType()
}
