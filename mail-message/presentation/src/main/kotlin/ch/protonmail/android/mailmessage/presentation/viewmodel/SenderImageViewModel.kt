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

package ch.protonmail.android.mailmessage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailmessage.domain.usecase.GetSenderImage
import ch.protonmail.android.mailmessage.presentation.model.SenderImageState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class SenderImageViewModel @Inject constructor(
    private val getSenderImage: GetSenderImage,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    // Map to keep the state for each email address
    private val senderImageStateMap = mutableMapOf<String, MutableStateFlow<SenderImageState>>()

    // Function to get the state for a particular address
    fun stateForAddress(address: String): StateFlow<SenderImageState> {
        return senderImageStateMap.getOrPut(address) {
            MutableStateFlow(SenderImageState.NotLoaded)
        }
    }



    fun loadSenderImage(address: String, bimiSelector: String?) {

        // Retrieve or initialize the state for the given address
        val stateFlow = senderImageStateMap.getOrPut(address) {
            MutableStateFlow(SenderImageState.NotLoaded)
        }

        // Check if the image is already loaded or is being loaded
        if (stateFlow.value != SenderImageState.NotLoaded) {
            return
        }

        // Run on IO dispatcher to make File IO operations in a background thread
        viewModelScope.launch(dispatcherProvider.Io) {
            stateFlow.value = SenderImageState.Loading

            val senderImage = getSenderImage(address, bimiSelector)

            if (senderImage != null && senderImage.imageFile.exists() && senderImage.imageFile.length() > 0) {
                stateFlow.value = SenderImageState.Data(senderImage.imageFile)
            } else {
                stateFlow.value = SenderImageState.NoImageAvailable
            }
        }
    }

}
