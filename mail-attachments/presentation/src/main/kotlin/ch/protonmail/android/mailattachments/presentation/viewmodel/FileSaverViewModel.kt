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

package ch.protonmail.android.mailattachments.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailattachments.presentation.ExternalAttachmentsHandler
import ch.protonmail.android.mailattachments.presentation.model.FileContent
import ch.protonmail.android.mailattachments.presentation.model.FileSaveState
import ch.protonmail.android.mailattachments.presentation.ui.SaveAttachmentInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileSaverViewModel @Inject constructor(
    private val externalAttachmentsHandler: ExternalAttachmentsHandler
) : ViewModel() {

    private val mutableSaveState = MutableStateFlow<FileSaveState>(FileSaveState.Idle)
    val saveState = mutableSaveState.asStateFlow()

    fun requestSave(content: FileContent) {
        mutableSaveState.value = FileSaveState.RequestingSave(content)
    }

    fun performSave(destinationUri: Uri, sourceUri: Uri) {
        viewModelScope.launch {
            mutableSaveState.value = FileSaveState.Saving

            externalAttachmentsHandler.copyUriToDestination(sourceUri = sourceUri, destinationUri = destinationUri)
                .onLeft { mutableSaveState.value = FileSaveState.Error(it) }
                .onRight { mutableSaveState.value = FileSaveState.Saved.UserPicked }
        }
    }

    fun performSaveToDownloadFolder(attachmentInput: SaveAttachmentInput) {
        viewModelScope.launch {
            mutableSaveState.value = FileSaveState.Saving

            externalAttachmentsHandler.saveFileToDownloadsFolder(attachmentInput)
                .onLeft { mutableSaveState.value = FileSaveState.Error(it) }
                .onRight { mutableSaveState.value = FileSaveState.Saved.FallbackLocation }
        }
    }

    fun resetState() {
        mutableSaveState.value = FileSaveState.Idle
    }

    fun markLaunchAsConsumed() {
        val current = mutableSaveState.value
        if (current is FileSaveState.RequestingSave) {
            mutableSaveState.value = FileSaveState.WaitingForUser(current.content)
        }
    }
}
