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
import ch.protonmail.android.mailcommon.datarust.mapper.LocalViewMode
import ch.protonmail.android.mailmessage.data.usecase.CreateMailbox
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.Mailbox
import javax.inject.Inject

class RustMailboxImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createMailbox: CreateMailbox
) : RustMailbox {

    private val mailboxMutableStatusFlow = MutableStateFlow<Mailbox?>(null)

    private val conversationMailboxFlow: Flow<Mailbox> = mailboxMutableStatusFlow.asStateFlow()
        .filterNotNull()
        .filter { it.viewMode() == LocalViewMode.CONVERSATIONS }

    private val messageMailboxFlow: Flow<Mailbox> = mailboxMutableStatusFlow.asStateFlow()
        .filterNotNull()
        .filter { it.viewMode() == LocalViewMode.MESSAGES }

    override suspend fun switchToMailbox(userId: UserId, labelId: LocalLabelId) {
        if (!shouldSwitchMailbox(labelId)) {
            return
        }

        // Reset mailbox to avoid using wrong mailbox while the new one is being created
        mailboxMutableStatusFlow.value = null

        val userSession = userSessionRepository.getUserSession(userId)
        if (userSession == null) {
            Timber.w("rust-mailbox: switchMailbox failed, no session for $userId")
            return
        }
        Timber.v("rust-mailbox: mailbox creation started... ${System.currentTimeMillis()}")
        val mailbox = createMailbox(userSession, labelId)
        Timber.d("rust-mailbox: Mailbox created for label: $labelId at ${System.currentTimeMillis()}")

        mailboxMutableStatusFlow.value = mailbox
    }

    override fun observeConversationMailbox(): Flow<Mailbox> = conversationMailboxFlow

    override fun observeMessageMailbox(): Flow<Mailbox> = messageMailboxFlow

    override fun observeMailbox(labelId: LocalLabelId): Flow<Mailbox> = mailboxMutableStatusFlow.asStateFlow()
        .filterNotNull()
        .filter { it.labelId() == labelId }

    override fun observeCurrentLabelId(): Flow<LocalLabelId> = mailboxMutableStatusFlow.asStateFlow()
        .filterNotNull()
        .map { it.labelId() }

    private fun shouldSwitchMailbox(labelId: LocalLabelId) = mailboxMutableStatusFlow.value?.labelId() != labelId
}
