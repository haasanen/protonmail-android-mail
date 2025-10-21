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

package ch.protonmail.android.mailmessage.presentation.ui

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailattachments.presentation.R
import ch.protonmail.android.mailattachments.presentation.ui.CreateDocumentWithMimeType
import ch.protonmail.android.mailattachments.presentation.ui.SaveAttachmentInput
import ch.protonmail.android.mailmessage.presentation.model.BodyImageUiModel
import ch.protonmail.android.mailmessage.presentation.model.BodyImageSaveState
import ch.protonmail.android.mailmessage.presentation.viewmodel.MessageBodyImageSaverViewModel
import timber.log.Timber

/**
 * A Composable that abstracts the save of message body images to an external location.
 */
@Composable
@Suppress("UseComposableActions")
fun messageBodyImageSaver(
    onFileSaving: (() -> Unit)? = null,
    onFileSaved: ((String) -> Unit),
    onError: ((String) -> Unit)
): (BodyImageUiModel) -> Unit {
    val viewModel = hiltViewModel<MessageBodyImageSaverViewModel>()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()

    val fileSaveLauncher = rememberLauncherForActivityResult(
        contract = CreateDocumentWithMimeType()
    ) { destinationUri: Uri? ->
        val state = saveState
        if (destinationUri != null && state is BodyImageSaveState.WaitingForUser) {
            viewModel.performSave(destinationUri, state.content)
        } else {
            viewModel.resetState()
        }
    }

    val fileSavedString = stringResource(R.string.file_saved)
    val fileSavedFallbackString = stringResource(R.string.file_saved_fallback)
    val fileSavedError = stringResource(R.string.error_saving_file)

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is BodyImageSaveState.RequestingSave -> {
                val attachmentInput = SaveAttachmentInput(
                    state.uiModel.imageUrl,
                    state.content.mimeType
                )

                try {
                    fileSaveLauncher.launch(attachmentInput)
                    viewModel.markLaunchAsConsumed()
                } catch (_: ActivityNotFoundException) {
                    Timber.d("Unable to find a suitable target for saving - fallback to Downloads folder")
                    viewModel.performSaveToDownloadFolder(state.uiModel, state.content)
                }
            }

            is BodyImageSaveState.Saving -> {
                onFileSaving?.invoke()
            }

            is BodyImageSaveState.Saved.UserPicked -> {
                onFileSaved.invoke(fileSavedString)
                viewModel.resetState()
            }

            is BodyImageSaveState.Saved.FallbackLocation -> {
                onFileSaved.invoke(fileSavedFallbackString)
                viewModel.resetState()
            }

            is BodyImageSaveState.Error -> {
                onError.invoke(fileSavedError)
                viewModel.resetState()
            }

            is BodyImageSaveState.Idle,
            is BodyImageSaveState.WaitingForUser -> Unit
        }
    }

    return { imageBodyUiModel -> viewModel.requestSave(imageBodyUiModel) }
}
