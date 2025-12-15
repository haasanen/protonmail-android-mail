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

package ch.protonmail.android.navigation.deeplinks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDeepLinkHandler @Inject constructor() {

    private val _pending = MutableStateFlow<NotificationDeepLinkData?>(null)
    private val _unlocked = MutableStateFlow(false)

    // Only emits when unlocked
    val pending: Flow<NotificationDeepLinkData?> = combine(_pending, _unlocked) { data, unlocked ->
        if (unlocked) data else null
    }

    fun hasPending(): Boolean = _pending.value != null

    fun setPending(data: NotificationDeepLinkData) {
        _pending.value = data
    }

    fun setLocked() {
        _unlocked.value = false
    }

    fun setUnlocked() {
        _unlocked.value = true
    }

    fun consume() {
        _pending.value = null
    }
}
