/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailpagination.data.scroller

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailpagination.data.mapper.toPaginationError
import ch.protonmail.android.mailpagination.data.model.scroller.PendingRequest
import ch.protonmail.android.mailpagination.data.model.scroller.RequestType
import timber.log.Timber

class ScrollerOnUpdateHandler<T>(
    private val tag: String,
    private val invalidate: () -> Unit
) {

    fun handleUpdate(
        pending: PendingRequest<T>?,
        update: ScrollerUpdate<T>,
        cacheSnapshot: List<T>
    ) {
        if (pending == null) {
            Timber.d(
                "$tag: No pending request, processing indirect update " +
                    "${update.javaClass.simpleName} as an invalidation"
            )
            invalidate()
            return
        }

        when (pending.type) {
            RequestType.Append -> {
                when {
                    update.isImmediateAppendResponseEquivalent() ->
                        processImmediateAppendResponse(pending, update)

                    else ->
                        processIndirectResponseAsFallback(pending, update, cacheSnapshot)
                }
            }

            RequestType.Refresh -> {
                when {
                    update.isImmediateRefreshResponseEquivalent() ->
                        processImmediateRefreshResponse(pending, update, cacheSnapshot)

                    else ->
                        processIndirectResponseAsFallback(pending, update, cacheSnapshot)
                }
            }
        }
    }

    // Append request got a matching immediate response (Append/None/Error)
    private fun processImmediateAppendResponse(pending: PendingRequest<T>, update: ScrollerUpdate<T>) {
        Timber.d("$tag: Received direct response ${update.javaClass.simpleName} for Append request")
        when (update) {
            is ScrollerUpdate.Append -> {
                pending.response.complete(update.items.right())
            }

            is ScrollerUpdate.None -> {
                pending.response.complete(emptyList<T>().right())
            }

            is ScrollerUpdate.Error -> {
                pending.response.complete(update.error.toPaginationError().left())
            }

            else -> {
                Timber.w("$tag: Unexpected direct response – predicate failed")
                pending.response.complete(emptyList<T>().right())
            }
        }
    }

    // Refresh request got a matching immediate response: ReplaceFrom(0)
    private fun processImmediateRefreshResponse(
        pending: PendingRequest<T>,
        update: ScrollerUpdate<T>,
        snapshot: List<T>
    ) {
        Timber.d("$tag: Received direct response ${update.javaClass.simpleName} for Refresh request")
        when (update) {
            is ScrollerUpdate.ReplaceFrom -> {
                if (update.idx == 0) {
                    pending.response.complete(update.items.right())
                } else {
                    Timber.w("$tag: Unexpected ReplaceFrom idx=${update.idx}, expected 0")
                }
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
        pending: PendingRequest<T>,
        update: ScrollerUpdate<T>,
        snapshot: List<T>
    ) {
        Timber.d("$tag: Received indirect response ${update.javaClass.simpleName} for ${pending.type} request")

        when (pending.type) {
            RequestType.Append -> {
                pending.response.complete(emptyList<T>().right())
            }

            RequestType.Refresh -> {
                pending.response.complete(snapshot.right())
            }
        }
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
fun <T> ScrollerUpdate<T>.isImmediateAppendResponseEquivalent(): Boolean = this is ScrollerUpdate.Append ||
    this is ScrollerUpdate.None ||
    this is ScrollerUpdate.Error

fun <T> ScrollerUpdate<T>.isImmediateRefreshResponseEquivalent(): Boolean =
    this is ScrollerUpdate.ReplaceFrom && this.idx == 0
