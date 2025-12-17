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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RustMailboxFactory @Inject constructor(
    private val executeWithUserSession: ExecuteWithUserSession,
    private val createMailbox: CreateMailbox,
    private val createAllMailMailbox: CreateAllMailMailbox,
    private val getSelectedMailLabelId: GetSelectedMailLabelId
) {

    private val mailboxCache = ConcurrentHashMap<MailboxCacheKey, MailboxWrapper>()
    private val allMailCache = ConcurrentHashMap<UserId, MailboxWrapper>()

    private val mailboxMutexes = ConcurrentHashMap<MailboxCacheKey, Mutex>()
    private val allMailMutexes = ConcurrentHashMap<UserId, Mutex>()

    @Deprecated("Error prone due to using selectedMailLabel; To be dropped after ET-1739")
    suspend fun create(userId: UserId): Either<DataError, MailboxWrapper> {
        val currentLabelId = getSelectedMailLabelId().labelId.toLocalLabelId()
        val mailboxCacheKey = MailboxCacheKey(userId, currentLabelId)

        Timber.d("rust-mailbox-factory: (deprecated) Looking up cache for user: $userId, label: $currentLabelId")

        mailboxCache[mailboxCacheKey]?.let {
            return it.right()
        }

        val mutex = mailboxMutexes.computeIfAbsent(mailboxCacheKey) { Mutex() }

        return mutex.withLock {
            // Double check: if we missed the cache earlier, it should be hit now if the value is present.
            mailboxCache[mailboxCacheKey]?.let {
                return it.right()
            }

            executeWithUserSession(userId) { session ->
                createMailbox(session, currentLabelId)
            }
                .flatten()
                .onRight { mailbox ->
                    Timber.d("rust-mailbox-factory: Mailbox created for user: $userId, label: $currentLabelId")
                    mailboxCache[mailboxCacheKey] = mailbox
                }
        }
    }

    suspend fun create(userId: UserId, labelId: LocalLabelId): Either<DataError, MailboxWrapper> {
        val mailboxCacheKey = MailboxCacheKey(userId, labelId)

        Timber.d("rust-mailbox-factory: Looking up cache for user: $userId, label: $labelId")

        mailboxCache[mailboxCacheKey]?.let {
            return it.right()
        }

        val mutex = mailboxMutexes.computeIfAbsent(mailboxCacheKey) { Mutex() }
        return mutex.withLock {
            // Double check: if we missed the cache earlier, it should be hit now if the value is present.
            mailboxCache[mailboxCacheKey]?.let {
                return it.right()
            }

            Timber.d("rust-mailbox-factory: start creating $labelId mailbox for user $userId")
            executeWithUserSession(userId) { session ->
                createMailbox(session, labelId)
            }
                .flatten()
                .onRight { mailbox ->
                    Timber.d("rust-mailbox-factory: Mailbox created for user: $userId, label: $labelId")
                    mailboxCache[mailboxCacheKey] = mailbox
                }
        }
    }

    suspend fun createSkipCache(userId: UserId, labelId: LocalLabelId): Either<DataError, MailboxWrapper> {
        return executeWithUserSession(userId) { session ->
            createMailbox(session, labelId)
        }.flatten()
    }

    suspend fun createAllMail(userId: UserId): Either<DataError, MailboxWrapper> {
        Timber.d("rust-mailbox-factory: Looking up AllMail cache for user: $userId")

        allMailCache[userId]?.let {
            Timber.d("rust-mailbox-factory: AllMail cache hit for user: $userId")
            return it.right()
        }

        val mutex = allMailMutexes.computeIfAbsent(userId) { Mutex() }
        return mutex.withLock {
            // Double check: if we missed the cache earlier, it should be hit now if the value is present.
            allMailCache[userId]?.let {
                Timber.d("rust-mailbox-factory: AllMail cache hit for user: $userId")
                return it.right()
            }
            Timber.d("rust-mailbox-factory: start creating AllMail mailbox")
            executeWithUserSession(userId) { session -> createAllMailMailbox(session) }
                .flatten()
                .onRight {
                    Timber.d("rust-mailbox-factory: Mailbox created for all mail label, userId: $userId")
                    allMailCache[userId] = it
                }
        }
    }
}

private data class MailboxCacheKey(
    val userId: UserId,
    val labelId: LocalLabelId
)
