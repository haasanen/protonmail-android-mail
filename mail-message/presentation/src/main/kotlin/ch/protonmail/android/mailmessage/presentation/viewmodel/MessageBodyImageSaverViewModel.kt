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

package ch.protonmail.android.mailmessage.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailattachments.presentation.ExternalAttachmentErrorResult
import ch.protonmail.android.mailattachments.presentation.ExternalAttachmentsHandler
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.usecase.LoadMessageBodyImage
import ch.protonmail.android.mailmessage.presentation.model.BodyImageSaveState
import ch.protonmail.android.mailmessage.presentation.model.BodyImageSaveState.Error
import ch.protonmail.android.mailmessage.presentation.model.BodyImageUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageBodyImageSaverViewModel @Inject constructor(
    private val externalAttachmentsHandler: ExternalAttachmentsHandler,
    private val loadMessageBodyImage: LoadMessageBodyImage,
    private val observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    private val mutableSaveState = MutableStateFlow<BodyImageSaveState>(BodyImageSaveState.Idle)
    val saveState = mutableSaveState.asStateFlow()

    fun requestSave(uiModel: BodyImageUiModel) {
        viewModelScope.launch {
            getImageData(uiModel)
                .onLeft { mutableSaveState.value = Error(it) }
                .onRight { mutableSaveState.value = BodyImageSaveState.RequestingSave(uiModel, it) }
        }
    }

    fun performSave(destinationUri: Uri, bodyImage: MessageBodyImage) {
        viewModelScope.launch {
            mutableSaveState.value = BodyImageSaveState.Saving

            externalAttachmentsHandler.saveDataToDestination(
                destinationUri = destinationUri,
                mimeType = bodyImage.mimeType,
                data = bodyImage.data
            )
                .onLeft { mutableSaveState.value = Error(it) }
                .onRight { mutableSaveState.value = BodyImageSaveState.Saved.UserPicked }
        }
    }

    fun performSaveToDownloadFolder(uiModel: BodyImageUiModel, bodyImage: MessageBodyImage) {
        viewModelScope.launch {
            mutableSaveState.value = BodyImageSaveState.Saving

            externalAttachmentsHandler.saveDataToDownloads(
                fileName = uiModel.imageUrl,
                mimeType = bodyImage.mimeType,
                data = bodyImage.data
            )
                .onLeft { mutableSaveState.value = Error(it) }
                .onRight { mutableSaveState.value = BodyImageSaveState.Saved.FallbackLocation }
        }
    }

    fun resetState() {
        mutableSaveState.value = BodyImageSaveState.Idle
    }

    fun markLaunchAsConsumed() {
        val current = mutableSaveState.value
        if (current is BodyImageSaveState.RequestingSave) {
            mutableSaveState.value = BodyImageSaveState.WaitingForUser(current.uiModel, current.content)
        }
    }

    private suspend fun getImageData(
        bodyImage: BodyImageUiModel
    ): Either<ExternalAttachmentErrorResult, MessageBodyImage> {
        val userId = observePrimaryUserId().firstOrNull()

        if (userId == null) {
            return ExternalAttachmentErrorResult.UserNotFound.left()
        }

        return loadMessageBodyImage(userId, bodyImage.messageId, bodyImage.imageUrl, shouldLoadImagesSafely = true)
            .mapLeft { ExternalAttachmentErrorResult.UnableToLoadImage }
    }

}
