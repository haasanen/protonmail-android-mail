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

package ch.protonmail.android.mailconversation.data.local

import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversation
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationForLabelPaginator
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageNumber
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.ConversationPaginator
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSession
import javax.inject.Inject

class RustConversationsQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val invalidationTracker: RustInvalidationTracker,
    private val createRustConversationForLabelPaginator: CreateRustConversationForLabelPaginator,
    private val rustMailbox: RustMailbox
) : RustConversationsQuery {

    private var paginator: Paginator? = null

    private val conversationsUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.d("rust-conversations-query: conversations updated, invalidating paginator...")

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.CONVERSATION,
                    RustDataSourceId.LABELS
                )
            )
        }
    }

    override suspend fun getConversations(userId: UserId, pageKey: PageKey): List<LocalConversation>? {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-conversation-query: trying to load conversation with a null session")
            return null
        }

        val labelId = pageKey.labelId.toLocalLabelId()
        rustMailbox.switchToMailbox(userId, labelId)
        Timber.v("rust-conversation-query: observe conversations for labelId $labelId")

        initPaginator(userId, labelId, session)

        Timber.v("rust-conversation-query: Paging: querying ${pageKey.pageNumber.name} page for messages")
        val conversations = when (pageKey.pageNumber) {
            PageNumber.First -> paginator?.rustPaginator?.currentPage()
            PageNumber.Next -> paginator?.rustPaginator?.nextPage()
            PageNumber.All -> paginator?.rustPaginator?.reload()
        }

        Timber.v("rust-conversation-query: init value for conversation is $conversations")
        return conversations
    }

    private suspend fun initPaginator(
        userId: UserId,
        labelId: LocalLabelId,
        session: MailUserSession
    ) {
        if (shouldInitPaginator(userId, labelId)) {
            Timber.v("rust-conversation-query: [destroy and] initialize paginator instance...")
            destroy()
            paginator = Paginator(
                createRustConversationForLabelPaginator(session, labelId, conversationsUpdatedCallback),
                userId,
                labelId
            )
        }
    }

    private fun shouldInitPaginator(userId: UserId, labelId: LocalLabelId) =
        paginator == null || paginator?.userId != userId || paginator?.labelId != labelId

    private fun destroy() {
        Timber.v("rust-conversation-query: disconnecting and destroying watcher")
        paginator?.rustPaginator?.handle()?.disconnect()
        paginator = null
    }

    private data class Paginator(
        val rustPaginator: ConversationPaginator,
        val userId: UserId,
        val labelId: LocalLabelId
    )
}
