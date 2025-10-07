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

import java.io.IOException
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailattachments.presentation.model.FileContent
import ch.protonmail.android.mailattachments.presentation.model.FileSaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FileSaverViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val mutableSaveState = MutableStateFlow<FileSaveState>(FileSaveState.Idle)
    val saveState = mutableSaveState.asStateFlow()

    fun requestSave(content: FileContent) {
        mutableSaveState.value = FileSaveState.RequestingSave(content)
    }

    fun performSave(destinationUri: Uri, sourceUri: Uri) {
        viewModelScope.launch {
            mutableSaveState.value = FileSaveState.Saving
            try {
                copyUriToExternal(context, sourceUri = sourceUri, destinationUri = destinationUri)
                mutableSaveState.value = FileSaveState.Saved
            } catch (e: IOException) {
                mutableSaveState.value = FileSaveState.Error(e)
            }
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

    private suspend fun copyUriToExternal(
        context: Context,
        sourceUri: Uri,
        destinationUri: Uri
    ) = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                inputStream.copyTo(outputStream, bufferSize = 8 * 1024)
            }
        } ?: throw IOException("Failed to open streams for file copy")
    }
}
