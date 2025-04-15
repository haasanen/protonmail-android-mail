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

package ch.protonmail.android.mailmessage.data.repository

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import ch.protonmail.android.mailmessage.data.ImageLoaderCoroutineScope
import ch.protonmail.android.mailmessage.domain.model.AvatarImageState
import ch.protonmail.android.mailmessage.domain.repository.InMemoryAvatarImageStateRepository
import ch.protonmail.android.mailmessage.domain.model.AvatarImageStates
import ch.protonmail.android.mailmessage.domain.usecase.GetSenderImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class InMemoryAvatarImageStateRepositoryImpl @Inject constructor(
    private val getSenderImage: GetSenderImage,
    @ImageLoaderCoroutineScope private val coroutineScope: CoroutineScope
) : InMemoryAvatarImageStateRepository {

    private val avatarImageStateMap = ConcurrentHashMap<String, MutableStateFlow<AvatarImageState>>()
    private val allStatesFlow = MutableStateFlow(AvatarImageStates(emptyMap()))

    override fun loadImage(address: String, bimiSelector: String?) {

        val stateFlow = avatarImageStateMap.computeIfAbsent(address) {
            MutableStateFlow(AvatarImageState.NotLoaded)
        }

        // Prevent redundant loading
        if (stateFlow.value == AvatarImageState.Loading || stateFlow.value is AvatarImageState.Data) {
            return
        }

        stateFlow.value = AvatarImageState.Loading

        coroutineScope.launch {


            val senderImage = getSenderImage(address, null)
            val newState = if (senderImage?.imageFile?.existsWithNonZeroLength() == true) {
                AvatarImageState.Data(senderImage.imageFile)
            } else {
                AvatarImageState.NoImageAvailable
            }

            stateFlow.value = newState

            // Atomic update of all states
            allStatesFlow.update { current ->
                current.copy(states = avatarImageStateMap.mapValues { it.value.value })
            }
        }
    }

    override fun getAvatarImageState(address: String): AvatarImageState =
        avatarImageStateMap[address]?.value ?: AvatarImageState.NotLoaded

    override fun observeAvatarImageStates(): Flow<AvatarImageStates> = allStatesFlow

    override fun handleLoadingFailure(address: String, bimiSelector: String?) {
        avatarImageStateMap.remove(address)?.let { state ->
            allStatesFlow.update { current ->
                current.copy(states = avatarImageStateMap.mapValues { it.value.value })
            }
        }

        // Retry loading the image
        loadImage(address, bimiSelector)
    }

    private fun File.existsWithNonZeroLength() = exists() && length() > 0
}

