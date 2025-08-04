/*
 * Copyright (c) 2025 Proton Technologies AG
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
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpAnswer
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpEvent
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.local.RustMailboxFactory
import ch.protonmail.android.mailmessage.data.usecase.CreateRustEventServiceAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustEventServiceProviderAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.wrapper.DecryptedMessageWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustRsvpEventDataSourceImpl @Inject constructor(
    private val createRustMessageBodyAccessor: CreateRustMessageBodyAccessor,
    private val createRustEventServiceProviderAccessor: CreateRustEventServiceProviderAccessor,
    private val createRustEventServiceAccessor: CreateRustEventServiceAccessor,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val rustMailboxFactory: RustMailboxFactory
) : RustRsvpEventDataSource {

    override suspend fun identifyRsvp(userId: UserId, messageId: LocalMessageId): Either<DataError, Boolean> =
        withContext(ioDispatcher) {
            return@withContext getDecryptedMessageWrapper(userId, messageId)
                .flatMap { decryptedMessage ->
                    (createRustEventServiceProviderAccessor(decryptedMessage, messageId) != null).right()
                }
        }

    override suspend fun getRsvpEvent(userId: UserId, messageId: LocalMessageId): Either<DataError, LocalRsvpEvent> =
        withContext(ioDispatcher) {
            return@withContext getDecryptedMessageWrapper(userId, messageId)
                .flatMap { decryptedMessage ->
                    createRustEventServiceProviderAccessor(decryptedMessage, messageId)?.let {
                        createRustEventServiceAccessor(it, messageId)?.get()
                    } ?: return@withContext DataError.Local.NoDataCached.left()
                }
        }

    override suspend fun answerRsvpEvent(
        userId: UserId,
        messageId: LocalMessageId,
        answer: LocalRsvpAnswer
    ): Either<DataError, Unit> = withContext(ioDispatcher) {
        return@withContext getDecryptedMessageWrapper(userId, messageId)
            .flatMap { decryptedMessage ->
                createRustEventServiceProviderAccessor(decryptedMessage, messageId)?.let {
                    createRustEventServiceAccessor(it, messageId)?.answer(answer)
                } ?: return@withContext DataError.Local.NoDataCached.left()
            }
    }

    private suspend fun getDecryptedMessageWrapper(
        userId: UserId,
        messageId: LocalMessageId
    ): Either<DataError, DecryptedMessageWrapper> {
        // Hardcoded rust mailbox to "AllMail" to avoid this method having labelId as param;
        // the current labelId is not needed to get the body and is planned to be dropped on this API
        val mailbox = rustMailboxFactory.createAllMail(userId).getOrNull()
            ?: return DataError.Local.NoDataCached.left()

        return createRustMessageBodyAccessor(mailbox, messageId)
            .onLeft { Timber.e("rust-rsvp: Failed to get message body $it") }
    }
}
