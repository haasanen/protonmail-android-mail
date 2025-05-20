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
import arrow.core.flatMap
import arrow.core.left
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelAsAction
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.data.mapper.RemoteMessageId
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.mapper.toMessageBody
import ch.protonmail.android.mailmessage.data.mapper.toMessageId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.usecase.GetRustAllMessageBottomBarActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustAvailableMessageActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustMessageLabelAsActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustMessageMoveToActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustSenderImage
import ch.protonmail.android.mailmessage.data.usecase.RustDeleteMessages
import ch.protonmail.android.mailmessage.data.usecase.RustLabelMessages
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessageAsLegitimate
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesRead
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesUnread
import ch.protonmail.android.mailmessage.data.usecase.RustMoveMessages
import ch.protonmail.android.mailmessage.data.usecase.RustReportPhishing
import ch.protonmail.android.mailmessage.data.usecase.RustStarMessages
import ch.protonmail.android.mailmessage.data.usecase.RustUnblockAddress
import ch.protonmail.android.mailmessage.data.usecase.RustUnstarMessages
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_common.TransformOpts
import uniffi.proton_mail_uniffi.AllBottomBarMessageActions
import uniffi.proton_mail_uniffi.EmbeddedAttachmentInfo
import uniffi.proton_mail_uniffi.MessageAvailableActions
import uniffi.proton_mail_uniffi.MoveAction
import javax.inject.Inject

@SuppressWarnings("LongParameterList")
class RustMessageDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val rustMailboxFactory: RustMailboxFactory,
    private val rustMessageQuery: RustMessageQuery,
    private val createRustMessageAccessor: CreateRustMessageAccessor,
    private val createRustMessageBodyAccessor: CreateRustMessageBodyAccessor,
    private val getRustSenderImage: GetRustSenderImage,
    private val rustMarkMessagesRead: RustMarkMessagesRead,
    private val rustMarkMessagesUnread: RustMarkMessagesUnread,
    private val rustStarMessages: RustStarMessages,
    private val rustUnstarMessages: RustUnstarMessages,
    private val getRustAllMessageBottomBarActions: GetRustAllMessageBottomBarActions,
    private val rustDeleteMessages: RustDeleteMessages,
    private val rustMoveMessages: RustMoveMessages,
    private val rustLabelMessages: RustLabelMessages,
    private val getRustAvailableMessageActions: GetRustAvailableMessageActions,
    private val getRustMessageMoveToActions: GetRustMessageMoveToActions,
    private val getRustMessageLabelAsActions: GetRustMessageLabelAsActions,
    private val rustMarkMessageAsLegitimate: RustMarkMessageAsLegitimate,
    private val rustUnblockAddress: RustUnblockAddress,
    private val rustReportPhishing: RustReportPhishing
) : RustMessageDataSource {

    override suspend fun getMessage(
        userId: UserId,
        messageId: LocalMessageId
    ): Either<DataError, LocalMessageMetadata> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-message: trying to load message with a null session")
            return DataError.Local.NoUserSession.left()
        }

        return createRustMessageAccessor(session, messageId)
            .onLeft { Timber.e("rust-message: Failed to get message $it") }
    }

    override suspend fun getMessage(
        userId: UserId,
        messageId: RemoteMessageId
    ): Either<DataError, LocalMessageMetadata> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-message: trying to fetch remote message with a null session")
            return DataError.Local.NoUserSession.left()
        }

        return createRustMessageAccessor(session, messageId)
            .onLeft { Timber.e("rust-message: Failed to get remote message $it") }
    }

    override suspend fun getMessageBody(
        userId: UserId,
        messageId: LocalMessageId,
        transformations: MessageBodyTransformations
    ): Either<DataError, MessageBody> {
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to get the body and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull() ?: return DataError.Local.NoDataCached.left()

        return createRustMessageBodyAccessor(mailbox, messageId)
            .onLeft { Timber.e("rust-message: Failed to get message body $it") }
            .flatMap { decryptedMessage ->
                val transformOptions = TransformOpts(
                    showBlockQuote = transformations.showQuotedText,
                    hideRemoteImages = transformations.hideRemoteContent,
                    hideEmbeddedImages = transformations.hideEmbeddedImages
                )

                decryptedMessage.body(transformOptions).map { decryptedBody ->
                    decryptedBody.toMessageBody(messageId.toMessageId(), decryptedMessage.mimeType())
                }
            }
    }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata> {
        Timber.d("rust-message: getMessages for pageKey: $pageKey")
        val messages = rustMessageQuery.getMessages(userId, pageKey)
        Timber.d("rust-message: paginator returning messages ${messages?.joinToString { it.id.toString() }}")
        return messages ?: run {
            Timber.w("rust-message: paginator returned null result for $pageKey")
            emptyList()
        }
    }

    override suspend fun getSenderImage(
        userId: UserId,
        address: String,
        bimi: String?
    ): String? {
        Timber.d("rust-message: getSenderImage for address: $address")
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-message: trying to get sender image with a null session")
            return null
        }

        return getRustSenderImage(session, address, bimi)
            .onLeft { Timber.e("rust-message: Failed to get sender image $it") }
            .getOrNull()
    }

    @MissingRustApi
    override suspend fun markRead(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit> {
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to mark as read and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to mark message read with a null mailbox")
            return DataError.Local.Unknown.left()
        }

        Timber.v("rust-message: marking message as read...")
        return rustMarkMessagesRead(mailbox, messages)
            .onLeft { Timber.e("rust-message: Failed to mark message read $it") }
    }

    override suspend fun markUnread(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit> {
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to mark as unread and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to mark unread with null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }

        return rustMarkMessagesUnread(mailbox, messages)
            .onLeft { Timber.e("rust-message: Failed to mark message unread $it") }
    }

    override suspend fun starMessages(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit> {
        val session = userSessionRepository.getUserSession(userId) ?: return DataError.Local.NoUserSession.left()

        return rustStarMessages(session, messages)
            .onLeft { Timber.e("rust-message: Failed to mark message as starred $it") }
    }

    override suspend fun unStarMessages(userId: UserId, messages: List<LocalMessageId>): Either<DataError, Unit> {
        val session = userSessionRepository.getUserSession(userId) ?: return DataError.Local.NoUserSession.left()

        return rustUnstarMessages(session, messages)
            .onLeft { Timber.e("rust-message: Failed to mark message unStarred $it") }
    }

    override suspend fun moveMessages(
        userId: UserId,
        messageIds: List<LocalMessageId>,
        toLabelId: LocalLabelId
    ): Either<DataError, Unit> {
        val mailbox = rustMailboxFactory.create(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to move messages with null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }
        return rustMoveMessages(mailbox, toLabelId, messageIds)
            .onLeft { Timber.e("rust-message: Failed to move messages $it") }
    }

    override suspend fun getAvailableActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageId: LocalMessageId
    ): Either<DataError, MessageAvailableActions> {
        val mailbox = rustMailboxFactory.create(userId, labelId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get available actions for null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }
        return getRustAvailableMessageActions(mailbox, messageId)
    }

    override suspend fun getAllAvailableBottomBarActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError, AllBottomBarMessageActions> {
        val mailbox = rustMailboxFactory.create(userId, labelId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get all available actions for null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }

        return getRustAllMessageBottomBarActions(mailbox, messageIds)
    }

    override suspend fun getAvailableSystemMoveToActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError, List<MoveAction.SystemFolder>> {
        val mailbox = rustMailboxFactory.create(userId, labelId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get available actions for null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }
        val moveActions = getRustMessageMoveToActions(mailbox, messageIds)
        return moveActions.map { it.filterIsInstance<MoveAction.SystemFolder>() }
    }

    override suspend fun getAvailableLabelAsActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError, List<LocalLabelAsAction>> {
        val mailbox = rustMailboxFactory.create(userId, labelId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get available label actions for null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }
        return getRustMessageLabelAsActions(mailbox, messageIds)
    }

    override suspend fun deleteMessages(userId: UserId, messageIds: List<LocalMessageId>): Either<DataError, Unit> {
        Timber.v("rust-message: executing delete message for $messageIds")
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to delete messages and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to delete messages with null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }

        return rustDeleteMessages(mailbox, messageIds)
            .onLeft { Timber.e("rust-message: Failure deleting message on rust lib $it") }
    }

    override suspend fun labelMessages(
        userId: UserId,
        messageIds: List<LocalMessageId>,
        selectedLabelIds: List<LocalLabelId>,
        partiallySelectedLabelIds: List<LocalLabelId>,
        shouldArchive: Boolean
    ): Either<DataError, Unit> {
        Timber.v("rust-message: executing labels messages for $messageIds")
        val mailbox = rustMailboxFactory.create(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to label messages with null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }

        return rustLabelMessages(
            mailbox = mailbox,
            messageIds = messageIds,
            selectedLabelIds = selectedLabelIds,
            partiallySelectedLabelIds = partiallySelectedLabelIds,
            shouldArchive = shouldArchive
        )
    }

    override suspend fun getEmbeddedImage(
        userId: UserId,
        messageId: LocalMessageId,
        contentId: String
    ): Either<DataError, EmbeddedAttachmentInfo> {
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to get the body and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull() ?: return DataError.Local.NoDataCached.left()

        return createRustMessageBodyAccessor(mailbox, messageId)
            .onLeft { Timber.e("rust-message: Failed to build message body accessor $it") }
            .flatMap { decryptedMessage ->
                decryptedMessage.getEmbeddedAttachment(contentId)
            }
    }

    override suspend fun markMessageAsLegitimate(userId: UserId, messageId: LocalMessageId): Either<DataError, Unit> {
        val mailbox = rustMailboxFactory.create(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to mark message as legitimate with null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }

        return rustMarkMessageAsLegitimate(mailbox, messageId)
    }

    override suspend fun unblockSender(userId: UserId, email: String): Either<DataError, Unit> {
        val mailbox = rustMailboxFactory.create(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to unblock sender with null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }

        return rustUnblockAddress(mailbox, email)
    }

    override suspend fun reportPhishing(userId: UserId, messageId: LocalMessageId): Either<DataError, Unit> {
        val mailbox = rustMailboxFactory.create(userId).getOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to report phishing with null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }

        return rustReportPhishing(mailbox, messageId)
    }
}
