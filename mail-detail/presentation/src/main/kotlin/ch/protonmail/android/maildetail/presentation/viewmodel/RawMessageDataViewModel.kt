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

package ch.protonmail.android.maildetail.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.maildetail.domain.usecase.DownloadRawMessageData
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.RawMessageDataState
import ch.protonmail.android.maildetail.presentation.model.RawMessageDataType
import ch.protonmail.android.maildetail.presentation.ui.RawMessageDataScreen
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetRawMessageBody
import ch.protonmail.android.mailmessage.domain.usecase.GetRawMessageHeaders
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.deserializeOrNull
import javax.inject.Inject

@HiltViewModel
class RawMessageDataViewModel @Inject constructor(
    private val downloadRawMessageData: DownloadRawMessageData,
    private val getRawMessageBody: GetRawMessageBody,
    private val getRawMessageHeaders: GetRawMessageHeaders,
    private val savedStateHandle: SavedStateHandle,
    observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId().filterNotNull()

    private val messageId = requireMessageId()
    private val rawMessageDataType = requireRawMessageDataType()

    private val mutableState = MutableStateFlow<RawMessageDataState>(RawMessageDataState.Loading(rawMessageDataType))
    val state: StateFlow<RawMessageDataState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val event = when (rawMessageDataType) {
                RawMessageDataType.Headers -> getRawMessageHeaders(primaryUserId.first(), messageId).fold(
                    ifLeft = { RawMessageDataState.Error(rawMessageDataType) },
                    ifRight = { RawMessageDataState.Data(rawMessageDataType, it.value, Effect.empty()) }
                )
                RawMessageDataType.HTML -> getRawMessageBody(primaryUserId.first(), messageId).fold(
                    ifLeft = { RawMessageDataState.Error(rawMessageDataType) },
                    ifRight = { RawMessageDataState.Data(rawMessageDataType, it.value, Effect.empty()) }
                )
            }

            mutableState.emit(event)
        }
    }

    fun downloadData(type: RawMessageDataType, data: String) = viewModelScope.launch {
        val fileName = when (type) {
            RawMessageDataType.Headers -> "headers"
            RawMessageDataType.HTML -> "html"
        }
        downloadRawMessageData(fileName, data).fold(
            ifLeft = {
                mutableState.update {
                    if (it is RawMessageDataState.Data) {
                        it.copy(toast = Effect.of(R.string.raw_message_data_failed_download))
                    } else {
                        it
                    }
                }
            },
            ifRight = {
                mutableState.update {
                    if (it is RawMessageDataState.Data) {
                        it.copy(toast = Effect.of(R.string.raw_message_data_successful_download))
                    } else {
                        it
                    }
                }
            }
        )
    }

    private fun requireMessageId(): MessageId {
        val messageIdParam = savedStateHandle.get<String>(RawMessageDataScreen.MESSAGE_ID_KEY)
            ?: throw IllegalStateException("No Message id given")

        return MessageId(messageIdParam)
    }

    private fun requireRawMessageDataType(): RawMessageDataType {
        return savedStateHandle.get<String>(
            RawMessageDataScreen.RAW_DATA_TYPE_KEY
        )?.deserializeOrNull() ?: throw IllegalStateException("No raw message data type given")
    }
}
