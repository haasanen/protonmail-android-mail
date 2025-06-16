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
import ch.protonmail.android.mailcommon.domain.AppInBackgroundState
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPending
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

class ShouldPresentPinInsertionScreen @Inject constructor(
    private val appInBackgroundState: AppInBackgroundState,
    private val autoLockRepository: AutoLockRepository,
    private val autoLockCheckPendingState: AutoLockCheckPendingState
) {

    operator fun invoke(): Flow<Boolean> = combine(
        appInBackgroundState.observe(),
        autoLockCheckPendingState.state
    ) { inBackground, isAutoLockPending ->

        if (inBackground) {
            // Reset the flag when app goes to background
            autoLockCheckPendingState.emitOperationSignal(AutoLockCheckPending(true))
            false
        } else if (!isAutoLockPending.value) {
            false
        } else {
            autoLockRepository.shouldAutoLock()
                .getOrElse {
                    Timber.e("ShouldPresentPinInsertionScreen unable to get a value for shouldAutoLock")
                    false
                }
        }
    }
}
