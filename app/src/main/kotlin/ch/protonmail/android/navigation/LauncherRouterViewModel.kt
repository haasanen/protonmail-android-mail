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

package ch.protonmail.android.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailpinlock.domain.usecase.ShouldPresentPinInsertionScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
internal class LauncherRouterViewModel @Inject constructor(
    shouldPresentPinInsertionScreen: ShouldPresentPinInsertionScreen
) : ViewModel() {

    private val mutableShowLockScreenEvent = MutableSharedFlow<Unit>()
    val showLockScreenEvent = mutableShowLockScreenEvent.asSharedFlow()

    init {
        shouldPresentPinInsertionScreen()
            .onEach { shouldShow ->
                if (shouldShow) {
                    mutableShowLockScreenEvent.emit(Unit)
                }
            }
            .launchIn(viewModelScope)
    }
}
