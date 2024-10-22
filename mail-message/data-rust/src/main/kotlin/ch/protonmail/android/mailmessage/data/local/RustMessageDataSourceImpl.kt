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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.datarust.mapper.LocalDecryptedMessage
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelAsAction
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.usecase.GetRustAllMessageBottomBarActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustAvailableMessageActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustMessageLabelAsActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustMessageMoveToActions
import ch.protonmail.android.mailmessage.data.usecase.GetRustSenderImage
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesRead
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesUnread
import ch.protonmail.android.mailmessage.data.usecase.RustStarMessages
import ch.protonmail.android.mailmessage.data.usecase.RustUnstarMessages
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.AllBottomBarMessageActions
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.MailboxException
import uniffi.proton_mail_uniffi.MessageAvailableActions
import uniffi.proton_mail_uniffi.MoveAction
import javax.inject.Inject

class RustMessageDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val rustMailbox: RustMailbox,
    private val rustMessageQuery: RustMessageQuery,
    private val createRustMessageAccessor: CreateRustMessageAccessor,
    private val createRustMessageBodyAccessor: CreateRustMessageBodyAccessor,
    private val getRustSenderImage: GetRustSenderImage,
    private val rustMarkMessagesRead: RustMarkMessagesRead,
    private val rustMarkMessagesUnread: RustMarkMessagesUnread,
    private val rustStarMessages: RustStarMessages,
    private val rustUnstarMessages: RustUnstarMessages,
    private val getRustAllMessageBottomBarActions: GetRustAllMessageBottomBarActions,
    private val getRustAvailableMessageActions: GetRustAvailableMessageActions,
    private val getRustMessageMoveToActions: GetRustMessageMoveToActions,
    private val getRustMessageLabelAsActions: GetRustMessageLabelAsActions
) : RustMessageDataSource {

    override suspend fun getMessage(userId: UserId, messageId: LocalMessageId): LocalMessageMetadata? {
        return try {
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-message: trying to load message with a null session")
                return null
            }
            createRustMessageAccessor(session, messageId)
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to get message")
            null
        }
    }

    override suspend fun getMessageBody(
        userId: UserId,
        messageId: LocalMessageId,
        labelId: LocalLabelId?
    ): LocalDecryptedMessage? {
        val mailboxFlow = if (labelId != null) {
            rustMailbox.observeMailbox(labelId)
        } else {
            rustMailbox.observeMailbox()
        }

        return try {
            mailboxFlow
                .mapLatest { mailbox ->
                    createRustMessageBodyAccessor(mailbox, messageId)
                }
                .firstOrNull()
        } catch (e: MailboxException) {
            Timber.e(e, "rust-message: Failed to get message body")
            null
        }
    }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata> {
        Timber.d("rust-message: getMessages for pageKey: $pageKey")
        return rustMessageQuery.getMessages(userId, pageKey) ?: run {
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
        return try {
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-message: trying to get sender image with a null session")
                return null
            }
            getRustSenderImage(session, address, bimi)
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to get sender image")
            null
        }
    }

    @MissingRustApi
    override suspend fun markRead(userId: UserId, messages: List<LocalMessageId>): Either<DataError.Local, Unit> {
        return try {
            val mailbox = rustMailbox.observeMailbox().firstOrNull()
            if (mailbox == null) {
                Timber.e("rust-message: trying to mark message read with a null mailbox")
                return DataError.Local.Unknown.left()
            }

            rustMarkMessagesRead(mailbox, messages).right()
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to mark message read")
            DataError.Local.Unknown.left()
        }
    }

    override suspend fun markUnread(userId: UserId, messages: List<LocalMessageId>): Either<DataError.Local, Unit> {
        return try {
            val mailbox = rustMailbox.observeMailbox().firstOrNull()
            if (mailbox == null) {
                Timber.e("rust-message: trying to mark unread with null Mailbox! failing")
                return DataError.Local.NoDataCached.left()
            }

            rustMarkMessagesUnread(mailbox, messages).right()
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to mark message unread")
            DataError.Local.Unknown.left()
        }
    }

    override suspend fun starMessages(userId: UserId, messages: List<LocalMessageId>): Either<DataError.Local, Unit> {
        val session = userSessionRepository.getUserSession(userId) ?: return DataError.Local.Unknown.left()
        return try {
            rustStarMessages(session, messages).right()
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to mark message unStarred")
            DataError.Local.Unknown.left()
        }
    }

    override suspend fun unStarMessages(userId: UserId, messages: List<LocalMessageId>): Either<DataError.Local, Unit> {
        val session = userSessionRepository.getUserSession(userId) ?: return DataError.Local.Unknown.left()
        return try {
            rustUnstarMessages(session, messages).right()
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-message: Failed to mark message unStarred")
            DataError.Local.Unknown.left()
        }
    }

    override suspend fun getAvailableActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): MessageAvailableActions? {
        val mailbox = rustMailbox.observeMailbox(labelId).firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get available actions for null Mailbox! failing")
            return null
        }
        return getRustAvailableMessageActions(mailbox, messageIds)
    }

    override suspend fun getAllAvailableBottomBarActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): Either<DataError.Local, AllBottomBarMessageActions> {
        val mailbox = rustMailbox.observeMailbox(labelId).firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get all available actions for null Mailbox! failing")
            return DataError.Local.NoDataCached.left()
        }
        return Either.catch {
            getRustAllMessageBottomBarActions(mailbox, messageIds)
        }.mapLeft {
            DataError.Local.Unknown
        }
    }

    override suspend fun getAvailableSystemMoveToActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): List<MoveAction.SystemFolder>? {
        val mailbox = rustMailbox.observeMailbox(labelId).firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get available actions for null Mailbox! failing")
            return null
        }
        val moveActions = getRustMessageMoveToActions(mailbox, messageIds)
        return moveActions.filterIsInstance<MoveAction.SystemFolder>()
    }

    override suspend fun getAvailableLabelAsActions(
        userId: UserId,
        labelId: LocalLabelId,
        messageIds: List<LocalMessageId>
    ): List<LocalLabelAsAction>? {
        val mailbox = rustMailbox.observeMailbox(labelId).firstOrNull()
        if (mailbox == null) {
            Timber.e("rust-message: trying to get available label actions for null Mailbox! failing")
            return null
        }
        return getRustMessageLabelAsActions(mailbox, messageIds)
    }
}
