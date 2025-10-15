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

package ch.protonmail.android.mailattachments.presentation.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailattachments.presentation.model.FileContent
import ch.protonmail.android.mailattachments.presentation.model.FileSaveState
import ch.protonmail.android.mailattachments.presentation.viewmodel.FileSaverViewModel

/**
 * A Composable that abstracts the save of file contents to an external location.
 */
@Composable
@Suppress("UseComposableActions")
fun fileSaver(
    onFileSaving: (() -> Unit)? = null,
    onFileSaved: (() -> Unit)? = null,
    onError: ((Exception) -> Unit)? = null
): (FileContent) -> Unit {
    val viewModel = hiltViewModel<FileSaverViewModel>()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()

    val fileSaveLauncher = rememberLauncherForActivityResult(
        contract = CreateDocumentWithMimeType()
    ) { destinationUri: Uri? ->
        val state = saveState
        if (destinationUri != null && state is FileSaveState.WaitingForUser) {
            viewModel.performSave(destinationUri, state.content.uri)
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is FileSaveState.RequestingSave -> {
                fileSaveLauncher.launch(SaveAttachmentInput(state.content.name, state.content.mimeType))
                viewModel.markLaunchAsConsumed()
            }

            is FileSaveState.Saving -> {
                onFileSaving?.invoke()
            }

            is FileSaveState.Saved -> {
                onFileSaved?.invoke()
                viewModel.resetState()
            }

            is FileSaveState.Error -> {
                onError?.invoke(state.exception)
                viewModel.resetState()
            }

            is FileSaveState.Idle,
            is FileSaveState.WaitingForUser -> Unit
        }
    }

    return { content -> viewModel.requestSave(content) }
}
