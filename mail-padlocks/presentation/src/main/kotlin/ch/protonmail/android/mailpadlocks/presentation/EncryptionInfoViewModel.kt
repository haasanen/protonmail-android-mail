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

package ch.protonmail.android.mailpadlocks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsShowEncryptionInfoEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EncryptionInfoViewModel @Inject constructor(
    @IsShowEncryptionInfoEnabled val showEncryptionInfoFeatureFlag: FeatureFlag<Boolean>
) : ViewModel() {

    private val mutableState = MutableStateFlow<EncryptionInfoState>(EncryptionInfoState.Disabled)

    val state: StateFlow<EncryptionInfoState> = mutableState

    init {
        checkEncryptionInfoFeatureFlag()
    }

    private fun checkEncryptionInfoFeatureFlag() {
        viewModelScope.launch {
            if (showEncryptionInfoFeatureFlag.get()) {
                mutableState.emit(EncryptionInfoState.Enabled)
            } else {
                mutableState.emit(EncryptionInfoState.Disabled)
            }
        }
    }

}

