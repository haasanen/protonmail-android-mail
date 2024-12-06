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
import arrow.core.right
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.usecase.ExecuteWithUserSession
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.mailmessage.data.usecase.CreateAllMailMailbox
import ch.protonmail.android.mailmessage.data.usecase.CreateMailbox
import ch.protonmail.android.mailmessage.data.wrapper.MailboxWrapper
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustMailboxFactory @Inject constructor(
    private val executeWithUserSession: ExecuteWithUserSession,
    private val createMailbox: CreateMailbox,
    private val createAllMailMailbox: CreateAllMailMailbox,
    private val selectedMailLabelId: SelectedMailLabelId
) {

    private val mailboxCache: MutableMap<LocalLabelId, MailboxWrapper> = mutableMapOf()

    suspend fun create(userId: UserId): Either<DataError, MailboxWrapper> {
        val currentLabelId = selectedMailLabelId.flow.value.labelId.toLocalLabelId()

        val cachedMailbox = mailboxCache[currentLabelId]
        if (cachedMailbox != null) {
            return cachedMailbox.right()
        }

        return executeWithUserSession(userId) { session ->
            val mailbox = createMailbox(session, currentLabelId)
            mailboxCache[currentLabelId] = mailbox
            Timber.d("rust-mailbox-factory: Mailbox created for Current Label: $currentLabelId")
            mailbox
        }
    }

    suspend fun create(userId: UserId, labelId: LocalLabelId): Either<DataError, MailboxWrapper> {
        val cachedMailbox = mailboxCache[labelId]
        if (cachedMailbox != null) {
            return cachedMailbox.right()
        }

        return executeWithUserSession(userId) { session ->
            val mailbox = createMailbox(session, labelId)
            mailboxCache[labelId] = mailbox
            Timber.d("rust-mailbox-factory: Mailbox created for label: $labelId")
            mailbox
        }
    }

    suspend fun createAllMail(userId: UserId): Either<DataError, MailboxWrapper> =
        executeWithUserSession(userId) { session ->
            Timber.d("rust-mailbox-factory: Mailbox created for all mail label")
            createAllMailMailbox(session)
        }

}
