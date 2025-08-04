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

package ch.protonmail.android.mailmessage.data.usecase

import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailmessage.data.wrapper.RsvpEventServiceProviderWrapper
import ch.protonmail.android.mailmessage.data.wrapper.RsvpEventServiceWrapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

class CreateRustEventServiceAccessor @Inject constructor() {

    private var cache: Pair<LocalMessageId, RsvpEventServiceWrapper>? = null
    private val mutex = Mutex()

    suspend operator fun invoke(
        rsvpEventServiceProviderWrapper: RsvpEventServiceProviderWrapper,
        messageId: LocalMessageId
    ): RsvpEventServiceWrapper? = mutex.withLock {
        cache?.let {
            if (messageId == it.first) {
                Timber.d("RustMessageEvent: cache hit, returning RSVP Event Service...")
                return it.second
            }
        }

        return rsvpEventServiceProviderWrapper.eventService()?.let {
            cache = Pair(messageId, RsvpEventServiceWrapper(it))
            RsvpEventServiceWrapper(it)
        }
    }
}
