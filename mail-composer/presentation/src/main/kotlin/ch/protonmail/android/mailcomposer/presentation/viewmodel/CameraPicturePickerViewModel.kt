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

package ch.protonmail.android.mailcomposer.presentation.viewmodel

import java.io.File
import java.time.LocalDate
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcommon.presentation.usecase.FormatLocalDate
import ch.protonmail.android.mailcomposer.domain.repository.CameraTempImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CameraPicturePickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val formatLocalDate: FormatLocalDate,
    private val cameraTempImageRepository: CameraTempImageRepository
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Initial)
    val state = _state.asStateFlow()

    fun onPermissionGranted() {
        viewModelScope.launch {
            generateFileForCapture()
        }
    }

    fun onCapturePictureRequested() {
        viewModelScope.launch {
            _state.emit(State.CheckPermissions)
        }
    }

    private suspend fun generateFileForCapture() = when (val file = getTempImageFile()) {
        null -> {
            Timber.w("Failed to generate temp file in cache dir to capture image")
            _state.emit(State.Error.FileError)
        }
        else -> {
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            _state.emit(State.FileInfo(fileUri))
        }
    }

    private suspend fun getTempImageFile(): File? {
        val date = formatLocalDate(LocalDate.now())
        val extension = ".jpg"
        val fileName = "camera-capture-$date$extension"

        return cameraTempImageRepository.getFile(fileName).getOrNull()
    }

    sealed interface State {
        data object Initial : State
        data object CheckPermissions : State
        data class FileInfo(val fileUri: Uri) : State
        sealed interface Error : State {
            data object FileError : State
        }
    }

}
