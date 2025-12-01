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

package ch.protonmail.android.mailpinlock.domain.usecase

import arrow.core.getOrElse
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

class ShouldPresentPinInsertionScreen @Inject constructor(
    private val autoLockRepository: AutoLockRepository,
    private val autoLockCheckPendingState: AutoLockCheckPendingState
) {

    operator fun invoke(): Flow<Boolean> = autoLockCheckPendingState.autoLockCheckEvents
        .onStart { emit(Unit) }
        .map {
            Timber.tag("ShouldPresentPin").d("checking shouldAutoLock...")
            val shouldLock = autoLockRepository.shouldAutoLock()
                .getOrElse {
                    Timber.tag("ShouldPresentPin").e("Unable to get a value for shouldAutolock")
                    false
                }
            Timber.tag("ShouldPresentPin").d("got '$shouldLock', continuing...")
            shouldLock
        }
}
