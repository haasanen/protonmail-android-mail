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

package ch.protonmail.android.mailcommon.data.repository

import ch.protonmail.android.mailcommon.data.EphemeralMailBoxCoroutineScope
import ch.protonmail.android.mailcommon.domain.model.EphemeralMailboxCursor
import ch.protonmail.android.mailcommon.domain.repository.ConversationCursor
import ch.protonmail.android.mailcommon.domain.repository.EphemeralMailboxCursorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class EphemeralMailboxCursorRepository @Inject constructor(
    @EphemeralMailBoxCoroutineScope private val coroutineScope: CoroutineScope
) :
    EphemeralMailboxCursorRepository {

    private var initialised = false

    private val _cursorState = MutableStateFlow<ConversationCursor?>(null)

    private val cursorStateFlow = _cursorState
        .map { cursor ->
            if (!initialised) {
                EphemeralMailboxCursor.NotInitalised
            } else if (cursor == null) {
                EphemeralMailboxCursor.CursorDead
            } else {
                EphemeralMailboxCursor.Data(cursor)
            }
        }
        .onCompletion { cause ->
            // no subscribers
            cleanup()
        }
        .shareIn(
            scope = coroutineScope,
            started = WhileSubscribed(stopTimeoutMillis = 5000.milliseconds.inWholeMilliseconds),
            replay = 1
        )

    override fun observeCursor(): Flow<EphemeralMailboxCursor?> = cursorStateFlow
    override fun setEphemeralCursor(conversationCursor: ConversationCursor) {
        initialised = true
        _cursorState.value = conversationCursor

    }

    private fun cleanup() {
        Timber.d("conversation-cursor being cleaned up")
        _cursorState.value?.close()
        _cursorState.value = null
    }
}
