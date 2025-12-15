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

package ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailcommon.presentation.AutoLockUnlockSignal
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockOverlayState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    private val autoLockRepository: AutoLockRepository,
    private val autoLockUnlockSignal: AutoLockUnlockSignal
) : ViewModel() {

    val state = autoLockRepository.observeAppLock().map {
        it.asInterstitialState()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = AutoLockOverlayState.Loading
    )

    private fun AutoLock.asInterstitialState() = when (this.protectionType) {
        Protection.Pin -> AutoLockOverlayState.Pin
        Protection.Biometrics -> AutoLockOverlayState.Biometrics
        Protection.None -> AutoLockOverlayState.Error
    }

    fun onSuccessfulBiometrics() {
        viewModelScope.launch {
            autoLockRepository.signalBiometricsCheckPassed()
            autoLockUnlockSignal.signalUnlock()
        }
    }
}
