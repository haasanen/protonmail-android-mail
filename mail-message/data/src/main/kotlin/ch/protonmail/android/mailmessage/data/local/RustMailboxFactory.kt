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
import arrow.core.flatten
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.mailmessage.data.usecase.CreateAllMailMailbox
import ch.protonmail.android.mailmessage.data.usecase.CreateMailbox
import ch.protonmail.android.mailmessage.data.wrapper.MailboxWrapper
import ch.protonmail.android.mailsession.data.usecase.ExecuteWithUserSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustMailboxFactory @Inject constructor(
    private val executeWithUserSession: ExecuteWithUserSession,
    private val createMailbox: CreateMailbox,
    private val createAllMailMailbox: CreateAllMailMailbox,
    private val selectedMailLabelId: SelectedMailLabelId
) {

    private val mutex = Mutex()
    private var mailboxCache: MailboxWrapper? = null

    @Deprecated("Error prone due to using selectedMailLabel; To be dropped after ET-1739")
    suspend fun create(userId: UserId): Either<DataError, MailboxWrapper> = mutex.withLock {
        val currentLabelId = selectedMailLabelId.flow.value.labelId.toLocalLabelId()

        val cachedMailbox = getMailboxFromCache(currentLabelId)
        if (cachedMailbox != null) {
            return cachedMailbox.right()
        }

        return executeWithUserSession(userId) { session -> createMailbox(session, currentLabelId) }
            .flatten()
            .onRight {
                Timber.d("rust-mailbox-factory: Mailbox created for Current Label: $currentLabelId")
                mailboxCache = it
            }
    }

    suspend fun create(userId: UserId, labelId: LocalLabelId): Either<DataError, MailboxWrapper> = mutex.withLock {
        val cachedMailbox = getMailboxFromCache(labelId)
        if (cachedMailbox != null) {
            return cachedMailbox.right()
        }

        return executeWithUserSession(userId) { session -> createMailbox(session, labelId) }
            .flatten()
            .onRight {
                Timber.d("rust-mailbox-factory: Mailbox created for label: $labelId")
                mailboxCache = it
            }
    }

    suspend fun createAllMail(userId: UserId): Either<DataError, MailboxWrapper> = mutex.withLock {
        return executeWithUserSession(userId) { session -> createAllMailMailbox(session) }
            .flatten()
            .onRight { Timber.d("rust-mailbox-factory: Mailbox created for all mail label") }
    }

    private fun getMailboxFromCache(currentLabelId: LocalLabelId): MailboxWrapper? {
        if (mailboxCache?.labelId() == currentLabelId) {
            return mailboxCache
        }
        return null
    }
}
