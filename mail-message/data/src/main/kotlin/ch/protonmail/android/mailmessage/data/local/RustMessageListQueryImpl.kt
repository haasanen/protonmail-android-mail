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
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesPaginator
import ch.protonmail.android.mailmessage.data.usecase.CreateRustSearchPaginator
import ch.protonmail.android.mailmessage.data.util.awaitWithTimeout
import ch.protonmail.android.mailmessage.data.wrapper.MessagePaginatorWrapper
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
import uniffi.proton_mail_uniffi.Message
import uniffi.proton_mail_uniffi.MessageScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.MessageScrollerUpdate
import javax.inject.Inject

class RustMessageListQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustMessagesPaginator: CreateRustMessagesPaginator,
    private val createRustSearchPaginator: CreateRustSearchPaginator,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope,
    private val invalidationRepository: PageInvalidationRepository
) : RustMessageListQuery {

    private var paginatorState: PaginatorState? = null
    private val paginatorMutex = Mutex()

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): Either<PaginationError, List<Message>> {

        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-message-query: trying to load messages with a null session")
            return PaginationError.Other(DataError.Local.NoUserSession).left()
        }

        val pageDescriptor = pageKey.toPageDescriptor(userId)

        paginatorMutex.withLock {
            if (shouldInitPaginator(pageDescriptor, pageKey)) {
                initPaginator(pageDescriptor, session)
            }
        }

        Timber.d("rust-message-query: Paging: querying ${pageKey.pageToLoad.name} page for messages")

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
                        Timber.d("rust-message-query: Follow-up response timed out.")
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

    override suspend fun terminatePaginator(userId: UserId) {
        if (paginatorState?.pageDescriptor?.userId == userId) {
            paginatorMutex.withLock {
                destroy()
            }
        } else {
            Timber.d("rust-message-query: Not terminating paginator, userId does not match")
        }
    }

    private suspend fun initPaginator(pageDescriptor: PageDescriptor, session: MailUserSessionWrapper) {
        Timber.d("rust-message-query: [destroy and] initialize paginator instance...")
        destroy()

        val scrollerOnUpdateHandler = ScrollerOnUpdateHandler<Message>(
            tag = "rust-message-query",
            invalidate = { invalidateLoadedItems() }
        )


        when (pageDescriptor) {
            is PageDescriptor.Default -> createRustMessagesPaginator(
                session = session,
                labelId = pageDescriptor.labelId.toLocalLabelId(),
                unread = pageDescriptor.unread,
                callback = messagesUpdatedCallback(scrollerOnUpdateHandler)
            )

            is PageDescriptor.Search -> createRustSearchPaginator(
                session = session,
                keyword = pageDescriptor.keyword,
                callback = messagesUpdatedCallback(scrollerOnUpdateHandler)
            )
        }.onRight { wrapper ->
            paginatorState = PaginatorState(
                paginatorWrapper = wrapper,
                pageDescriptor = pageDescriptor,
                scrollerCache = ScrollerCache()
            )
        }
    }

    private fun messagesUpdatedCallback(onUpdateHandler: ScrollerOnUpdateHandler<Message>) =
        object : MessageScrollerLiveQueryCallback {
            override fun onUpdate(update: MessageScrollerUpdate) {
                Timber.d("rust-message-query: Received paginator update: ${update.javaClass.simpleName}")
                coroutineScope.launch {
                    paginatorMutex.withLock {
                        val update = update.toScrollerUpdate()

                        val snapshot = paginatorState?.scrollerCache?.applyUpdate(update) ?: emptyList()
                        val pending = paginatorState?.pendingRequest

                        onUpdateHandler.handleUpdate(pending, update, snapshot) {
                            // We need to wait for the follow-up response
                            if (pending?.type == RequestType.Append) {
                                Timber.d("rust-message-query: Triggering follow-up after immediate Append None")
                                paginatorState = paginatorState?.withFollowUpResponse()
                            }
                        }

                        if (paginatorState?.pendingRequest == null) {
                            Timber.d("rust-message-query: No pending request")
                        } else if (paginatorState?.pendingRequest?.isCompleted() == true) {
                            Timber.d("rust-message-query: Clearing completed pending request")
                            paginatorState = paginatorState?.copy(pendingRequest = null)
                        } else {
                            Timber.d("rust-message-query: Keeping pending request, waiting for more data")
                        }
                    }
                }
            }
        }

    private fun shouldInitPaginator(pageDescriptor: PageDescriptor, pageKey: PageKey) = paginatorState == null ||
        paginatorState?.pageDescriptor != pageDescriptor ||
        pageKey.pageToLoad == PageToLoad.First

    private fun destroy() {
        if (paginatorState == null) {
            Timber.d("rust-message-query: no paginator to destroy")
        } else {
            Timber.d("rust-message-query: disconnecting and destroying paginator")
            paginatorState?.paginatorWrapper?.destroy()
            paginatorState = null
        }
    }

    private fun invalidateLoadedItems() {
        coroutineScope.launch {
            invalidationRepository.submit(PageInvalidationEvent.MessagesInvalidated)
        }
    }

    private suspend fun setPendingRequest(
        type: RequestType
    ): CompletableDeferred<Either<PaginationError, List<Message>>> {
        paginatorMutex.withLock {
            val deferred = CompletableDeferred<Either<PaginationError, List<Message>>>()
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
        val paginatorWrapper: MessagePaginatorWrapper,
        val pageDescriptor: PageDescriptor,
        val scrollerCache: ScrollerCache<Message>,
        val pendingRequest: PendingRequest<Message>? = null
    )

    private fun PaginatorState.withFollowUpResponse(): PaginatorState {
        val currentPending = this.pendingRequest
            ?: return this

        val newPending = currentPending.copy(followUpResponse = CompletableDeferred())

        return this.copy(pendingRequest = newPending)
    }

    private sealed interface PageDescriptor {
        val userId: UserId
        data class Default(override val userId: UserId, val labelId: LabelId, val unread: Boolean) : PageDescriptor
        data class Search(override val userId: UserId, val keyword: String) : PageDescriptor
    }

    private fun PageKey.toPageDescriptor(userId: UserId): PageDescriptor = when (this) {
        is PageKey.DefaultPageKey -> PageDescriptor.Default(
            userId = userId,
            labelId = this.labelId,
            unread = this.readStatus == ReadStatus.Unread
        )
        is PageKey.PageKeyForSearch -> PageDescriptor.Search(
            userId = userId,
            keyword = keyword
        )
    }

    companion object {
        const val NONE_FOLLOWUP_GRACE_MS = 250L
    }
}

fun MessageScrollerUpdate.toScrollerUpdate(): ScrollerUpdate<Message> = when (this) {
    is MessageScrollerUpdate.Append -> ScrollerUpdate.Append(v1)
    is MessageScrollerUpdate.ReplaceFrom -> ScrollerUpdate.ReplaceFrom(idx.toInt(), items)
    is MessageScrollerUpdate.ReplaceBefore -> ScrollerUpdate.ReplaceBefore(idx.toInt(), items)
    is MessageScrollerUpdate.Error -> ScrollerUpdate.Error(error)
    MessageScrollerUpdate.None -> ScrollerUpdate.None
    is MessageScrollerUpdate.ReplaceRange -> ScrollerUpdate.ReplaceRange(from.toInt(), to.toInt(), items)
}
