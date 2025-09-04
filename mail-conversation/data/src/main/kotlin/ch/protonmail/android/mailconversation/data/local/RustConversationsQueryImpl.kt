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
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailpagination.data.mapper.toPaginationError
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
import uniffi.proton_mail_uniffi.Conversation
import uniffi.proton_mail_uniffi.ConversationScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.ConversationScrollerUpdate
import javax.inject.Inject

class RustConversationsQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustConversationPaginator: CreateRustConversationPaginator,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope,
    private val invalidationRepository: PageInvalidationRepository
) : RustConversationsQuery {

    // Request–response wiring
    private enum class RequestType {

        Append, Refresh
    }

    private data class PendingRequest(
        val type: RequestType,
        val response: CompletableDeferred<Either<PaginationError, List<LocalConversation>>>
    )

    private var paginatorState: PaginatorState? = null
    private val paginatorMutex = Mutex()

    // Append request got a matching immediate response (Append/None/Error)
    private fun processImmediateAppendResponse(pending: PendingRequest, update: ConversationScrollerUpdate) {
        Timber.d("rust-conversation-query: Received direct response ${update.javaClass.simpleName} for Append request")
        when (update) {
            is ConversationScrollerUpdate.Append -> {
                pending.response.complete(update.v1.right())
            }

            is ConversationScrollerUpdate.None -> {
                pending.response.complete(emptyList<LocalConversation>().right())
            }

            is ConversationScrollerUpdate.Error -> {
                pending.response.complete(update.error.toPaginationError().left())
            }

            else -> {
                Timber.w("rust-conversation-query: Unexpected direct response – predicate failed")
                pending.response.complete(emptyList<LocalConversation>().right())
            }
        }
    }

    // Refresh request got a matching immediate response: ReplaceFrom(0)
    private fun processImmediateRefreshResponse(
        pending: PendingRequest,
        update: ConversationScrollerUpdate,
        snapshot: List<Conversation>
    ) {
        Timber.d("rust-conversation-query: Received direct response ${update.javaClass.simpleName} for Refresh request")
        when (update) {
            is ConversationScrollerUpdate.ReplaceFrom -> {
                pending.response.complete(update.items.right())
            }

            else -> {
                pending.response.complete(snapshot.right())
            }
        }
    }

    // A first response arrived but did NOT match the request's "immediate" expectation.
    //  - If request was Append -> return emptyList()
    //  - If request was Refresh -> return current cache snapshot
    private fun processIndirectResponseAsFallback(
        pending: PendingRequest,
        update: ConversationScrollerUpdate,
        snapshot: List<Conversation>
    ) {
        Timber.d(
            "rust-conversation-query: Received indirect response ${update.javaClass.simpleName} " +
                "for ${pending.type} request"
        )

        when (pending.type) {
            RequestType.Append -> {
                pending.response.complete(emptyList<LocalConversation>().right())
            }

            RequestType.Refresh -> {
                pending.response.complete(snapshot.right())
            }
        }
    }

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
                deferred.await()
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

        createRustConversationPaginator(
            session,
            pageDescriptor.labelId.toLocalLabelId(),
            pageDescriptor.unread,
            conversationsUpdatedCallback()
        )
            .onRight {
                paginatorState = PaginatorState(
                    paginatorWrapper = it,
                    pageDescriptor = pageDescriptor,
                    collectedItems = mutableListOf()
                )
            }
    }

    private fun conversationsUpdatedCallback() = object : ConversationScrollerLiveQueryCallback {
        override fun onUpdate(update: ConversationScrollerUpdate) {
            Timber.d("rust-conversation-query: Received paginator update: ${update.javaClass.simpleName}")
            coroutineScope.launch {
                paginatorMutex.withLock {

                    // Update internal cache
                    val snapshot = applyCacheUpdate(update)
                    val pending = paginatorState?.pendingRequest

                    if (pending != null) {
                        when (pending.type) {
                            RequestType.Append -> {
                                when {
                                    // This is the first expected response for Append request
                                    update.isImmediateAppendResponse() ->
                                        processImmediateAppendResponse(pending, update)

                                    else ->
                                        // This is fallback branch: We have an Append request but the response is not
                                        // one of expected Append responses. Not to get stuck on the request,
                                        // we complete the request with an Append empty list.
                                        processIndirectResponseAsFallback(pending, update, snapshot)
                                }
                            }

                            RequestType.Refresh -> {
                                when {
                                    // This is the first expected response for Refresh request
                                    update.isImmediateRefreshResponse() ->
                                        processImmediateRefreshResponse(pending, update, snapshot)

                                    else ->
                                        // This is fallback branch: We have a Refresh request but the response is not
                                        // the expected Refresh response. Not to get stuck on the request,
                                        // we complete the request with cache snapshot data.
                                        processIndirectResponseAsFallback(pending, update, snapshot)
                                }
                            }
                        }
                        paginatorState = paginatorState?.copy(
                            pendingRequest = null
                        )
                    } else {
                        Timber.d(
                            "rust-conversation-query: No pending request, processing indirect update " +
                                "${update.javaClass.simpleName} as an invalidation"
                        )
                        invalidateLoadedItems()
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

    private fun applyCacheUpdate(update: ConversationScrollerUpdate): List<Conversation> {
        val state = paginatorState ?: return emptyList()
        val list = state.collectedItems

        when (update) {
            is ConversationScrollerUpdate.Append -> {
                list.addAll(update.v1)
            }

            is ConversationScrollerUpdate.ReplaceFrom -> {
                val idx = update.idx.toInt()
                if (idx >= 0 && idx < list.size) {
                    list.subList(idx, list.size).clear()
                    list.addAll(update.items)
                } else if (idx == list.size) {
                    list.addAll(update.items)
                } else {
                    Timber.w(
                        "rust-conversation-query: applyCacheUpdate ReplaceFrom ignored: " +
                            "idx=$idx out of bounds (size=${list.size})"
                    )
                }
            }

            is ConversationScrollerUpdate.ReplaceBefore -> {
                val idx = update.idx.toInt()
                if (idx >= 0 && idx < list.size) {
                    list.subList(0, idx).clear()
                    list.addAll(0, update.items)
                } else if (idx == list.size) {
                    list.clear()
                    list.addAll(update.items)
                } else {
                    Timber.w(
                        "rust-conversation-query: applyCacheUpdate ReplaceBefore ignored: " +
                            "idx=$idx out of bounds (size=${list.size})"
                    )
                }
            }

            is ConversationScrollerUpdate.None,
            is ConversationScrollerUpdate.Error -> Unit
        }

        return list.toList()
    }

    private data class PaginatorState(
        val paginatorWrapper: ConversationPaginatorWrapper,
        val pageDescriptor: PageDescriptor,
        val collectedItems: MutableList<Conversation>,
        val pendingRequest: PendingRequest? = null
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

}

/**
 * Expected Rust callback responses to paginator calls
 *
 * * Direct
 * [ get_items ] => RefreshFrom (0)
 * [ force_refresh ] => ReplaceFrom (0)
 * [ fetch_more ] => Append / None / Error
 * [ *refresh ] => ReplaceBefore / ReplaceFrom / Error
 *
 * * Indirect
 * It is triggered when location was empty (means append to the top)
 * [ fetch_more ] => None => [ refresh ] => ReplaceBefore(0)
 *
 * When loaded and displayed data is not synced (offline mode) and we were able to sync the real data
 * [ fetch_more ] => None => [ refresh ] => ReplaceFrom(0)
 *
 * When you loose your network on empty location and sync failed (offline) new fetch_more will
 * be scheduled internally (when back online)
 * [ fetch_more ] => None => [ fetch_more ] => Append
 */

fun ConversationScrollerUpdate.isImmediateAppendResponse() = this is ConversationScrollerUpdate.Append ||
    this is ConversationScrollerUpdate.None ||
    this is ConversationScrollerUpdate.Error

fun ConversationScrollerUpdate.isImmediateRefreshResponse() =
    this is ConversationScrollerUpdate.ReplaceFrom && this.idx.toInt() == 0
