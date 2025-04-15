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

import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import javax.inject.Inject

class RustConversationsQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val invalidationTracker: RustInvalidationTracker,
    private val createRustConversationPaginator: CreateRustConversationPaginator
) : RustConversationsQuery {

    private var paginatorState: PaginatorState? = null
    private val paginatorMutex = Mutex()

    private val conversationsUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.d("rust-conversation-query: paging: conversations updated, invalidating paginator...")

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.CONVERSATION,
                    RustDataSourceId.LABELS
                )
            )
        }
    }

    override suspend fun getConversations(userId: UserId, pageKey: PageKey.DefaultPageKey): List<LocalConversation>? {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-conversation-query: trying to load conversation with a null session")
            return null
        }

        val labelId = pageKey.labelId.toLocalLabelId()
        val unread = pageKey.readStatus == ReadStatus.Unread
        Timber.v("rust-conversation-query: observe conversations for labelId $labelId unread: $unread")

        initPaginator(userId, labelId, unread, session, pageKey)

        Timber.v("rust-conversation-query: Paging: querying ${pageKey.pageToLoad.name} page for messages")
        val conversations = when (pageKey.pageToLoad) {
            PageToLoad.First -> paginatorState?.paginatorWrapper?.nextPage()
            PageToLoad.Next -> paginatorState?.paginatorWrapper?.nextPage()
            PageToLoad.All -> paginatorState?.paginatorWrapper?.reload()
        }?.getOrElse { emptyList() }

        Timber.v("rust-conversation-query: init value for conversation is $conversations")
        return conversations
    }

    private suspend fun initPaginator(
        userId: UserId,
        labelId: LocalLabelId,
        unread: Boolean,
        session: MailUserSessionWrapper,
        pageKey: PageKey.DefaultPageKey
    ) = paginatorMutex.withLock<Unit> {
        if (!shouldInitPaginator(userId, labelId, unread, pageKey)) {
            Timber.v("rust-conversation-query: reusing existing paginator instance...")
            return
        }

        Timber.v("rust-conversation-query: [destroy and] initialize paginator instance...")
        destroy()

        createRustConversationPaginator(session, labelId, unread, conversationsUpdatedCallback)
            .onRight {
                paginatorState = PaginatorState(
                    paginatorWrapper = it,
                    userId = userId,
                    labelId = labelId,
                    unread = unread
                )
            }
    }

    private fun shouldInitPaginator(
        userId: UserId,
        labelId: LocalLabelId,
        unread: Boolean,
        pageKey: PageKey.DefaultPageKey
    ) = paginatorState == null ||
        paginatorState?.userId != userId ||
        paginatorState?.labelId != labelId ||
        paginatorState?.unread != unread ||
        pageKey.pageToLoad == PageToLoad.First

    private fun destroy() {
        Timber.v("rust-conversation-query: disconnecting and destroying watcher")
        paginatorState?.paginatorWrapper?.disconnect()
        paginatorState = null
    }

    private data class PaginatorState(
        val paginatorWrapper: ConversationPaginatorWrapper,
        val userId: UserId,
        val labelId: LocalLabelId,
        val unread: Boolean
    )
}
