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

package ch.protonmail.android.mailpinlock.presentation.autolock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailpinlock.domain.AutolockRepository
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.presentation.autolock.mapper.AutolockSettingsUiMapper.toTextUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AutolockIntervalViewModel @Inject constructor(
    private val autolockRepository: AutolockRepository
) : ViewModel() {

    private val _effects = MutableStateFlow(AutolockIntervalEffects())
    val effects = _effects.asStateFlow()

    val state: Flow<AutolockIntervalState> = autolockRepository
        .observeAppLock()
        .mapLatest { autolock ->
            AutolockIntervalState.Data(
                autolock.autolockInterval,
                AutoLockInterval.entries.sortedBy { it.duration }
                    .toMutableList().apply { this.removeAt(this.lastIndex) }
                    .associateWith { it.toTextUiModel() }
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis),
            AutolockIntervalState.Loading
        )

    fun onIntervalSelected(interval: AutoLockInterval) {
        _effects.update { it.onCloseEffect() }
        viewModelScope.launch {
            autolockRepository.updateAutolockInterval(interval)
        }
    }
}
