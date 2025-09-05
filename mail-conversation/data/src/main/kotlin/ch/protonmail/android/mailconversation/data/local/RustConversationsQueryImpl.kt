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

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.data.util.awaitWithTimeout
import ch.protonmail.android.mailpagination.data.model.scroller.PendingRequest
import ch.protonmail.android.mailpagination.data.model.scroller.RequestType
import ch.protonmail.android.mailpagination.data.model.scroller.isCompleted
import ch.protonmail.android.mailpagination.data.scroller.ScrollerCache
import ch.protonmail.android.mailpagination.data.scroller.ScrollerOnUpdateHandler
import ch.protonmail.android.mailpagination.data.scroller.ScrollerUpdate
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailpagination.domain.repository.PageInvalidationRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.ConversationScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.ConversationScrollerUpdate
import javax.inject.Inject

class RustConversationsQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustConversationPaginator: CreateRustConversationPaginator,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope,
    private val invalidationRepository: PageInvalidationRepository
) : RustConversationsQuery {

    private var paginatorState: PaginatorState? = null
    private val paginatorMutex = Mutex()

    override suspend fun getConversations(
        userId: UserId,
        pageKey: PageKey.DefaultPageKey
    ): Either<PaginationError, List<LocalConversation>> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-conversation-query: trying to load conversation with a null session")
            return PaginationError.Other(DataError.Local.NoUserSession).left()
        }

        val labelId = pageKey.labelId
        val unread = pageKey.readStatus == ReadStatus.Unread
        Timber.d("rust-conversation-query: observe conversations for labelId $labelId unread: $unread")

        val pageDescriptor = pageKey.toPageDescriptor(userId)

        paginatorMutex.withLock {
            if (shouldInitPaginator(pageDescriptor, pageKey)) {
                initPaginator(pageDescriptor, session)
            }
        }

        Timber.d("rust-conversation-query: Paging: querying ${pageKey.pageToLoad.name} page for conversation")

        return when (pageKey.pageToLoad) {
            PageToLoad.First,
            PageToLoad.Next -> {
                val deferred = setPendingRequest(RequestType.Append)
                paginatorState?.paginatorWrapper?.nextPage()

                // Wait for immediate Append response
                deferred.await().let { firstResponse ->

                    // If available wait for follow-up response
                    val followUp = paginatorState?.pendingRequest?.followUpResponse
                    followUp?.awaitWithTimeout(NONE_FOLLOWUP_GRACE_MS, firstResponse) {
                        Timber.d("rust-conversation-query: Follow-up response timed out.")
                    } ?: firstResponse
                }
            }

            PageToLoad.All -> {
                val deferred = setPendingRequest(RequestType.Refresh)
                paginatorState?.paginatorWrapper?.reload()

                // Wait for immediate Refresh response
                deferred.await()
            }
        }
    }

    private suspend fun initPaginator(pageDescriptor: PageDescriptor, session: MailUserSessionWrapper) {

        Timber.d("rust-conversation-query: [destroy and] initialize paginator instance...")
        destroy()

        val scrollerOnUpdateHandler = ScrollerOnUpdateHandler<LocalConversation>(
            tag = "rust-conversation-query",
            invalidate = { invalidateLoadedItems() }
        )

        createRustConversationPaginator(
            session,
            pageDescriptor.labelId.toLocalLabelId(),
            pageDescriptor.unread,
            conversationsUpdatedCallback(scrollerOnUpdateHandler)
        )
            .onRight {
                paginatorState = PaginatorState(
                    paginatorWrapper = it,
                    pageDescriptor = pageDescriptor,
                    scrollerCache = ScrollerCache()
                )
            }
    }

    private fun conversationsUpdatedCallback(onUpdateHandler: ScrollerOnUpdateHandler<LocalConversation>) =
        object : ConversationScrollerLiveQueryCallback {
            override fun onUpdate(update: ConversationScrollerUpdate) {
                Timber.d("rust-conversation-query: Received paginator update: ${update.javaClass.simpleName}")
                coroutineScope.launch {
                    paginatorMutex.withLock {

                        // Update internal cache
                        val snapshot = paginatorState?.scrollerCache?.applyUpdate(
                            update.toScrollerUpdate()
                        ) ?: emptyList()
                        val pending = paginatorState?.pendingRequest

                        onUpdateHandler.handleUpdate(pending, update.toScrollerUpdate(), snapshot) {
                            // We need to wait for the follow-up response
                            if (pending?.type == RequestType.Append) {
                                Timber.d("rust-conversation-query: Triggering follow-up after immediate Append None")
                                paginatorState = paginatorState?.withFollowUpResponse()
                            }
                        }

                        if (paginatorState?.pendingRequest != null &&
                            paginatorState?.pendingRequest?.isCompleted() == true
                        ) {

                            Timber.d("rust-conversation-query: Clearing completed pending request")
                            paginatorState = paginatorState?.copy(pendingRequest = null)
                        } else {
                            Timber.d("rust-conversation-query: Keeping pending request, waiting for more data")
                        }
                    }
                }
            }
        }

    private fun shouldInitPaginator(pageDescriptor: PageDescriptor, pageKey: PageKey.DefaultPageKey) =
        paginatorState == null ||
            paginatorState?.pageDescriptor != pageDescriptor ||
            pageKey.pageToLoad == PageToLoad.First

    private fun destroy() {
        Timber.d("rust-conversation-query: disconnecting and destroying watcher")
        paginatorState?.paginatorWrapper?.disconnect()
        paginatorState = null
    }

    private fun invalidateLoadedItems() {
        coroutineScope.launch {
            invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated)
        }
    }

    private suspend fun setPendingRequest(
        type: RequestType
    ): CompletableDeferred<Either<PaginationError, List<LocalConversation>>> {
        paginatorMutex.withLock {
            val deferred = CompletableDeferred<Either<PaginationError, List<LocalConversation>>>()
            paginatorState = paginatorState?.copy(
                pendingRequest = PendingRequest(
                    type = type,
                    response = deferred
                )
            )

            return deferred
        }
    }

    private data class PaginatorState(
        val paginatorWrapper: ConversationPaginatorWrapper,
        val pageDescriptor: PageDescriptor,
        val scrollerCache: ScrollerCache<LocalConversation>,
        val pendingRequest: PendingRequest<LocalConversation>? = null
    )

    private data class PageDescriptor(
        val userId: UserId,
        val labelId: LabelId,
        val unread: Boolean
    )

    private fun PageKey.DefaultPageKey.toPageDescriptor(userId: UserId): PageDescriptor {
        return PageDescriptor(
            userId = userId,
            labelId = this.labelId,
            unread = this.readStatus == ReadStatus.Unread
        )
    }

    private fun PaginatorState.withFollowUpResponse(): PaginatorState {
        val currentPending = this.pendingRequest
            ?: return this

        val newPending = currentPending.copy(followUpResponse = CompletableDeferred())

        return this.copy(pendingRequest = newPending)
    }

    companion object {
        const val NONE_FOLLOWUP_GRACE_MS = 250L
    }
}

fun ConversationScrollerUpdate.toScrollerUpdate(): ScrollerUpdate<LocalConversation> = when (this) {
    is ConversationScrollerUpdate.Append -> ScrollerUpdate.Append(v1)
    is ConversationScrollerUpdate.ReplaceFrom -> ScrollerUpdate.ReplaceFrom(idx.toInt(), items)
    is ConversationScrollerUpdate.ReplaceBefore -> ScrollerUpdate.ReplaceBefore(idx.toInt(), items)
    is ConversationScrollerUpdate.Error -> ScrollerUpdate.Error(error)
    ConversationScrollerUpdate.None -> ScrollerUpdate.None
}

