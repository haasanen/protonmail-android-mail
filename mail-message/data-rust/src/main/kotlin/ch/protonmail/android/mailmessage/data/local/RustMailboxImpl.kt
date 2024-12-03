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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.usecase.ExecuteActionWithUserSession
import ch.protonmail.android.mailmessage.data.usecase.CreateAllMailMailbox
import ch.protonmail.android.mailmessage.data.usecase.CreateMailbox
import ch.protonmail.android.mailmessage.data.wrapper.MailboxWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustMailboxImpl @Inject constructor(
    private val executeActionWithUserSession: ExecuteActionWithUserSession,
    private val createMailbox: CreateMailbox,
    private val createAllMailMailbox: CreateAllMailMailbox
) : RustMailbox {

    private val mailboxMutableStatusFlow = MutableStateFlow<MailboxWrapper?>(null)

    private val mailboxFlow: Flow<MailboxWrapper> = mailboxMutableStatusFlow.asStateFlow()
        .filterNotNull()

    override suspend fun switchToMailbox(userId: UserId, labelId: LocalLabelId) {
        if (!shouldSwitchMailbox(labelId)) {
            return
        }
        // Reset mailbox to avoid using wrong mailbox while the new one is being created
        mailboxMutableStatusFlow.value = null

        executeActionWithUserSession(userId) { userSession ->
            Timber.v("rust-mailbox: mailbox creation started... ${System.currentTimeMillis()}")
            val mailbox = createMailbox(userSession, labelId)
            Timber.d("rust-mailbox: Mailbox created for label: $labelId at ${System.currentTimeMillis()}")
            mailboxMutableStatusFlow.value = mailbox
        }
    }

    /**
     * Switches rust Mailbox object to AllMail OR AlmostAllMail
     * depending on the user's preference. (Decision making is delegated to the rust lib)
     */
    override suspend fun switchToAllMailMailbox(userId: UserId) {
        // Reset mailbox to avoid using wrong mailbox while the new one is being created
        mailboxMutableStatusFlow.value = null

        executeActionWithUserSession(userId) { userSession ->
            Timber.v("rust-mailbox: all mail mailbox creation...")
            val mailbox = createAllMailMailbox(userSession)
            mailboxMutableStatusFlow.value = mailbox
        }
    }


    override fun observeMailbox(): Flow<MailboxWrapper> = mailboxFlow

    override fun observeMailbox(labelId: LocalLabelId): Flow<MailboxWrapper> = mailboxMutableStatusFlow
        .asStateFlow()
        .filterNotNull()
        .filter { it.labelId() == labelId }

    override fun observeCurrentLabelId(): Flow<LocalLabelId> = mailboxMutableStatusFlow.asStateFlow()
        .filterNotNull()
        .map { it.labelId() }

    private fun shouldSwitchMailbox(labelId: LocalLabelId) = mailboxMutableStatusFlow.value?.labelId() != labelId
}
