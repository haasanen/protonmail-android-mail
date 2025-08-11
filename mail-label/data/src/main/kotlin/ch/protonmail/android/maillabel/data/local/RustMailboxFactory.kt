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

package ch.protonmail.android.maillabel.data.local

import java.util.concurrent.ConcurrentHashMap
import arrow.core.Either
import arrow.core.flatten
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.data.usecase.CreateAllMailMailbox
import ch.protonmail.android.maillabel.data.usecase.CreateMailbox
import ch.protonmail.android.maillabel.data.wrapper.MailboxWrapper
import ch.protonmail.android.maillabel.domain.usecase.GetSelectedMailLabelId
import ch.protonmail.android.mailsession.data.usecase.ExecuteWithUserSession
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustMailboxFactory @Inject constructor(
    private val executeWithUserSession: ExecuteWithUserSession,
    private val createMailbox: CreateMailbox,
    private val createAllMailMailbox: CreateAllMailMailbox,
    private val getSelectedMailLabelId: GetSelectedMailLabelId
) {

    private val mailboxCache = ConcurrentHashMap<MailboxCacheKey, MailboxWrapper>()

    @Deprecated("Error prone due to using selectedMailLabel; To be dropped after ET-1739")
    suspend fun create(userId: UserId): Either<DataError, MailboxWrapper> {
        val currentLabelId = getSelectedMailLabelId().labelId.toLocalLabelId()
        val mailboxCacheKey = MailboxCacheKey(userId, currentLabelId)

        mailboxCache[mailboxCacheKey]?.let {
            Timber.d("rust-mailbox-factory: (deprecated) Cache hit for user: $userId, label: $currentLabelId")
            return it.right()
        }

        return executeWithUserSession(userId) { session -> createMailbox(session, currentLabelId) }
            .flatten()
            .onRight {
                Timber.d("rust-mailbox-factory: (deprecated) Mailbox created for user: $userId, label: $currentLabelId")
                mailboxCache[mailboxCacheKey] = it
            }
    }

    suspend fun create(userId: UserId, labelId: LocalLabelId): Either<DataError, MailboxWrapper> {
        val mailboxCacheKey = MailboxCacheKey(userId, labelId)

        mailboxCache[mailboxCacheKey]?.let {
            Timber.d("rust-mailbox-factory: Cache hit for user: $userId, label: $labelId")
            return it.right()
        }

        return executeWithUserSession(userId) { session ->
            createMailbox(session, labelId)
        }
            .flatten()
            .onRight { mailbox ->
                Timber.d("rust-mailbox-factory: Mailbox created for user: $userId, label: $labelId")
                mailboxCache[mailboxCacheKey] = mailbox
            }
    }

    suspend fun createSkipCache(userId: UserId, labelId: LocalLabelId): Either<DataError, MailboxWrapper> {
        return executeWithUserSession(userId) { session ->
            createMailbox(session, labelId)
        }.flatten()
    }

    suspend fun createAllMail(userId: UserId): Either<DataError, MailboxWrapper> {
        return executeWithUserSession(userId) { session -> createAllMailMailbox(session) }
            .flatten()
            .onRight { Timber.d("rust-mailbox-factory: Mailbox created for all mail label") }
    }
}

private data class MailboxCacheKey(
    val userId: UserId,
    val labelId: LocalLabelId
)
