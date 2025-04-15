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

package ch.protonmail.android.mailmessage.data.local

import arrow.core.Either
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelAsAction
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.datarust.mapper.RemoteMessageId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailpagination.domain.model.PageKey
import me.proton.core.domain.entity.UserId
import uniffi.proton_mail_uniffi.AllBottomBarMessageActions
import uniffi.proton_mail_uniffi.EmbeddedAttachmentInfo
import uniffi.proton_mail_uniffi.MessageAvailableActions
import uniffi.proton_mail_uniffi.MoveAction

interface RustMessageDataSource {

    suspend fun getMessage(userId: UserId, messageId: LocalMessageId): Either<DataError, LocalMessageMetadata>
    suspend fun getMessage(userId: UserId, messageId: RemoteMessageId): Either<DataError, LocalMessageMetadata>

    suspend fun getMessageBody(
        userId: UserId,
        messageId: LocalMessageId,
        transformations: MessageBodyTransformations
    ): Either<DataError, MessageBody>

    suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata>

    suspend fun getSenderImage(
        userId: UserId,
        address: String,
        bimi: String?
    ): String?

    suspend fun markRead(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit>
    suspend fun markUnread(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit>

    suspend fun starMessages(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit>
    suspend fun unStarMessages(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit>

    suspend fun moveMessages(
        userId: UserId,
        messageIds: List<LocalMessageId>,
        toLabelId: LocalLabelId
    ): Either<DataError, Unit>

    suspend fun getAvailableActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError, MessageAvailableActions>

    suspend fun getAvailableSystemMoveToActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError, List<MoveAction.SystemFolder>>

    suspend fun getAvailableLabelAsActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError, List<LocalLabelAsAction>>

    suspend fun getAllAvailableBottomBarActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError, AllBottomBarMessageActions>

    suspend fun deleteMessages(userId: UserId, messageIds: List<LocalMessageId>): Either<DataError, Unit>

    suspend fun labelMessages(
        userId: UserId,
        messageIds: List<LocalMessageId>,
        selectedLabelIds: List<LocalLabelId>,
        partiallySelectedLabelIds: List<LocalLabelId>,
        shouldArchive: Boolean
    ): Either<DataError, Unit>

    suspend fun getEmbeddedImage(
        userId: UserId,
        messageId: LocalMessageId,
        contentId: String
    ): Either<DataError, EmbeddedAttachmentInfo>
}
